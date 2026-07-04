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

import {execFile} from 'child_process';

export interface LocalGitInfo {
    branch: string;
    repoPath: string;
    host: string;
}

export class GitUtil {
    private static runGit(args: string[], cwd: string): Promise<string> {
        return new Promise((resolve) => {
            execFile('git', args, {cwd}, (error, stdout) => {
                if (error) {
                    resolve('');
                } else {
                    resolve(stdout.trim());
                }
            });
        });
    }

    public static async getGitInfo(workspacePath: string): Promise<LocalGitInfo | null> {
        try {
            const isGit = await this.runGit(['rev-parse', '--is-inside-work-tree'], workspacePath);
            if (isGit !== 'true') {
                return null;
            }

            const branchRaw = await this.runGit(['rev-parse', '--abbrev-ref', 'HEAD'], workspacePath);
            const branch = branchRaw.replace(/[\r\n]/g, '').trim();
            const remoteUrlRaw = await this.runGit(['config', '--get', 'remote.origin.url'], workspacePath);
            const remoteUrl = remoteUrlRaw.replace(/[\r\n]/g, '').trim();

            let repoPath = '';
            let host = '';

            if (remoteUrl) {
                let cleanUrl = remoteUrl;
                if (cleanUrl.startsWith('ssh://')) {
                    cleanUrl = cleanUrl.substring(6);
                } else if (cleanUrl.startsWith('https://')) {
                    cleanUrl = cleanUrl.substring(8);
                } else if (cleanUrl.startsWith('http://')) {
                    cleanUrl = cleanUrl.substring(7);
                }

                if (cleanUrl.startsWith('git@')) {
                    cleanUrl = cleanUrl.substring(4);
                }

                const firstSepIndex = cleanUrl.search(/[:/]/);
                if (firstSepIndex !== -1) {
                    host = cleanUrl.substring(0, firstSepIndex);
                    let rawPath = cleanUrl.substring(firstSepIndex + 1);
                    if (rawPath.endsWith('.git')) {
                        rawPath = rawPath.substring(0, rawPath.length - 4);
                    }
                    repoPath = rawPath;
                }
            }

            return {
                branch,
                repoPath,
                host
            };
        } catch (e) {
            console.error('Failed to get local git info', e);
            return null;
        }
    }
}
