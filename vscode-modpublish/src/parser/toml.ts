/**
 * A lightweight, simple TOML parser designed specifically to extract
 * mod metadata from mods.toml (Forge/NeoForge) and fabric.mod.json / quilt.mod.json dependencies.
 */
export function parseToml(content: string): any {
    const result: any = {mods: []};
    const lines = content.split(/\r?\n/);
    let currentMod: any = null;
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
                currentMod = {};
                result.mods.push(currentMod);
            } else {
                isUnderMods = false;
            }
            continue;
        } else if (line.startsWith('[') && line.endsWith(']')) {
            isUnderMods = false;
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

            if (isUnderMods && currentMod) {
                currentMod[key] = val;
            } else if (!isUnderMods) {
                result[key] = val;
            }
        }
    }

    return result;
}
