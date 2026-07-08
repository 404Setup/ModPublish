/*
 * Copyright (C) 2025 - 2026 404Setup (https://github.com/404Setup)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';
import * as crypto from 'crypto';
import {Lang} from '../utils/i18n';
import {Version, VersionConstraintParser, VersionUtil} from '../utils/versions';
import {LocalModInfo} from '../parser/modParser';
import {DependencyInfo, PublishData} from '../publisher/api';
import {ModrinthAPI} from '../publisher/modrinth';
import {CurseForgeAPI} from '../publisher/curseforge';
import {GithubAPI} from '../publisher/github';
import {GitlabAPI} from '../publisher/gitlab';

export class PublishModPanel {
    public static currentPanel: PublishModPanel | undefined;
    private readonly _panel: vscode.WebviewPanel;
    private readonly _context: vscode.ExtensionContext;
    private readonly _files: string[];
    private readonly _modInfo: LocalModInfo;
    private _disposables: vscode.Disposable[] = [];

    private constructor(
        panel: vscode.WebviewPanel,
        context: vscode.ExtensionContext,
        files: string[],
        modInfo: LocalModInfo
    ) {
        this._panel = panel;
        this._context = context;
        this._files = files;
        this._modInfo = modInfo;

        this._update();

        this._panel.onDidDispose(() => this.dispose(), null, this._disposables);

        this._panel.webview.onDidReceiveMessage(
            async (message) => {
                switch (message.command) {
                    case 'cancel':
                        this._panel.dispose();
                        break;
                    case 'saveConfig':
                        await this._saveConfig(message.data);
                        vscode.window.showInformationMessage(Lang.get('title.success'));
                        break;
                    case 'resolveDependency':
                        await this._resolveDependency(message.dependency);
                        break;
                    case 'publish':
                        await this._publish(message.data);
                        break;
                    case 'updateVersionList': {
                        const success = await VersionUtil.updateVersions(this._context);
                        if (success) {
                            this._panel.webview.postMessage({
                                command: 'versionsUpdated',
                                reason: 'update',
                                versions: VersionUtil.getVersions(this._context)
                            });
                        } else {
                            this._panel.webview.postMessage({
                                command: 'versionsUpdateFailed'
                            });
                        }
                        break;
                    }
                    case 'clearVersionListCache': {
                        vscode.window.showWarningMessage(message.text || 'Clear version list cache?', { modal: true }, 'OK').then(answer => {
                            if (answer === 'OK') {
                                VersionUtil.clearCache(this._context);
                                this._panel.webview.postMessage({
                                    command: 'versionsUpdated',
                                    reason: 'clear',
                                    versions: VersionUtil.getVersions(this._context)
                                });
                            }
                        });
                        break;
                    }
                    case 'showNotification': {
                        if (message.type === 'error') {
                            vscode.window.showErrorMessage(message.text);
                        } else if (message.type === 'warning') {
                            vscode.window.showWarningMessage(message.text);
                        } else {
                            vscode.window.showInformationMessage(message.text);
                        }
                        break;
                    }
                    case 'openExternal': {
                        try {
                            const uri = vscode.Uri.parse(String(message.url), true);
                            if (uri.scheme === 'https' || uri.scheme === 'http') {
                                vscode.env.openExternal(uri);
                            }
                        } catch {
                            // Ignore invalid URLs coming from the webview
                        }
                        break;
                    }
                    case 'renderMarkdown': {
                        vscode.commands.executeCommand('markdown.api.render', message.text || '').then(html => {
                            this._panel.webview.postMessage({
                                command: 'markdownRendered',
                                html: typeof html === 'string' ? html : (html as any)?.value || ''
                            });
                        });
                        break;
                    }
                }
            },
            null,
            this._disposables
        );
    }

    public static show(context: vscode.ExtensionContext, files: string[], modInfo: LocalModInfo) {
        const column = vscode.window.activeTextEditor ? vscode.window.activeTextEditor.viewColumn : undefined;

        if (PublishModPanel.currentPanel) {
            PublishModPanel.currentPanel._panel.dispose();
        }

        const panel = vscode.window.createWebviewPanel(
            'modpublish',
            Lang.get('action.modpublish.action.publish.text') + ` - ${modInfo.name}`,
            column || vscode.ViewColumn.One,
            {
                enableScripts: true,
                retainContextWhenHidden: true,
                localResourceRoots: [
                    vscode.Uri.file(path.join(context.extensionPath, 'src', 'webview'))
                ]
            }
        );

        PublishModPanel.currentPanel = new PublishModPanel(panel, context, files, modInfo);
    }

    private dispose() {
        PublishModPanel.currentPanel = undefined;

        this._panel.dispose();

        while (this._disposables.length) {
            const x = this._disposables.pop();
            if (x) {
                x.dispose();
            }
        }
    }

    private async _update() {
        this._panel.webview.html = await this._getHtmlForWebview();
    }

    private _getConfig(): vscode.WorkspaceConfiguration {
        const workspaceFolder = vscode.workspace.getWorkspaceFolder(vscode.Uri.file(this._files[0]));
        return vscode.workspace.getConfiguration('modpublish', workspaceFolder?.uri);
    }

    private async _getToken(config: vscode.WorkspaceConfiguration, key: string): Promise<string> {
        return config.get<string>(`${key}`) || await this._context.secrets.get(`modpublish.${key}`) || '';
    }

    private async _getTokens(config: vscode.WorkspaceConfiguration): Promise<Record<string, string>> {
        return {
            modrinth: await this._getToken(config, 'modrinth.token'),
            curseforge: await this._getToken(config, 'curseforge.token'),
            curseforgeStudio: await this._getToken(config, 'curseforge.studioToken'),
            github: await this._getToken(config, 'github.token'),
            gitlab: await this._getToken(config, 'gitlab.token')
        };
    }

    /**
     * Serializes data for inline script injection, escaping '<' to prevent
     * script-tag breakout (XSS) from untrusted values such as mod metadata.
     */
    private static _safeJson(value: any): string {
        return JSON.stringify(value).replace(/</g, '\\u003c');
    }

    private async _getHtmlForWebview(): Promise<string> {
        const htmlPath = path.join(this._context.extensionPath, 'src', 'webview', 'webview.html');
        const cssPath = path.join(this._context.extensionPath, 'src', 'webview', 'webview.css');
        const jsPath = path.join(this._context.extensionPath, 'src', 'webview', 'webview.js');

        let html = fs.readFileSync(htmlPath, 'utf8');
        const css = fs.readFileSync(cssPath, 'utf8');
        const js = fs.readFileSync(jsPath, 'utf8');

        const config = this._getConfig();
        const tokens = await this._getTokens(config);
        const modrinthToken = tokens.modrinth;
        const curseforgeToken = tokens.curseforge;
        const githubToken = tokens.github;
        const gitlabToken = tokens.gitlab;
        const curseforgeStudioToken = tokens.curseforgeStudio;

        const workspaceConfig = {
            modrinthConfigured: !!(modrinthToken && config.get('modrinth.modid')),
            curseforgeConfigured: !!(curseforgeToken && config.get('curseforge.modid')),
            githubConfigured: !!(githubToken && config.get('github.repo')),
            gitlabConfigured: !!(gitlabToken && config.get('gitlab.repo')),
            modrinthTokenAvailable: !!modrinthToken,
            curseforgeTokenAvailable: !!curseforgeToken,
            curseforgeStudioTokenAvailable: !!curseforgeStudioToken,
            githubTokenAvailable: !!githubToken,
            gitlabTokenAvailable: !!gitlabToken,
            modrinthIdAvailable: !!config.get('modrinth.modid'),
            curseforgeIdAvailable: !!config.get('curseforge.modid'),
            githubRepoAvailable: !!config.get('github.repo'),
            gitlabRepoAvailable: !!config.get('gitlab.repo'),
            releaseChannel: config.get('common.releaseChannel') || 'release',
            changelog: config.get('common.changelog') || '',
            environment: config.get('common.environment') || 'client_and_server',
            versionFormat: config.get('common.versionFormat') || '',
            dependencies: config.get('common.dependencies') || []
        };

        const i18nData = Lang.loadMergedLocaleData(this._context);

        const versions = VersionUtil.getVersions(this._context);

        const selectedVersions = this._parseVersionRange(this._modInfo.versionRange, versions);

        const nonce = crypto.randomBytes(16).toString('base64');
        const csp = `<meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src 'unsafe-inline'; script-src 'nonce-${nonce}'; img-src data: https:;">`;

        const dataInjection = `
            <script nonce="${nonce}">
                window.FILES = ${PublishModPanel._safeJson(this._files)};
                window.MINECRAFT_VERSIONS = ${PublishModPanel._safeJson(versions)};
                window.SELECTED_MC_VERSIONS = ${PublishModPanel._safeJson(selectedVersions)};
                window.MOD_INFO = ${PublishModPanel._safeJson(this._modInfo)};
                window.CONFIG = ${PublishModPanel._safeJson(workspaceConfig)};
                window.I18N = ${PublishModPanel._safeJson(i18nData)};
            </script>
        `;

        html = html.replace('<head>', `<head>${csp}${dataInjection}`);
        html = html.replace('<script>', `<script nonce="${nonce}">`);
        html = html.replace('/*INJECT_CSS*/', css);
        html = html.replace('/*INJECT_JS*/', js);

        return html;
    }

    private _parseVersionRange(range: string, versions: any[]): string[] {
        if (!range) return [];
        try {
            const constraint = VersionConstraintParser.parse(range);
            return versions.filter(item => {
                if (item.t !== 'release') return false;
                try {
                    const v = new Version(item.v);
                    return constraint.satisfies(v);
                } catch {
                    return false;
                }
            }).map(v => v.v);
        } catch (e) {
            console.error('Failed to parse version range:', range, e);
            return [];
        }
    }

    private async _saveConfig(data: any) {
        const config = this._getConfig();
        await config.update('common.releaseChannel', data.releaseChannel, vscode.ConfigurationTarget.WorkspaceFolder);
        await config.update('common.changelog', data.changelog, vscode.ConfigurationTarget.WorkspaceFolder);
        await config.update('common.environment', data.environment, vscode.ConfigurationTarget.WorkspaceFolder);

        const savedDeps = data.dependencies.map((d: any) => ({
            projectId: d.projectId,
            type: d.type,
            customTitle: d.customTitle,
            modrinthModInfo: d.modrinthModInfo,
            curseforgeModInfo: d.curseforgeModInfo
        }));
        await config.update('common.dependencies', savedDeps, vscode.ConfigurationTarget.WorkspaceFolder);
    }

    private async _resolveDependency(dep: DependencyInfo) {
        const config = this._getConfig();
        const modrinthToken = await this._getToken(config, 'modrinth.token');
        const curseforgeStudioToken = await this._getToken(config, 'curseforge.studioToken');

        const parts = dep.projectId.split(',');
        const modrinthId = parts[0]?.trim();
        const curseforgeId = parts[1]?.trim() || (parts.length === 1 ? parts[0].trim() : '');

        let modrinthErr = '';
        let curseforgeErr = '';

        if (modrinthId) {
            const modrinthAPI = new ModrinthAPI();
            const info = await modrinthAPI.getModInfo(modrinthId, modrinthToken);
            if (info) {
                dep.modrinthModInfo = info;
                dep.customTitle = info.title;
            } else {
                modrinthErr = 'Modrinth mod not found or API error';
            }
        }

        if (curseforgeId) {
            if (!curseforgeStudioToken) {
                curseforgeErr = Lang.get('failed.11') || 'CurseForge Studio API Token is missing or invalid';
            } else {
                const curseforgeAPI = new CurseForgeAPI();
                const info = await curseforgeAPI.getModInfo(curseforgeId, curseforgeStudioToken);
                if (info) {
                    dep.curseforgeModInfo = info;
                    if (!dep.customTitle) dep.customTitle = info.title;
                } else {
                    curseforgeErr = 'CurseForge mod not found or API error';
                }
            }
        }

        const err = modrinthErr || curseforgeErr;

        this._panel.webview.postMessage({
            command: 'resolvedDependency',
            success: !err,
            error: err,
            dependency: dep
        });
    }

    private async _publish(data: any) {
        const config = this._getConfig();
        const tokens = await this._getTokens(config);

        const versions = VersionUtil.getVersions(this._context);
        const versionMap: Record<string, number> = {};
        versions.forEach(v => {
            versionMap[v.v] = v.i;
        });

        const publishConfigs: Record<string, any> = {
            modrinthModId: config.get('modrinth.modid'),
            curseforgeModId: config.get('curseforge.modid'),
            githubRepo: config.get('github.repo'),
            githubBranch: config.get('github.branch'),
            gitlabRepo: config.get('gitlab.repo'),
            gitlabBranch: config.get('gitlab.branch'),
            minecraftVersionCfIds: versionMap
        };

        // Only allow files that were originally selected; never trust paths coming from the webview.
        const primaryFile = this._files.includes(data.primaryFile) ? data.primaryFile : this._files[0];
        const sortedFiles = [primaryFile];
        this._files.forEach(f => {
            if (f !== primaryFile) {
                sortedFiles.push(f);
            }
        });

        const publishData: PublishData = {
            versionName: data.versionName,
            versionNumber: data.versionNumber,
            releaseChannel: data.releaseChannel,
            loaders: data.loaders,
            clientRequired: data.clientRequired,
            serverRequired: data.serverRequired,
            minecraftVersions: data.minecraftVersions,
            changelog: data.changelog,
            dependencies: data.dependencies,
            environment: data.environment,
            files: sortedFiles
        };

        const publishPromises = data.targets.map(async (target: string) => {
            return vscode.window.withProgress({
                location: vscode.ProgressLocation.Notification,
                title: `${Lang.get('action.modpublish.action.publish.text')} [${target}]`,
                cancellable: false
            }, async (progress) => {
                progress.report({ message: Lang.get('button.publishing') });

                let publisher;
                if (target === 'modrinth') publisher = new ModrinthAPI();
                else if (target === 'curseforge') publisher = new CurseForgeAPI();
                else if (target === 'github') publisher = new GithubAPI();
                else if (target === 'gitlab') publisher = new GitlabAPI();

                if (publisher) {
                    const res = await publisher.publish(publishData, tokens, publishConfigs);
                    if (res.success) {
                        vscode.window.showInformationMessage(`[${target}] ${Lang.get('message.success') || 'Publish successfully'}`);
                    } else {
                        const errMsg = Lang.get(res.message || '') || res.message;
                        vscode.window.showErrorMessage(`[${target}] ${errMsg}`);
                    }
                    return { target, success: res.success, platform: res.platform, message: res.message };
                }
                return { target, success: true };
            });
        });

        const results = await Promise.all(publishPromises);
        const failedResults = results.filter(r => !r.success);

        this._panel.webview.postMessage({
            command: 'publishResult',
            success: failedResults.length === 0,
            silent: true
        });

        if (failedResults.length === 0) {
            setTimeout(() => {
                this._panel.dispose();
            }, 2000);
        }
    }
}
