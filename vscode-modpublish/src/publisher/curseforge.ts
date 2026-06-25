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

import axios from 'axios';
import * as fs from 'fs';
import * as path from 'path';
import FormData from 'form-data';
import {API, PublishData, PublishResult} from './api';

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
            return {success: false, platform: this.id, message: 'tooltip.curseforge.disable'};
        }

        try {
            let primaryFileId: number | null = null;

            for (let index = 0; index < data.files.length; index++) {
                const filePath = data.files[index];
                const fileName = path.basename(filePath);

                const form = new FormData();
                form.append('file', fs.createReadStream(filePath), {
                    filename: fileName,
                    contentType: 'application/java-archive'
                });

                const metadata: any = {
                    changelog: data.changelog,
                    changelogType: 'markdown',
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

                    metadata.relations = data.dependencies.map(dep => {
                        const slug = dep.curseforgeModInfo?.slug || dep.projectId;
                        let cfType = 'optionalDependency';
                        if (dep.type === 'required') cfType = 'requiredDependency';
                        else if (dep.type === 'embedded') cfType = 'embeddedLibrary';
                        else if (dep.type === 'incompatible') cfType = 'incompatible';

                        return {
                            slug: slug,
                            type: cfType
                        };
                    });
                }

                form.append('metadata', JSON.stringify(metadata));

                const url = `${this.uploadUrl}projects/${projectId}/upload-file`;
                const headers = {
                    ...form.getHeaders(),
                    'User-Agent': 'modpublish/v1 (github.com/404Setup/ModPublish)',
                    'X-Api-Token': token
                };

                const response = await axios.post(url, form, {headers, validateStatus: () => true});
                const err = this.validateResponse(response);
                if (err) {
                    return {success: false, platform: this.id, message: err};
                }

                if (index === 0 && response.data && response.data.id) {
                    primaryFileId = response.data.id;
                }
            }

            return {success: true, platform: this.id};
        } catch (e: any) {
            return {success: false, platform: this.id, message: e.message || String(e)};
        }
    }

    async getModInfo(modid: string, token: string): Promise<{ modid: string; slug: string; title: string } | null> {
        try {
            const url = `${this.apiUrl}mods/${modid}`;
            const headers = {
                'User-Agent': 'modpublish/v1 (github.com/404Setup/ModPublish)',
                'x-api-key': token
            };

            const response = await axios.get(url, {headers, validateStatus: () => true});
            if (response.status === 200 && response.data && response.data.data) {
                const mod = response.data.data;
                return {
                    modid: String(mod.id),
                    slug: mod.slug,
                    title: mod.name
                };
            }
        } catch (e) {
            console.error('Failed to get CurseForge mod info', e);
        }
        return null;
    }
}
