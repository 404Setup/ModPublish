import axios from 'axios';
import * as fs from 'fs';
import * as path from 'path';
import FormData from 'form-data';
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

            form.append('data', JSON.stringify(modrinthData), {
                contentType: 'application/json'
            });

            data.files.forEach((filePath, index) => {
                const fileName = path.basename(filePath);
                const fileKey = index === 0 ? `${fileName}-primary` : `${fileName}-${index - 1}`;
                form.append(fileKey, fs.createReadStream(filePath), {
                    filename: fileName,
                    contentType: 'application/java-archive'
                });
            });

            const headers = {
                ...form.getHeaders(),
                'User-Agent': 'modpublish/v1 (github.com/404Setup/ModPublish)',
                'Authorization': token
            };

            const response = await axios.post(url, form, {headers, validateStatus: () => true});

            const err = this.validateResponse(response);
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

            const response = await axios.get(url, {headers, validateStatus: () => true});
            if (response.status === 200 && response.data) {
                return {
                    modid: response.data.id,
                    slug: response.data.slug,
                    title: response.data.title
                };
            }
        } catch (e) {
            console.error('Failed to get Modrinth mod info', e);
        }
        return null;
    }
}
