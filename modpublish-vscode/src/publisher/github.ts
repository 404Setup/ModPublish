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

export class GithubAPI extends API {
    readonly id = 'Github';
    private readonly baseUrl = 'https://api.github.com/';

    async publish(data: PublishData, tokens: Record<string, string>, config: Record<string, any>): Promise<PublishResult> {
        const token = tokens['github'];
        const repo = config['githubRepo'];
        const branch = config['githubBranch'] || 'main';

        if (!token || !repo) {
            return this.failure('tooltip.git.disable');
        }

        return this.runPublish(async () => {
            const tagName = this.deriveTagName(data.versionNumber);
            const repoPath = repo.split('/').map(encodeURIComponent).join('/');

            const headers = this.buildHeaders({
                'Accept': 'application/vnd.github+json',
                'X-GitHub-Api-Version': '2022-11-28',
                'Authorization': `Bearer ${token}`
            });

            const checkUrl = `${this.baseUrl}repos/${repoPath}/releases/tags/${encodeURIComponent(tagName)}`;
            const checkResponse = await fetch(checkUrl, {headers});

            let releaseData: any = null;
            if (checkResponse.status === 200) {
                releaseData = await checkResponse.json().catch(() => null);
            } else if (checkResponse.status === 404) {
                const createUrl = `${this.baseUrl}repos/${repoPath}/releases`;
                const createBody = {
                    tag_name: tagName,
                    target_commitish: branch,
                    name: data.versionName,
                    body: data.changelog,
                    draft: false,
                    prerelease: data.releaseChannel !== 'release',
                    generate_release_notes: true,
                    make_latest: 'true'
                };

                const createResponse = await fetch(createUrl, {
                    method: 'POST',
                    headers: {
                        ...headers,
                        'Content-Type': 'application/json'
                    },
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

            if (!releaseData || !releaseData.upload_url) {
                return this.failure('Failed to get upload URL');
            }

            const rawUploadUrl = releaseData.upload_url.split('{')[0]; // Extract before '{?name,label}'

            for (const filePath of data.files) {
                const {blob, name: fileName} = await this.readFileBlob(filePath);
                const uploadAssetUrl = `${rawUploadUrl}?name=${encodeURIComponent(fileName)}`;

                const uploadResponse = await fetch(uploadAssetUrl, {
                    method: 'POST',
                    headers: {
                        ...headers,
                        'Content-Type': 'application/java-archive'
                    },
                    body: blob
                });

                const err = await this.validateResponse(uploadResponse);
                if (err) {
                    return this.failure(`Failed uploading ${fileName}: ${err}`);
                }
            }

            return this.success();
        });
    }

    async getModInfo(modid: string, token: string): Promise<ModRef | null> {
        // GitHub release target doesn't require looking up mod info
        return null;
    }
}
