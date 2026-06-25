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
