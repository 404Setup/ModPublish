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

export const USER_AGENT = 'modpublish-vsc/v1 (github.com/404Setup/ModPublish)';

export type DependencyType = 'required' | 'optional' | 'embedded' | 'incompatible';

export interface ModRef {
    modid: string;
    slug: string;
    title: string;
}

export interface DependencyInfo {
    projectId: string;
    type: DependencyType;
    customTitle?: string;
    modrinthModInfo?: ModRef;
    curseforgeModInfo?: ModRef;
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
    environment?: string;
    files: string[];
}

export interface PublishResult {
    success: boolean;
    platform: string;
    message?: string;
}

export abstract class API {
    abstract readonly id: string;

    /**
     * Builds the common request headers (User-Agent) merged with platform specific headers.
     */
    protected buildHeaders(extra: Record<string, string> = {}): Record<string, string> {
        return {
            'User-Agent': USER_AGENT,
            ...extra
        };
    }

    /**
     * Reads a local file and returns it as a Blob together with its base name.
     */
    protected async readFileBlob(filePath: string): Promise<{ blob: Blob; name: string }> {
        const fileBuf = await fs.promises.readFile(filePath);
        return {blob: new Blob([fileBuf]), name: path.basename(filePath)};
    }

    /**
     * Maps the generic dependency type to a platform specific value, falling back to the 'optional' mapping.
     */
    protected mapDependencyType(type: DependencyType, mapping: Record<DependencyType, string>): string {
        return mapping[type] || mapping['optional'];
    }

    /**
     * Derives a git tag name from a version number (ensures the 'v' prefix).
     */
    protected deriveTagName(versionNumber: string): string {
        return versionNumber.startsWith('v') ? versionNumber : `v${versionNumber}`;
    }

    protected success(): PublishResult {
        return {success: true, platform: this.id};
    }

    protected failure(message: string): PublishResult {
        return {success: false, platform: this.id, message};
    }

    /**
     * Runs the platform specific publish logic, converting thrown errors into a failed PublishResult.
     */
    protected async runPublish(fn: () => Promise<PublishResult>): Promise<PublishResult> {
        try {
            return await fn();
        } catch (e: any) {
            return this.failure(e?.message || String(e));
        }
    }

    protected async validateResponse(response: Response): Promise<string | null> {
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

        let text = await response.text().catch(() => '');
        return `HTTP ${code}: ${text}`;
    }

    abstract publish(data: PublishData, tokens: Record<string, string>, config: Record<string, any>): Promise<PublishResult>;

    /**
     * Gets mod details by its ID or slug from the remote platform (used when adding a dependency to look up its info).
     */
    abstract getModInfo(modid: string, token: string): Promise<ModRef | null>;
}
