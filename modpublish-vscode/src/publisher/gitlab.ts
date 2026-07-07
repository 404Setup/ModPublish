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

export class GitlabAPI extends API {
    readonly id = 'GitLab';
    private readonly baseUrl = 'https://gitlab.com/api/v4/';

    async publish(data: PublishData, tokens: Record<string, string>, config: Record<string, any>): Promise<PublishResult> {
        const token = tokens['gitlab'];
        const repo = config['gitlabRepo'];
        const branch = config['gitlabBranch'] || 'main';

        if (!token || !repo) {
            return this.failure('tooltip.git.disable');
        }

        return this.runPublish(async () => {
            const projectPathEncoded = encodeURIComponent(repo);
            const tagName = this.deriveTagName(data.versionNumber);
            const tagNameEncoded = encodeURIComponent(tagName);

            const headers = this.buildHeaders({
                'Accept': 'application/json',
                'PRIVATE-TOKEN': token
            });

            const releaseUrl = `${this.baseUrl}projects/${projectPathEncoded}/releases/${tagNameEncoded}`;
            const checkResponse = await fetch(releaseUrl, {headers});

            let releaseData: any = null;
            if (checkResponse.status === 200) {
                releaseData = await checkResponse.json().catch(() => null);
                await fetch(releaseUrl, {
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
                    return this.failure(`Failed to create release: ${err}`);
                }
                releaseData = await createResponse.json().catch(() => null);
            } else {
                return this.failure(`Failed checking tag: HTTP ${checkResponse.status}`);
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
                const {blob} = await this.readFileBlob(filePath);
                form.append('file', blob, fileName);

                const uploadResponse = await fetch(uploadUrl, {
                    method: 'POST',
                    headers,
                    body: form
                });

                const uploadErr = await this.validateResponse(uploadResponse);
                if (uploadErr) {
                    return this.failure(`Failed uploading ${fileName}: ${uploadErr}`);
                }

                const uploadData: any = await uploadResponse.json().catch(() => ({}));
                const uploadedUrl = uploadData.url;
                if (!uploadedUrl) {
                    return this.failure(`Failed uploading ${fileName}: Missing relative URL in response`);
                }

                const gitlabRepoUrl = `https://gitlab.com/${repo.replace('%2F', '/')}`;
                const linkUrl = `${gitlabRepoUrl}${uploadedUrl}`;

                const linkApiUrl = `${releaseUrl}/assets/links`;
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
                    return this.failure(`Failed linking ${fileName}: ${linkErr}`);
                }
            }

            return this.success();
        });
    }

    async getModInfo(modid: string, token: string): Promise<ModRef | null> {
        // GitLab release target doesn't require looking up mod info
        return null;
    }
}
