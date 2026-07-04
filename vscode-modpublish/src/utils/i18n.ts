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
        this.fallbackLocaleData = this.readLocaleFile(localeDir, 'en.json') || {};
        this.currentLocaleData = this.readLocaleFile(localeDir, this.resolveLocaleFile(vscode.env.language)) || this.fallbackLocaleData;

        this.isInitialized = true;
    }

    /**
     * Resolves the locale file name for a VS Code language identifier.
     * The identifier is sanitized so it can never escape the locales directory.
     */
    public static resolveLocaleFile(language: string): string {
        const lang = (language || '').toLowerCase().replace(/[^a-z0-9-]/g, '');
        if (lang.startsWith('zh')) {
            if (lang.includes('hk')) return 'zh-hk.json';
            if (lang.includes('tw') || lang.includes('hant')) return 'zh-tw.json';
            return 'zh-cn.json';
        }
        return `${lang || 'en'}.json`;
    }

    /**
     * Reads and parses a single locale file, returning null when missing or invalid.
     */
    private static readLocaleFile(localeDir: string, fileName: string): Record<string, string> | null {
        try {
            const filePath = path.join(localeDir, fileName);
            if (fs.existsSync(filePath)) {
                return JSON.parse(fs.readFileSync(filePath, 'utf8'));
            }
        } catch (e) {
            console.error(`Failed to load locale file: ${fileName}`, e);
        }
        return null;
    }

    /**
     * Returns the English data merged with the current UI language data (used by the webview).
     */
    public static loadMergedLocaleData(context: vscode.ExtensionContext): Record<string, string> {
        const localeDir = path.join(context.extensionPath, 'locales');
        const defaultData = this.readLocaleFile(localeDir, 'en.json') || {};
        const targetData = this.readLocaleFile(localeDir, this.resolveLocaleFile(vscode.env.language)) || {};
        return {...defaultData, ...targetData};
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
