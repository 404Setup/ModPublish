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

function escapeHtml(text: string): string {
    return text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

function processInlineMd(text: string): string {
    const inlineCodes: string[] = [];
    text = text.replace(/`([^`]+)`/g, (_, code) => {
        inlineCodes.push(`<code>${escapeHtml(code)}</code>`);
        return `\x00IC${inlineCodes.length - 1}\x00`;
    });

    text = escapeHtml(text);

    text = text
        .replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1">')
        .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2">$1</a>')
        .replace(/\*\*\*(.+?)\*\*\*/g, '<strong><em>$1</em></strong>')
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/__(.+?)__/g, '<strong>$1</strong>')
        .replace(/\*(.+?)\*/g, '<em>$1</em>')
        .replace(/_(.+?)_/g, '<em>$1</em>')
        .replace(/~~(.+?)~~/g, '<del>$1</del>');

    return text.replace(/\x00IC(\d+)\x00/g, (_, idx) => inlineCodes[+idx]);
}

function processTableMd(lines: string[]): string {
    const parseRow = (line: string): string[] =>
        line.replace(/^\||\|$/g, '').split('|').map(c => c.trim());

    const headers = parseRow(lines[0]);
    const alignments = parseRow(lines[1]).map(cell => {
        if (/^:-+:$/.test(cell)) {
            return 'center';
        }
        if (/^-+:$/.test(cell)) {
            return 'right';
        }
        return 'left';
    });

    const headerHtml = headers.map((h, idx) =>
        `<th style="text-align:${alignments[idx] ?? 'left'}">${processInlineMd(h)}</th>`
    ).join('');

    const rowsHtml = lines.slice(2).map(line =>
        `<tr>${parseRow(line).map((cell, idx) =>
            `<td style="text-align:${alignments[idx] ?? 'left'}">${processInlineMd(cell)}</td>`
        ).join('')}</tr>`
    ).join('');

    return `<table><thead><tr>${headerHtml}</tr></thead><tbody>${rowsHtml}</tbody></table>`;
}

function processListMd(lines: string[]): string {
    const isOrdered = /^[ \t]*\d+\./.test(lines[0]);
    const tag = isOrdered ? 'ol' : 'ul';
    const items: string[] = [];
    let current = '';

    for (const line of lines) {
        const m = line.match(/^[ \t]*(?:[-*+]|\d+\.)\s+(.*)/);
        if (m) {
            if (current) {
                items.push(current);
            }
            const content = m[1]
                .replace(/^\[ \]\s/, '<input type="checkbox" disabled> ')
                .replace(/^\[x\]\s/i, '<input type="checkbox" checked disabled> ');
            current = processInlineMd(content);
        } else if (/^[ \t]+\S/.test(line) && current) {
            current += ' ' + processInlineMd(line.trim());
        }
    }
    if (current) {
        items.push(current);
    }

    return `<${tag}>${items.map(item => `<li>${item}</li>`).join('')}</${tag}>`;
}

export function markdownToHtml(markdown: string): string {
    let text = markdown.replace(/\r\n/g, '\n').replace(/\r/g, '\n');

    const codeBlocks: string[] = [];
    text = text.replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => {
        const escaped = escapeHtml(code.replace(/\n$/, ''));
        const langAttr = lang ? ` class="language-${lang}"` : '';
        codeBlocks.push(`<pre><code${langAttr}>${escaped}</code></pre>`);
        return `\x00CB${codeBlocks.length - 1}\x00`;
    });

    const lines = text.split('\n');
    const result: string[] = [];
    let i = 0;

    while (i < lines.length) {
        const line = lines[i];

        if (/\x00CB\d+\x00/.test(line)) {
            result.push(line.replace(/\x00CB(\d+)\x00/g, (_, idx) => codeBlocks[+idx]));
            i++;
            continue;
        }

        if (/^[ \t]*([-*_])(\s*\1){2,}\s*$/.test(line)) {
            result.push('<hr>');
            i++;
            continue;
        }

        const headingMatch = line.match(/^(#{1,6})\s+(.*)/);
        if (headingMatch) {
            const level = headingMatch[1].length;
            result.push(`<h${level}>${processInlineMd(headingMatch[2])}</h${level}>`);
            i++;
            continue;
        }

        if (/^>\s?/.test(line)) {
            const quoteLines: string[] = [];
            while (i < lines.length && /^>\s?/.test(lines[i])) {
                quoteLines.push(lines[i].replace(/^>\s?/, ''));
                i++;
            }
            const innerHtml = quoteLines.map(l => processInlineMd(l)).join('<br>');
            result.push(`<blockquote><p>${innerHtml}</p></blockquote>`);
            continue;
        }

        if (/^\|.+\|/.test(line) && i + 1 < lines.length && /^\|[\s\-:|]+\|/.test(lines[i + 1])) {
            const tableLines: string[] = [];
            while (i < lines.length && /^\|.*\|/.test(lines[i])) {
                tableLines.push(lines[i]);
                i++;
            }
            result.push(processTableMd(tableLines));
            continue;
        }

        if (/^[ \t]*[-*+]\s/.test(line) || /^[ \t]*\d+\.\s/.test(line)) {
            const listLines: string[] = [];
            while (i < lines.length) {
                const l = lines[i];
                if (/^[ \t]*[-*+]\s/.test(l) || /^[ \t]*\d+\.\s/.test(l) ||
                    (/^[ \t]+\S/.test(l) && listLines.length > 0)) {
                    listLines.push(l);
                    i++;
                } else {
                    break;
                }
            }
            result.push(processListMd(listLines));
            continue;
        }

        if (line.trim() === '') {
            result.push('');
            i++;
            continue;
        }

        const paraLines: string[] = [];
        while (i < lines.length) {
            const l = lines[i];
            if (l.trim() === '' ||
                /^#{1,6}\s/.test(l) ||
                /^[ \t]*[-*+]\s/.test(l) ||
                /^[ \t]*\d+\.\s/.test(l) ||
                /^[ \t]*([-*_])(\s*\1){2,}\s*$/.test(l) ||
                /^>\s?/.test(l) ||
                /^\|.*\|/.test(l) ||
                /\x00CB\d+\x00/.test(l)) {
                break;
            }
            paraLines.push(l);
            i++;
        }
        if (paraLines.length > 0) {
            const html = paraLines.map(l => processInlineMd(l)).join('<br>');
            result.push(`<p>${html}</p>`);
        }
    }

    return result.filter(l => l !== '').join('\n');
}
