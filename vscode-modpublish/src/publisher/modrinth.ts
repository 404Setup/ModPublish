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

import * as fs from 'fs';
import * as path from 'path';
import {API, PublishData, PublishResult} from './api';

export class ModrinthAPI extends API {
    readonly id = 'Modrinth';
    private readonly baseUrl = 'https://api.modrinth.com/v2/';

    async publish(data: PublishData, tokens: Record<string, string>, config: Record<string, any>): Promise<PublishResult> {
        const token = tokens['modrinth'];
        const projectId = config['modrinthModId'];

        if (!token || !projectId) {
            return {success: false, platform: this.id, message: 'tooltip.modrinth.disable'};
        }

        try {
            const url = `${this.baseUrl}version`;
            const form = new FormData();

            const modrinthData = {
                name: data.versionName,
                version_number: data.versionNumber,
                changelog: data.changelog,
                dependencies: data.dependencies.map(dep => {
                    const id = dep.modrinthModInfo?.modid || dep.projectId;
                    let depType = 'optional';
                    if (dep.type === 'required') depType = 'required';
                    else if (dep.type === 'embedded') depType = 'embedded';
                    else if (dep.type === 'incompatible') depType = 'incompatible';

                    return {
                        project_id: id,
                        dependency_type: depType
                    };
                }),
                game_versions: data.minecraftVersions,
                version_type: data.releaseChannel,
                loaders: data.loaders.map(l => l.toLowerCase()),
                featured: true,
                project_id: projectId,
                file_parts: data.files.map(f => path.basename(f))
            };

            form.append('data', JSON.stringify(modrinthData));

            for (let index = 0; index < data.files.length; index++) {
                const filePath = data.files[index];
                const fileName = path.basename(filePath);
                const fileKey = index === 0 ? `${fileName}-primary` : `${fileName}-${index - 1}`;
                const fileBuf = await fs.promises.readFile(filePath);
                form.append(fileKey, new Blob([fileBuf]), fileName);
            }

            const headers = {
                'User-Agent': 'modpublish-vsc/v1 (github.com/404Setup/ModPublish)',
                'Authorization': token
            };

            const response = await fetch(url, {
                method: 'POST',
                headers,
                body: form
            });

            const err = await this.validateResponse(response);
            if (err) {
                return {success: false, platform: this.id, message: err};
            }

            return {success: true, platform: this.id};
        } catch (e: any) {
            return {success: false, platform: this.id, message: e.message || String(e)};
        }
    }

    async getModInfo(modid: string, token: string): Promise<{ modid: string; slug: string; title: string } | null> {
        try {
            const url = `${this.baseUrl}project/${modid}`;
            const headers: Record<string, string> = {
                'User-Agent': 'modpublish/v1 (github.com/404Setup/ModPublish)'
            };
            if (token) {
                headers['Authorization'] = token;
            }

            const response = await fetch(url, {headers});
            if (response.status === 200) {
                const resData: any = await response.json().catch(() => null);
                if (resData) {
                    return {
                        modid: resData.id,
                        slug: resData.slug,
                        title: resData.title
                    };
                }
            }
        } catch (e) {
            console.error('Failed to get Modrinth mod info', e);
        }
        return null;
    }
}
