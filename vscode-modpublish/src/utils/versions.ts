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
import * as vscode from 'vscode';
import axios from 'axios';

export interface MinecraftVersionItem {
    v: string;
    t: string;
    i: number;
    d: string;
}

export class VersionUtil {
    private static cachedVersions: MinecraftVersionItem[] = [];

    public static getVersions(context: vscode.ExtensionContext): MinecraftVersionItem[] {
        if (this.cachedVersions.length > 0) {
            return this.cachedVersions;
        }

        const cachePath = path.join(context.globalStorageUri.fsPath, 'minecraft.version.json');
        if (fs.existsSync(cachePath)) {
            try {
                const data = fs.readFileSync(cachePath, 'utf8');
                this.cachedVersions = JSON.parse(data);
                return this.cachedVersions;
            } catch (e) {
                console.error('Failed to read version cache, fallback to bundle', e);
            }
        }

        const bundlePath = path.join(context.extensionPath, 'resources', 'minecraft.version.json');
        try {
            if (fs.existsSync(bundlePath)) {
                const data = fs.readFileSync(bundlePath, 'utf8');
                this.cachedVersions = JSON.parse(data);
            }
        } catch (e) {
            console.error('Failed to read bundle minecraft version list', e);
        }

        return this.cachedVersions;
    }

    public static clearCache(context: vscode.ExtensionContext): boolean {
        const cachePath = path.join(context.globalStorageUri.fsPath, 'minecraft.version.json');
        if (fs.existsSync(cachePath)) {
            try {
                fs.unlinkSync(cachePath);
                this.cachedVersions = [];
                return true;
            } catch (e) {
                console.error('Failed to delete cache file', e);
            }
        }
        return false;
    }

    public static async updateVersions(context: vscode.ExtensionContext): Promise<boolean> {
        try {
            const mojangRes = await axios.get('https://launchermeta.mojang.com/mc/game/version_manifest_v2.json');
            if (mojangRes.status !== 200 || !mojangRes.data || !mojangRes.data.versions) {
                return false;
            }

            const headers: Record<string, string> = {
                'User-Agent': 'modpublish-vsc/v1 (github.com/404Setup/ModPublish)'
            };

            const cfRes = await axios.get('https://api.curseforge.com/v1/minecraft/version', {
                headers,
                validateStatus: () => true
            });
            if (cfRes.status !== 200 || !cfRes.data || !cfRes.data.data) {
                return false;
            }

            const manifest = mojangRes.data;
            const cfVersions = cfRes.data.data;

            const versionMapping = new Map<string, number>();
            for (const v of cfVersions) {
                if (v.versionString && v.gameVersionId !== undefined) {
                    versionMapping.set(v.versionString, v.gameVersionId);
                }
            }

            const merged: MinecraftVersionItem[] = manifest.versions.map((version: any) => {
                let releaseTime = version.releaseTime || '';
                if (releaseTime.endsWith('+00:00')) {
                    releaseTime = releaseTime.substring(0, releaseTime.length - 6) + 'Z';
                }
                const gameVersionId = versionMapping.get(version.id) ?? -1;
                return {
                    v: version.id,
                    t: version.type,
                    i: gameVersionId,
                    d: releaseTime
                };
            });

            const cacheDir = context.globalStorageUri.fsPath;
            if (!fs.existsSync(cacheDir)) {
                fs.mkdirSync(cacheDir, {recursive: true});
            }
            const cachePath = path.join(cacheDir, 'minecraft.version.json');
            fs.writeFileSync(cachePath, JSON.stringify(merged, null, 2), 'utf8');
            this.cachedVersions = merged;
            return true;
        } catch (e) {
            console.error('Failed to update Minecraft version list', e);
            return false;
        }
    }
}

export enum VersionType {
    RELEASE = 'RELEASE',
    PRE_RELEASE = 'PRE_RELEASE',
    RELEASE_CANDIDATE = 'RELEASE_CANDIDATE',
    BETA = 'BETA',
    SNAPSHOT = 'SNAPSHOT',
    UNKNOWN = 'UNKNOWN'
}

export class Version {
    original: string;
    type: VersionType;
    major: number;
    minor: number;
    patch: number;
    preRelease: string | null;

    constructor(original: string) {
        this.original = original;
        const trimmed = original.trim();
        let maj = 0, min = 0, pat = 0;
        let pre: string | null = null;
        let verType = VersionType.UNKNOWN;

        const RELEASE_PATTERN = /^(\d+)\.(\d+)\.(\d+)$/;
        const SHORT_RELEASE_PATTERN = /^(\d+)\.(\d+)$/;
        const PRE_RELEASE_PATTERN = /^(\d+)\.(\d+)\.(\d+)-(pre|rc)(\d+)$/;
        const BETA_PATTERN = /^b(\d+)\.(\d+)\.(\d+)$/;
        const SNAPSHOT_PATTERN = /^(\d{2})w(\d{2})([a-z])(?:_or_([a-z]))?$/;

        let matcher: RegExpMatchArray | null;
        if ((matcher = trimmed.match(RELEASE_PATTERN))) {
            maj = parseInt(matcher[1], 10);
            min = parseInt(matcher[2], 10);
            pat = parseInt(matcher[3], 10);
            verType = VersionType.RELEASE;
        } else if ((matcher = trimmed.match(SHORT_RELEASE_PATTERN))) {
            maj = parseInt(matcher[1], 10);
            min = parseInt(matcher[2], 10);
            pat = 0;
            verType = VersionType.RELEASE;
        } else if ((matcher = trimmed.match(PRE_RELEASE_PATTERN))) {
            maj = parseInt(matcher[1], 10);
            min = parseInt(matcher[2], 10);
            pat = parseInt(matcher[3], 10);
            const t = matcher[4];
            const n = matcher[5];
            pre = t + n;
            verType = t === 'rc' ? VersionType.RELEASE_CANDIDATE : VersionType.PRE_RELEASE;
        } else if ((matcher = trimmed.match(BETA_PATTERN))) {
            maj = parseInt(matcher[1], 10);
            min = parseInt(matcher[2], 10);
            pat = parseInt(matcher[3], 10);
            verType = VersionType.BETA;
        } else if ((matcher = trimmed.match(SNAPSHOT_PATTERN))) {
            maj = 2000 + parseInt(matcher[1], 10);
            min = parseInt(matcher[2], 10);
            pat = matcher[3].charCodeAt(0) - 'a'.charCodeAt(0);
            verType = VersionType.SNAPSHOT;
        } else {
            let end = trimmed.length;
            while (end > 0 && trimmed[end - 1] === '.') {
                end--;
            }
            const sliced = trimmed.substring(0, end);
            const parts = sliced.split('.');

            const getDigits = (str: string): string => {
                let i = 0;
                while (i < str.length && /\d/.test(str[i])) {
                    i++;
                }
                return i === str.length ? str : str.substring(0, i);
            };

            const part1 = parts[0] || '';
            const part2 = parts[1] || '';
            const part3 = parts[2] || '';

            maj = parseInt(getDigits(part1) || '0', 10);
            min = parseInt(getDigits(part2) || '0', 10);
            pat = parseInt(getDigits(part3) || '0', 10);

            const hasAtLeastTwoParts = parts.length >= 2;
            const allPartsDigits = parts.every(p => /^\d+$/.test(p));

            verType = (hasAtLeastTwoParts && allPartsDigits) ? VersionType.RELEASE : VersionType.UNKNOWN;
        }

        this.major = maj;
        this.minor = min;
        this.patch = pat;
        this.preRelease = pre;
        this.type = verType;
    }

    typePriority(): number {
        switch (this.type) {
            case VersionType.BETA:
                return 1;
            case VersionType.SNAPSHOT:
                return 2;
            case VersionType.PRE_RELEASE:
                return 3;
            case VersionType.RELEASE_CANDIDATE:
                return 4;
            case VersionType.RELEASE:
                return 5;
            default:
                return 0;
        }
    }

    compareTo(other: Version): number {
        if (this.major !== other.major) return this.major - other.major;
        if (this.minor !== other.minor) return this.minor - other.minor;
        if (this.patch !== other.patch) return this.patch - other.patch;

        const thisPri = this.typePriority();
        const otherPri = other.typePriority();
        if (thisPri !== otherPri) return thisPri - otherPri;

        const thisPre = this.preRelease || '';
        const otherPre = other.preRelease || '';
        return thisPre.localeCompare(otherPre);
    }
}

export interface VersionConstraint {
    satisfies(version: Version): boolean;
}

class ExactConstraint implements VersionConstraint {
    constructor(private val: string) {
    }

    satisfies(version: Version): boolean {
        return version.original.trim() === this.val.trim();
    }
}

class RangeConstraint implements VersionConstraint {
    constructor(
        private minVer: Version | null,
        private maxVer: Version | null,
        private includeMin: boolean,
        private includeMax: boolean
    ) {
    }

    satisfies(version: Version): boolean {
        if (this.minVer) {
            const cmp = version.compareTo(this.minVer);
            if ((this.includeMin && cmp < 0) || (!this.includeMin && cmp <= 0)) {
                return false;
            }
        }
        if (this.maxVer) {
            const cmp = version.compareTo(this.maxVer);
            if ((this.includeMax && cmp > 0) || (!this.includeMax && cmp >= 0)) {
                return false;
            }
        }
        return true;
    }
}

class TildeConstraint implements VersionConstraint {
    private readonly baseVersion: Version;

    constructor(versionStr: string) {
        this.baseVersion = new Version(versionStr.startsWith('~') ? versionStr.substring(1) : versionStr);
    }

    satisfies(version: Version): boolean {
        if (version.compareTo(this.baseVersion) < 0) return false;
        const upperBound = new Version(`${this.baseVersion.major}.${this.baseVersion.minor + 1}.0`);
        return version.compareTo(upperBound) < 0;
    }
}

class CaretConstraint implements VersionConstraint {
    private readonly baseVersion: Version;

    constructor(versionStr: string) {
        this.baseVersion = new Version(versionStr.startsWith('^') ? versionStr.substring(1) : versionStr);
    }

    satisfies(version: Version): boolean {
        if (version.compareTo(this.baseVersion) < 0) return false;
        const upperBound = new Version(`${this.baseVersion.major + 1}.0.0`);
        return version.compareTo(upperBound) < 0;
    }
}

class CompositeConstraint implements VersionConstraint {
    constructor(private constraints: VersionConstraint[]) {
    }

    satisfies(version: Version): boolean {
        return this.constraints.every(c => c.satisfies(version));
    }
}

class OrConstraint implements VersionConstraint {
    constructor(private constraints: VersionConstraint[]) {
    }

    satisfies(version: Version): boolean {
        return this.constraints.some(c => c.satisfies(version));
    }
}

export class VersionConstraintParser {
    public static parse(constraintStr: string): VersionConstraint {
        const trimmed = constraintStr.trim();
        if (!trimmed) {
            throw new Error('Version constraint cannot be empty');
        }

        const normalized = trimmed.replace(/([><=])\s+/g, '$1');

        const spaceParts = normalized.split(/\s+|&&/);
        if (spaceParts.length > 1) {
            const subConstraints = spaceParts.filter(p => p.trim()).map(p => this.parseSingle(p));
            return new CompositeConstraint(subConstraints);
        }

        return this.parseSingle(normalized);
    }

    private static parseSingle(normalized: string): VersionConstraint {
        if (/^=\d/.test(normalized)) {
            return new ExactConstraint(normalized.substring(1));
        }

        const rangeMatch = normalized.match(/^([\w.-]+)-([\w.-]+)$/);
        if (rangeMatch) {
            return new RangeConstraint(new Version(rangeMatch[1]), new Version(rangeMatch[2]), true, true);
        }

        if (normalized.startsWith('~')) {
            return new TildeConstraint(normalized);
        }

        if (normalized.startsWith('^')) {
            return new CaretConstraint(normalized);
        }

        const compMatch = normalized.match(/^(>=|<=|>|<)([\w.-]+)$/);
        if (compMatch) {
            const op = compMatch[1];
            const verStr = compMatch[2];
            const ver = new Version(verStr);
            if (op === '>=') return new RangeConstraint(ver, null, true, false);
            if (op === '<=') return new RangeConstraint(null, ver, false, true);
            if (op === '>') return new RangeConstraint(ver, null, false, false);
            if (op === '<') return new RangeConstraint(null, ver, false, false);
        }

        if (/^[\[(].+?[\])]$/.test(normalized)) {
            const includeMin = normalized.startsWith('[');
            const includeMax = normalized.endsWith(']');
            const content = normalized.substring(1, normalized.length - 1);

            const parts = content.split(',').map(p => p.trim());
            if (parts.length === 1) {
                return new ExactConstraint(parts[0]);
            }
            if (parts.length === 2) {
                const part1 = parts[0];
                const part2 = parts[1];
                const minVer = part1 ? new Version(part1) : null;
                const maxVer = part2 ? new Version(part2) : null;
                return new RangeConstraint(minVer, maxVer, includeMin, includeMax);
            }
            const exacts = parts.filter(p => p).map(p => new ExactConstraint(p));
            return new OrConstraint(exacts);
        }

        if (/^\d/.test(normalized)) {
            return new ExactConstraint(normalized);
        }

        throw new Error(`Unable to parse version constraint: ${normalized}`);
    }
}
