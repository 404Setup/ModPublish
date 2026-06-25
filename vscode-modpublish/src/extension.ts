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
import * as path from 'path';
import {Lang} from './utils/i18n';
import {LocalModInfo, ModParser} from './parser/modParser';
import {GitUtil} from './utils/git';
import {PublishModPanel} from './webview/panel';

export function activate(context: vscode.ExtensionContext) {
    Lang.initialize(context);
    console.log('ModPublish extension is now active!');

    const publishCommand = vscode.commands.registerCommand('modpublish.publish', async (uri: vscode.Uri, uris?: vscode.Uri[]) => {
        const filesToScan: string[] = [];

        if (uris && uris.length > 0) {
            uris.forEach(u => {
                if (u.scheme === 'file') {
                    const ext = path.extname(u.fsPath).toLowerCase();
                    if (ext === '.jar' || ext === '.litemod') {
                        filesToScan.push(u.fsPath);
                    }
                }
            });
        } else if (uri && uri.scheme === 'file') {
            const ext = path.extname(uri.fsPath).toLowerCase();
            if (ext === '.jar' || ext === '.litemod') {
                filesToScan.push(uri.fsPath);
            }
        }

        if (filesToScan.length === 0) {
            vscode.window.showWarningMessage(Lang.get('message.invalid-file'));
            return;
        }

        await vscode.window.withProgress({
            location: vscode.ProgressLocation.Notification,
            title: Lang.get('action.modpublish.action.publish.text'),
            cancellable: false
        }, async (progress) => {
            progress.report({message: 'Scanning mod files...'});

            const validFiles: string[] = [];
            let primaryModInfo: LocalModInfo | null = null;

            for (const filePath of filesToScan) {
                const info = await ModParser.parse(filePath);
                if (info) {
                    validFiles.push(filePath);
                    if (!primaryModInfo) {
                        primaryModInfo = info;
                    }
                }
            }

            if (validFiles.length === 0 || !primaryModInfo) {
                vscode.window.showWarningMessage(Lang.get('message.invalid-file'));
                return;
            }

            let workspaceFolder = vscode.workspace.getWorkspaceFolder(vscode.Uri.file(validFiles[0]));
            if (!workspaceFolder && vscode.workspace.workspaceFolders && vscode.workspace.workspaceFolders.length > 0) {
                workspaceFolder = vscode.workspace.workspaceFolders[0];
            }
            if (workspaceFolder) {
                const gitInfo = await GitUtil.getGitInfo(workspaceFolder.uri.fsPath);
                if (gitInfo) {
                    const config = vscode.workspace.getConfiguration('modpublish', workspaceFolder.uri);

                    const githubRepo = config.get<string>('github.repo');
                    if ((!githubRepo || githubRepo.trim() === '') && gitInfo.host.toLowerCase().includes('github')) {
                        await config.update('github.repo', gitInfo.repoPath, vscode.ConfigurationTarget.WorkspaceFolder);
                    }
                    const githubBranch = config.get<string>('github.branch');
                    if (!githubBranch || githubBranch.trim() === '') {
                        await config.update('github.branch', gitInfo.branch, vscode.ConfigurationTarget.WorkspaceFolder);
                    }
                    const gitlabRepo = config.get<string>('gitlab.repo');
                    if ((!gitlabRepo || gitlabRepo.trim() === '') && gitInfo.host.toLowerCase().includes('gitlab')) {
                        await config.update('gitlab.repo', gitInfo.repoPath, vscode.ConfigurationTarget.WorkspaceFolder);
                    }
                    const gitlabBranch = config.get<string>('gitlab.branch');
                    if (!gitlabBranch || gitlabBranch.trim() === '') {
                        await config.update('gitlab.branch', gitInfo.branch, vscode.ConfigurationTarget.WorkspaceFolder);
                    }
                }
            }

            PublishModPanel.show(context, validFiles, primaryModInfo);
        });
    });

    const setTokenCommand = vscode.commands.registerCommand('modpublish.setToken', async () => {
        const platforms = [
            {label: 'Modrinth', key: 'modpublish.modrinth.token'},
            {label: 'CurseForge (Upload API)', key: 'modpublish.curseforge.token'},
            {label: 'CurseForge Studio (Mod Info API)', key: 'modpublish.curseforge.studioToken'},
            {label: 'GitHub', key: 'modpublish.github.token'},
            {label: 'GitLab', key: 'modpublish.gitlab.token'}
        ];

        const selection = await vscode.window.showQuickPick(platforms, {
            placeHolder: 'Select a platform to set the API Token for'
        });

        if (!selection) {
            return;
        }

        const tokenValue = await vscode.window.showInputBox({
            prompt: `Enter API Token/Key for ${selection.label}`,
            placeHolder: 'Paste token here',
            password: true
        });

        if (tokenValue === undefined) {
            return;
        }

        if (tokenValue.trim() === '') {
            await context.secrets.delete(selection.key);
            vscode.window.showInformationMessage(`Cleared token for ${selection.label}`);
        } else {
            await context.secrets.store(selection.key, tokenValue.trim());
            vscode.window.showInformationMessage(`Successfully securely saved token for ${selection.label}`);
        }
    });

    context.subscriptions.push(publishCommand, setTokenCommand);
}

export function deactivate() {
}
