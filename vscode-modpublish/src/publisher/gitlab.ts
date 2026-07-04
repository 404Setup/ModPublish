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

export class GitlabAPI extends API {
    readonly id = 'GitLab';
    private readonly baseUrl = 'https://gitlab.com/api/v4/';

    async publish(data: PublishData, tokens: Record<string, string>, config: Record<string, any>): Promise<PublishResult> {
        const token = tokens['gitlab'];
        const repo = config['gitlabRepo'];
        const branch = config['gitlabBranch'] || 'main';

        if (!token || !repo) {
            return {success: false, platform: this.id, message: 'tooltip.git.disable'};
        }

        try {
            const projectPathEncoded = encodeURIComponent(repo);
            const tagName = data.versionNumber.startsWith('v') ? data.versionNumber : `v${data.versionNumber}`;

            const headers = {
                'User-Agent': 'modpublish-vsc/v1 (github.com/404Setup/ModPublish)',
                'Accept': 'application/json',
                'PRIVATE-TOKEN': token
            };

            const checkUrl = `${this.baseUrl}projects/${projectPathEncoded}/releases/${tagName}`;
            const checkResponse = await fetch(checkUrl, {headers});

            let releaseData: any = null;
            if (checkResponse.status === 200) {
                releaseData = await checkResponse.json().catch(() => null);
                const updateUrl = `${this.baseUrl}projects/${projectPathEncoded}/releases/${tagName}`;
                await fetch(updateUrl, {
                    method: 'PUT',
                    headers: { ...headers, 'Content-Type': 'application/json' },
                    body: JSON.stringify({description: data.changelog})
                });
            } else if (checkResponse.status === 404) {
                const createUrl = `${this.baseUrl}projects/${projectPathEncoded}/releases`;
                const createBody = {
                    tag_name: tagName,
                    ref: branch,
                    name: data.versionName,
                    description: data.changelog
                };

                const createResponse = await fetch(createUrl, {
                    method: 'POST',
                    headers: { ...headers, 'Content-Type': 'application/json' },
                    body: JSON.stringify(createBody)
                });
                const err = await this.validateResponse(createResponse);
                if (err) {
                    return {success: false, platform: this.id, message: `Failed to create release: ${err}`};
                }
                releaseData = await createResponse.json().catch(() => null);
            } else {
                return {
                    success: false,
                    platform: this.id,
                    message: `Failed checking tag: HTTP ${checkResponse.status}`
                };
            }

            const existingAssets = new Set<string>();
            if (releaseData && releaseData.assets && releaseData.assets.links) {
                releaseData.assets.links.forEach((link: any) => {
                    if (link.name) {
                        existingAssets.add(link.name);
                    }
                });
            }

            for (const filePath of data.files) {
                const fileName = path.basename(filePath);

                if (existingAssets.has(fileName)) {
                    continue;
                }

                const uploadUrl = `${this.baseUrl}projects/${projectPathEncoded}/uploads`;
                const form = new FormData();
                const fileBuf = await fs.promises.readFile(filePath);
                form.append('file', new Blob([fileBuf]), fileName);

                const uploadResponse = await fetch(uploadUrl, {
                    method: 'POST',
                    headers,
                    body: form
                });

                const uploadErr = await this.validateResponse(uploadResponse);
                if (uploadErr) {
                    return {success: false, platform: this.id, message: `Failed uploading ${fileName}: ${uploadErr}`};
                }

                const uploadData: any = await uploadResponse.json().catch(() => ({}));
                const uploadedUrl = uploadData.url;
                if (!uploadedUrl) {
                    return {
                        success: false,
                        platform: this.id,
                        message: `Failed uploading ${fileName}: Missing relative URL in response`
                    };
                }

                const gitlabRepoUrl = `https://gitlab.com/${repo.replace('%2F', '/')}`;
                const linkUrl = `${gitlabRepoUrl}${uploadedUrl}`;

                const linkApiUrl = `${this.baseUrl}projects/${projectPathEncoded}/releases/${tagName}/assets/links`;
                const linkBody = {
                    name: fileName,
                    url: linkUrl,
                    link_type: 'package'
                };

                const linkResponse = await fetch(linkApiUrl, {
                    method: 'POST',
                    headers: { ...headers, 'Content-Type': 'application/json' },
                    body: JSON.stringify(linkBody)
                });
                const linkErr = await this.validateResponse(linkResponse);
                if (linkErr) {
                    return {success: false, platform: this.id, message: `Failed linking ${fileName}: ${linkErr}`};
                }
            }

            return {success: true, platform: this.id};
        } catch (e: any) {
            return {success: false, platform: this.id, message: e.message || String(e)};
        }
    }

    async getModInfo(modid: string, token: string): Promise<{ modid: string; slug: string; title: string } | null> {
        // GitLab release target doesn't require looking up mod info
        return null;
    }
}
