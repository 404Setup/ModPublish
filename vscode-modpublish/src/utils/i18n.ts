import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';

export class Lang {
    private static currentLocaleData: Record<string, string> = {};
    private static fallbackLocaleData: Record<string, string> = {};
    private static isInitialized = false;

    public static initialize(context: vscode.ExtensionContext) {
        if (this.isInitialized) {
            return;
        }

        const localeDir = path.join(context.extensionPath, 'locales');

        try {
            const fallbackPath = path.join(localeDir, 'en.json');
            if (fs.existsSync(fallbackPath)) {
                this.fallbackLocaleData = JSON.parse(fs.readFileSync(fallbackPath, 'utf8'));
            }
        } catch (e) {
            console.error('Failed to load fallback locale', e);
        }

        const vsCodeLanguage = vscode.env.language.toLowerCase();
        try {
            let targetPath = path.join(localeDir, `${vsCodeLanguage}.json`);
            if (!fs.existsSync(targetPath)) {
                if (vsCodeLanguage.startsWith('zh')) {
                    if (vsCodeLanguage.includes('hk')) {
                        targetPath = path.join(localeDir, 'zh-hk.json');
                    } else if (vsCodeLanguage.includes('tw') || vsCodeLanguage.includes('hant')) {
                        targetPath = path.join(localeDir, 'zh-tw.json');
                    } else {
                        targetPath = path.join(localeDir, 'zh-cn.json');
                    }
                }
            }

            if (fs.existsSync(targetPath)) {
                this.currentLocaleData = JSON.parse(fs.readFileSync(targetPath, 'utf8'));
            } else {
                this.currentLocaleData = this.fallbackLocaleData;
            }
        } catch (e) {
            console.error(`Failed to load target locale: ${vsCodeLanguage}`, e);
            this.currentLocaleData = this.fallbackLocaleData;
        }

        this.isInitialized = true;
    }

    /**
     * Translates a key with optional string formatting arguments.
     */
    public static get(key: string, ...args: string[]): string {
        let value = this.currentLocaleData[key] || this.fallbackLocaleData[key] || key;

        if (args && args.length > 0) {
            for (let i = 0; i < args.length; i++) {
                value = value.replace(`{${i}}`, args[i]);
            }
        }

        return value;
    }
}
