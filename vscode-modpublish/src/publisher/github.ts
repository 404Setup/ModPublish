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

export class GithubAPI extends API {
    readonly id = 'Github';

    async publish(data: PublishData, tokens: Record<string, string>, config: Record<string, any>): Promise<PublishResult> {
        const token = tokens['github'];
        const repo = config['githubRepo'];
        const branch = config['githubBranch'] || 'main';

        if (!token || !repo) {
            return {success: false, platform: this.id, message: 'tooltip.git.disable'};
        }

        try {
            const tagName = data.versionNumber.startsWith('v') ? data.versionNumber : `v${data.versionNumber}`;

            const headers = {
                'User-Agent': 'modpublish-vsc/v1 (github.com/404Setup/ModPublish)',
                'Accept': 'application/vnd.github+json',
                'X-GitHub-Api-Version': '2022-11-28',
                'Authorization': `Bearer ${token}`
            };

            const checkUrl = `https://api.github.com/repos/${repo}/releases/tags/${tagName}`;
            const checkResponse = await fetch(checkUrl, {headers});

            let releaseData: any = null;
            if (checkResponse.status === 200) {
                releaseData = await checkResponse.json().catch(() => null);
            } else if (checkResponse.status === 404) {
                const createUrl = `https://api.github.com/repos/${repo}/releases`;
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

            if (!releaseData || !releaseData.upload_url) {
                return {success: false, platform: this.id, message: 'Failed to get upload URL'};
            }

            const rawUploadUrl = releaseData.upload_url.split('{')[0]; // Extract before '{?name,label}'

            for (const filePath of data.files) {
                const fileName = path.basename(filePath);
                const uploadAssetUrl = `${rawUploadUrl}?name=${encodeURIComponent(fileName)}`;

                const fileBuffer = fs.readFileSync(filePath);

                const uploadHeaders = {
                    ...headers,
                    'Content-Type': 'application/java-archive'
                };

                const uploadResponse = await fetch(uploadAssetUrl, {
                    method: 'POST',
                    headers: uploadHeaders,
                    body: fileBuffer
                });

                const err = await this.validateResponse(uploadResponse);
                if (err) {
                    return {success: false, platform: this.id, message: `Failed uploading ${fileName}: ${err}`};
                }
            }

            return {success: true, platform: this.id};
        } catch (e: any) {
            return {success: false, platform: this.id, message: e.message || String(e)};
        }
    }

    async getModInfo(modid: string, token: string): Promise<{ modid: string; slug: string; title: string } | null> {
        // GitHub release target doesn't require looking up mod info
        return null;
    }
}
