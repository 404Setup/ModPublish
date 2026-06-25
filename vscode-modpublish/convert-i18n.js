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

const fs = require('fs');
const path = require('path');

const srcDir = path.join(__dirname, '../src/main/resources/messages');
const destDir = path.join(__dirname, 'locales');

if (!fs.existsSync(destDir)) {
    fs.mkdirSync(destDir, {recursive: true});
}

function parseProperties(content) {
    const lines = content.split(/\r?\n/);
    const result = {};
    let currentKey = null;
    let currentValue = '';

    for (let line of lines) {
        line = line.trim();
        if (!line || line.startsWith('#') || line.startsWith('!')) {
            continue;
        }

        if (currentKey !== null) {
            if (line.endsWith('\\')) {
                currentValue += ' ' + line.slice(0, -1).trim();
            } else {
                currentValue += ' ' + line;
                result[currentKey] = decodeUnicode(currentValue.trim());
                currentKey = null;
                currentValue = '';
            }
            continue;
        }

        const eqIdx = line.indexOf('=');
        if (eqIdx !== -1) {
            const key = line.substring(0, eqIdx).trim();
            let val = line.substring(eqIdx + 1).trim();
            if (val.endsWith('\\')) {
                currentKey = key;
                currentValue = val.slice(0, -1).trim();
            } else {
                result[key] = decodeUnicode(val);
            }
        }
    }
    return result;
}

function decodeUnicode(str) {
    return str.replace(/\\u([a-fA-F0-9]{4})/g, (match, grp) => {
        return String.fromCharCode(parseInt(grp, 16));
    });
}

const files = fs.readdirSync(srcDir);
for (const file of files) {
    if (file.endsWith('.properties')) {
        let localeName = file.replace('ModPublish', '').replace('.properties', '').replace(/^_/, '').toLowerCase();
        if (!localeName) {
            localeName = 'en';
        }
        localeName = localeName.replace('_', '-');

        const content = fs.readFileSync(path.join(srcDir, file), 'utf8');
        const parsed = parseProperties(content);

        const destFile = path.join(destDir, `${localeName}.json`);
        fs.writeFileSync(destFile, JSON.stringify(parsed, null, 2), 'utf8');
        console.log(`Converted ${file} -> locales/${localeName}.json`);
    }
}
