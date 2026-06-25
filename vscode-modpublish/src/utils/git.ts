import {exec} from 'child_process';

export interface LocalGitInfo {
    branch: string;
    repoPath: string;
    host: string;
}

export class GitUtil {
    private static runCommand(cmd: string, cwd: string): Promise<string> {
        return new Promise((resolve) => {
            exec(cmd, {cwd}, (error, stdout) => {
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
            const isGit = await this.runCommand('git rev-parse --is-inside-work-tree', workspacePath);
            if (isGit !== 'true') {
                return null;
            }

            const branchRaw = await this.runCommand('git rev-parse --abbrev-ref HEAD', workspacePath);
            const branch = branchRaw.replace(/[\r\n]/g, '').trim();
            const remoteUrlRaw = await this.runCommand('git config --get remote.origin.url', workspacePath);
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
