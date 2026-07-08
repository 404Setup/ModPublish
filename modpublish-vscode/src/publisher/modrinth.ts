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

import * as path from 'path';
import {API, ModRef, PublishData, PublishResult} from './api';

const DEP_TYPE_MAP = {
    required: 'required',
    optional: 'optional',
    embedded: 'embedded',
    incompatible: 'incompatible'
} as const;

export class ModrinthAPI extends API {
    readonly id = 'Modrinth';
    private readonly baseUrl = 'https://api.modrinth.com/v2/';

    async publish(data: PublishData, tokens: Record<string, string>, config: Record<string, any>): Promise<PublishResult> {
        const token = tokens['modrinth'];
        const projectId = config['modrinthModId'];

        if (!token || !projectId) {
            return this.failure('tooltip.modrinth.disable');
        }

        return this.runPublish(async () => {
            const url = `${this.baseUrl}version`;
            const form = new FormData();

            const modrinthData = {
                name: data.versionName,
                version_number: data.versionNumber,
                changelog: data.changelog,
                dependencies: data.dependencies.map(dep => ({
                    project_id: dep.modrinthModInfo?.modid || dep.projectId,
                    dependency_type: this.mapDependencyType(dep.type, DEP_TYPE_MAP)
                })),
                game_versions: data.minecraftVersions,
                version_type: data.releaseChannel,
                loaders: data.loaders.map(l => l.toLowerCase()),
                featured: true,
                project_id: projectId,
                file_parts: data.files.map(f => path.basename(f)),
                environment: data.environment
            };

            form.append('data', JSON.stringify(modrinthData));

            for (let index = 0; index < data.files.length; index++) {
                const {blob, name} = await this.readFileBlob(data.files[index]);
                const fileKey = index === 0 ? `${name}-primary` : `${name}-${index - 1}`;
                form.append(fileKey, blob, name);
            }

            const response = await fetch(url, {
                method: 'POST',
                headers: this.buildHeaders({'Authorization': token}),
                body: form
            });

            const err = await this.validateResponse(response);
            if (err) {
                return this.failure(err);
            }

            return this.success();
        });
    }

    async getModInfo(modid: string, token: string): Promise<ModRef | null> {
        try {
            const url = `${this.baseUrl}project/${encodeURIComponent(modid)}`;
            const headers = this.buildHeaders(token ? {'Authorization': token} : {});

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
