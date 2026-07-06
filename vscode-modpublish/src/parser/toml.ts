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

/**
 * A lightweight, simple TOML parser designed specifically to extract
 * mod metadata from mods.toml (Forge/NeoForge) and fabric.mod.json / quilt.mod.json dependencies.
 */
export function parseToml(content: string): any {
    const result: any = {mods: []};
    const lines = content.split(/\r?\n/);
    let currentTable: any = result;
    let isUnderMods = false;

    for (let line of lines) {
        line = line.trim();
        if (!line || line.startsWith('#')) {
            continue;
        }

        if (line.startsWith('[[') && line.endsWith(']]')) {
            const tableName = line.slice(2, -2).trim();
            if (tableName === 'mods') {
                isUnderMods = true;
                const newMod = {};
                result.mods.push(newMod);
                currentTable = newMod;
            } else if (tableName.startsWith('dependencies.')) {
                isUnderMods = false;
                const modId = tableName.substring('dependencies.'.length).trim();
                if (!result.dependencies) {
                    result.dependencies = {};
                }
                if (!result.dependencies[modId]) {
                    result.dependencies[modId] = [];
                }
                const newDep = {};
                result.dependencies[modId].push(newDep);
                currentTable = newDep;
            } else {
                isUnderMods = false;
                currentTable = result;
            }
            continue;
        } else if (line.startsWith('[') && line.endsWith(']')) {
            isUnderMods = false;
            currentTable = result;
            continue;
        }

        const eqIdx = line.indexOf('=');
        if (eqIdx !== -1) {
            const key = line.substring(0, eqIdx).trim();
            let rawVal = line.substring(eqIdx + 1).trim();

            const commentIdx = rawVal.indexOf('#');
            if (commentIdx !== -1) {
                const quoteCountBefore = (rawVal.substring(0, commentIdx).match(/"/g) || []).length;
                if (quoteCountBefore % 2 === 0) {
                    rawVal = rawVal.substring(0, commentIdx).trim();
                }
            }

            let val: any = rawVal;
            if ((rawVal.startsWith('"') && rawVal.endsWith('"')) || (rawVal.startsWith("'") && rawVal.endsWith("'"))) {
                val = rawVal.slice(1, -1);
                val = val.replace(/\\"/g, '"').replace(/\\\\/g, '\\').replace(/\\n/g, '\n');
            } else if (rawVal === 'true') {
                val = true;
            } else if (rawVal === 'false') {
                val = false;
            } else if (!isNaN(Number(rawVal))) {
                val = Number(rawVal);
            }

            if (currentTable) {
                currentTable[key] = val;
            }
        }
    }

    return result;
}
