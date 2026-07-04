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
            const checkResponse = await axios.get(checkUrl, {headers, validateStatus: () => true});

            let releaseData: any = null;
            if (checkResponse.status === 200) {
                releaseData = checkResponse.data;
                const updateUrl = `${this.baseUrl}projects/${projectPathEncoded}/releases/${tagName}`;
                await axios.put(updateUrl, {description: data.changelog}, {headers, validateStatus: () => true});
            } else if (checkResponse.status === 404) {
                const createUrl = `${this.baseUrl}projects/${projectPathEncoded}/releases`;
                const createBody = {
                    tag_name: tagName,
                    ref: branch,
                    name: data.versionName,
                    description: data.changelog
                };

                const createResponse = await axios.post(createUrl, createBody, {headers, validateStatus: () => true});
                const err = this.validateResponse(createResponse);
                if (err) {
                    return {success: false, platform: this.id, message: `Failed to create release: ${err}`};
                }
                releaseData = createResponse.data;
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
                form.append('file', fs.createReadStream(filePath), {
                    filename: fileName,
                    contentType: 'application/octet-stream'
                });

                const uploadHeaders = {
                    ...headers,
                    ...form.getHeaders()
                };

                const uploadResponse = await axios.post(uploadUrl, form, {
                    headers: uploadHeaders,
                    maxContentLength: Infinity,
                    maxBodyLength: Infinity,
                    validateStatus: () => true
                });

                const uploadErr = this.validateResponse(uploadResponse);
                if (uploadErr) {
                    return {success: false, platform: this.id, message: `Failed uploading ${fileName}: ${uploadErr}`};
                }

                const uploadedUrl = uploadResponse.data.url;
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

                const linkResponse = await axios.post(linkApiUrl, linkBody, {
                    headers: headers,
                    validateStatus: () => true
                });
                const linkErr = this.validateResponse(linkResponse);
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
