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

import {AxiosRequestConfig, AxiosResponse} from 'axios';

export interface DependencyInfo {
    projectId: string;
    type: 'required' | 'optional' | 'embedded' | 'incompatible';
    customTitle?: string;
    modrinthModInfo?: { modid: string; slug: string; title: string };
    curseforgeModInfo?: { modid: string; slug: string; title: string };
}

export interface PublishData {
    versionName: string;
    versionNumber: string;
    releaseChannel: 'release' | 'beta' | 'alpha';
    loaders: string[];
    clientRequired: boolean;
    serverRequired: boolean;
    minecraftVersions: string[];
    changelog: string;
    dependencies: DependencyInfo[];
    files: string[];
}

export interface PublishResult {
    success: boolean;
    platform: string;
    message?: string;
}

export abstract class API {
    abstract readonly id: string;

    protected getBaseConfig(token: string): AxiosRequestConfig {
        return {
            headers: {
                'User-Agent': 'modpublish-vsc/v1 (github.com/404Setup/ModPublish)',
                'Authorization': token
            }
        };
    }

    protected validateResponse(response: AxiosResponse): string | null {
        const code = response.status;
        if (code >= 200 && code < 300) {
            return null;
        }
        if (code === 403) {
            return 'api.common.err.403';
        }
        if (code === 404) {
            return 'api.common.err.404';
        }
        if (code === 500) {
            return 'api.common.err.500';
        }
        return `HTTP ${code}: ${JSON.stringify(response.data)}`;
    }

    abstract publish(data: PublishData, tokens: Record<string, string>, config: Record<string, any>): Promise<PublishResult>;

    /**
     * Gets mod details by its ID or slug from the remote platform (used when adding a dependency to look up its info).
     */
    abstract getModInfo(modid: string, token: string): Promise<{ modid: string; slug: string; title: string } | null>;
}
