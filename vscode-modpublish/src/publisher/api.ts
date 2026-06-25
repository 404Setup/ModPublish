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
                'User-Agent': 'modpublish/v1 (github.com/404Setup/ModPublish)',
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
