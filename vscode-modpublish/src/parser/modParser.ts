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

import * as yauzl from 'yauzl';
import {parseToml} from './toml';

export enum SideType {
    BOTH = 'BOTH',
    CLIENT = 'CLIENT',
    SERVER = 'SERVER'
}

export enum PublishType {
    Fabric = 'Fabric',
    Quilt = 'Quilt',
    Forge = 'Forge',
    NeoForge = 'NeoForge',
    Rift = 'Rift',
    LiteLoader = 'LiteLoader',
    JavaAgent = 'JavaAgent'
}

export interface LocalModInfo {
    id?: string;
    name: string;
    version: string;
    versionRange: string;
    sideType: SideType;
    publishTypes: PublishType[];
}

export class ModParser {
    /**
     * Scans a JAR/Zip file to find Minecraft mod metadata files.
     * Returns the detected publish types and their corresponding file contents.
     */
    public static async parse(filePath: string): Promise<LocalModInfo | null> {
        try {
            const entries = await this.listEntries(filePath);

            const hasManifest = entries.includes('META-INF/MANIFEST.MF');
            const matchedTypes: { type: PublishType; file: string }[] = [];

            if (entries.includes('fabric.mod.json')) {
                matchedTypes.push({type: PublishType.Fabric, file: 'fabric.mod.json'});
            }
            if (entries.includes('quilt.mod.json')) {
                matchedTypes.push({type: PublishType.Quilt, file: 'quilt.mod.json'});
            }
            if (entries.includes('META-INF/neoforge.mods.toml')) {
                matchedTypes.push({type: PublishType.NeoForge, file: 'META-INF/neoforge.mods.toml'});
            }
            if (entries.includes('META-INF/mods.toml')) {
                matchedTypes.push({type: PublishType.Forge, file: 'META-INF/mods.toml'});
            }
            if (entries.includes('riftmod.json')) {
                matchedTypes.push({type: PublishType.Rift, file: 'riftmod.json'});
            }
            if (entries.includes('litemod.json')) {
                matchedTypes.push({type: PublishType.LiteLoader, file: 'litemod.json'});
            }

            if (matchedTypes.length === 0 && hasManifest) {
                const manifestContent = await this.readEntryContent(filePath, 'META-INF/MANIFEST.MF');
                if (manifestContent && (manifestContent.includes('Premain-Class') || manifestContent.includes('Agent-Class'))) {
                    const filename = filePath.split(/[/\\]/).pop() || 'JavaAgent';
                    const name = filename.replace(/\.jar$/, '');
                    return {
                        id: 'java-agent',
                        name: name,
                        version: '1.0.0',
                        versionRange: '1.0.0',
                        sideType: SideType.BOTH,
                        publishTypes: [PublishType.JavaAgent]
                    };
                }
            }

            if (matchedTypes.length === 0) {
                if (entries.includes('mcmod.info')) {
                    const content = await this.readEntryContent(filePath, 'mcmod.info');
                    if (content) {
                        try {
                            const json = JSON.parse(content);
                            const obj = Array.isArray(json) ? json[0] : (json.modList ? json.modList[0] : json);
                            const side = obj.serverSideOnly ? SideType.SERVER : (obj.clientSideOnly ? SideType.CLIENT : SideType.BOTH);
                            return {
                                id: obj.modid || '',
                                name: obj.name || '',
                                version: obj.version || '',
                                versionRange: obj.mcversion || '',
                                sideType: side,
                                publishTypes: [PublishType.Forge]
                            };
                        } catch (e) {
                            console.error('Failed to parse mcmod.info', e);
                        }
                    }
                }
                return null;
            }

            const primaryMatch = matchedTypes[0];
            const primaryContent = await this.readEntryContent(filePath, primaryMatch.file);
            if (!primaryContent) {
                return null;
            }

            const modInfo = this.parseMetadata(primaryMatch.type, primaryContent);
            if (modInfo) {
                modInfo.publishTypes = matchedTypes.map(t => t.type);
            }
            return modInfo;

        } catch (e) {
            console.error('Failed to parse jar metadata', e);
            return null;
        }
    }

    private static parseMetadata(type: PublishType, content: string): LocalModInfo | null {
        try {
            if (type === PublishType.Fabric || type === PublishType.Quilt) {
                const json = JSON.parse(content);
                const name = json.name || json.id || '';
                const version = json.version || '1.0.0';

                let versionRange = '';
                if (json.depends && json.depends.minecraft) {
                    const mc = json.depends.minecraft;
                    if (typeof mc === 'string') {
                        versionRange = mc;
                    } else if (Array.isArray(mc)) {
                        versionRange = mc.join(' ');
                    }
                }

                let sideType = SideType.BOTH;
                if (json.environment === 'client') {
                    sideType = SideType.CLIENT;
                } else if (json.environment === 'server') {
                    sideType = SideType.SERVER;
                }

                return {
                    id: json.id || '',
                    name,
                    version,
                    versionRange,
                    sideType,
                    publishTypes: []
                };
            }

            if (type === PublishType.Forge || type === PublishType.NeoForge) {
                const toml = parseToml(content);
                const mod = toml.mods && toml.mods[0];
                if (!mod) return null;

                const modId = mod.modId || '';
                const name = mod.displayName || modId || '';
                const version = mod.version || '1.0.0';

                let versionRange = '';
                let sideType = SideType.BOTH;

                if (toml.clientSideOnly === true) {
                    sideType = SideType.CLIENT;
                }

                if (toml.dependencies && toml.dependencies[modId]) {
                    const deps = toml.dependencies[modId];
                    if (Array.isArray(deps)) {
                        const mcDep = deps.find((d: any) => d.modId === 'minecraft');
                        if (mcDep) {
                            versionRange = mcDep.versionRange || '';
                        }

                        const forgeOrMcDep = deps.find((d: any) => d.modId === 'minecraft' || d.modId === 'forge' || d.modId === 'neoforge');
                        if (forgeOrMcDep && forgeOrMcDep.side) {
                            const sideStr = String(forgeOrMcDep.side).toUpperCase();
                            if (sideStr === 'CLIENT') {
                                sideType = SideType.CLIENT;
                            } else if (sideStr === 'SERVER') {
                                sideType = SideType.SERVER;
                            }
                        }
                    }
                }

                return {
                    id: modId,
                    name,
                    version,
                    versionRange,
                    sideType,
                    publishTypes: []
                };
            }

            if (type === PublishType.Rift) {
                const json = JSON.parse(content);
                return {
                    id: json.id || '',
                    name: json.name || json.id || '',
                    version: json.version || '1.0.0',
                    versionRange: '1.13',
                    sideType: SideType.BOTH,
                    publishTypes: []
                };
            }

            if (type === PublishType.LiteLoader) {
                const json = JSON.parse(content);
                return {
                    id: json.name || '',
                    name: json.name || '',
                    version: json.version || '1.0.0',
                    versionRange: json.mcversion || '',
                    sideType: SideType.BOTH,
                    publishTypes: []
                };
            }

            return null;
        } catch (e) {
            console.error(`Failed to parse metadata for ${type}`, e);
            return null;
        }
    }

    private static listEntries(filePath: string): Promise<string[]> {
        return new Promise((resolve, reject) => {
            const entries: string[] = [];
            yauzl.open(filePath, {lazyEntries: true}, (err, zipfile) => {
                if (err) return reject(err);

                zipfile.on('entry', (entry) => {
                    entries.push(entry.fileName);
                    zipfile.readEntry();
                });

                zipfile.on('end', () => {
                    zipfile.close();
                    resolve(entries);
                });

                zipfile.on('error', (err) => {
                    zipfile.close();
                    reject(err);
                });

                zipfile.readEntry();
            });
        });
    }

    private static readEntryContent(filePath: string, entryName: string): Promise<string | null> {
        return new Promise((resolve, reject) => {
            yauzl.open(filePath, {lazyEntries: true}, (err, zipfile) => {
                if (err) return reject(err);

                let found = false;
                zipfile.on('entry', (entry) => {
                    if (entry.fileName === entryName) {
                        found = true;
                        zipfile.openReadStream(entry, (err, readStream) => {
                            if (err) {
                                zipfile.close();
                                return reject(err);
                            }
                            const chunks: Buffer[] = [];
                            readStream.on('data', (chunk) => chunks.push(chunk));
                            readStream.on('end', () => {
                                zipfile.close();
                                resolve(Buffer.concat(chunks).toString('utf8'));
                            });
                            readStream.on('error', (err) => {
                                zipfile.close();
                                reject(err);
                            });
                        });
                    } else {
                        zipfile.readEntry();
                    }
                });

                zipfile.on('end', () => {
                    if (!found) {
                        zipfile.close();
                        resolve(null);
                    }
                });

                zipfile.on('error', (err) => {
                    zipfile.close();
                    reject(err);
                });

                zipfile.readEntry();
            });
        });
    }
}
