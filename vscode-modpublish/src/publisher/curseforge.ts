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

import {API, ModRef, PublishData, PublishResult} from './api';
import {markdownToHtml} from '../utils/markdownToHtml';

const DEP_TYPE_MAP = {
    required: 'requiredDependency',
    optional: 'optionalDependency',
    embedded: 'embeddedLibrary',
    incompatible: 'incompatible'
} as const;

export class CurseForgeAPI extends API {
    readonly id = 'CurseForge';
    private readonly uploadUrl = 'https://minecraft.curseforge.com/api/';
    private readonly apiUrl = 'https://api.curseforge.com/v1/';

    private readonly loaderCfIds: Record<string, number> = {
        'fabric': 7499,
        'quilt': 9153,
        'forge': 7498,
        'neoforge': 10150,
        'rift': 7500
    };

    async publish(data: PublishData, tokens: Record<string, string>, config: Record<string, any>): Promise<PublishResult> {
        const token = tokens['curseforge'];
        const projectId = config['curseforgeModId'];

        if (!token || !projectId) {
            return this.failure('tooltip.curseforge.disable');
        }

        return this.runPublish(async () => {
            let primaryFileId: number | null = null;

            for (let index = 0; index < data.files.length; index++) {
                const {blob, name: fileName} = await this.readFileBlob(data.files[index]);

                const form = new FormData();
                form.append('file', blob, fileName);

                const metadata: any = {
                    changelog: markdownToHtml(data.changelog),
                    changelogType: 'html',
                    displayName: data.versionName,
                    releaseType: data.releaseChannel
                };

                if (index > 0 && primaryFileId !== null) {
                    metadata.parentFileID = primaryFileId;
                } else {
                    const gameVersions: number[] = [];
                    const versionMap = config['minecraftVersionCfIds'] || {};
                    data.minecraftVersions.forEach(v => {
                        const id = versionMap[v];
                        if (id && id !== -1) {
                            gameVersions.push(id);
                        }
                    });

                    if (data.clientRequired) {
                        gameVersions.push(9638);
                    }
                    if (data.serverRequired) {
                        gameVersions.push(9639);
                    }

                    data.loaders.forEach(l => {
                        const cfId = this.loaderCfIds[l.toLowerCase()];
                        if (cfId && cfId > 0) {
                            gameVersions.push(cfId);
                        }
                    });

                    metadata.gameVersions = gameVersions;

                    if (data.dependencies && data.dependencies.length > 0) {
                        metadata.relations = {
                            projects: data.dependencies.map(dep => ({
                                slug: dep.curseforgeModInfo?.slug || dep.projectId,
                                type: this.mapDependencyType(dep.type, DEP_TYPE_MAP)
                            }))
                        };
                    }
                }

                form.append('metadata', JSON.stringify(metadata));

                const url = `${this.uploadUrl}projects/${encodeURIComponent(projectId)}/upload-file`;

                const response = await fetch(url, {
                    method: 'POST',
                    headers: this.buildHeaders({'X-Api-Token': token}),
                    body: form
                });
                const err = await this.validateResponse(response);
                if (err) {
                    return this.failure(err);
                }

                const resData: any = await response.json().catch(() => ({}));
                if (index === 0 && resData && resData.id) {
                    primaryFileId = resData.id;
                }
            }

            return this.success();
        });
    }

    async getModInfo(modid: string, token: string): Promise<ModRef | null> {
        try {
            const url = `${this.apiUrl}mods/${encodeURIComponent(modid)}`;
            const headers = this.buildHeaders({'x-api-key': token});

            const response = await fetch(url, {headers});
            if (response.status === 200) {
                const resData: any = await response.json().catch(() => null);
                if (resData && resData.data) {
                    const mod = resData.data;
                    return {
                        modid: String(mod.id),
                        slug: mod.slug,
                        title: mod.name
                    };
                }
            }
        } catch (e) {
            console.error('Failed to get CurseForge mod info', e);
        }
        return null;
    }
}
