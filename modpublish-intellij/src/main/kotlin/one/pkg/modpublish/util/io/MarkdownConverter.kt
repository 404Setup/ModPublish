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
package one.pkg.modpublish.util.io

import com.intellij.openapi.diagnostic.Logger
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet

object MarkdownConverter {
    private val LOG = Logger.getInstance(MarkdownConverter::class.java)
    private val HEADING = Regex("^(#{1,6})\\s+(.+)$")
    private val CODE = Regex("`([^`]+)`")
    private val BOLD = Regex("""\*\*(.+?)\*\*""")
    private val LINK = Regex("""\[([^\]]+)]\(([^)\s]+)\)""")

    private val engine: Pair<Parser, HtmlRenderer>? by lazy {
        runCatching {
            val options = MutableDataSet()
            Parser.builder(options).build() to HtmlRenderer.builder(options).build()
        }.onFailure { LOG.warn("Markdown engine unavailable: ${it.message}") }
            .getOrNull()
    }

    fun markdownToHtml(markdown: String): String {
        if (markdown.isBlank()) return ""
        val normalised = markdown.replace("\r\n", "\n").replace("\n\n", "\n")
        val current = engine
        if (current != null) {
            val converted = runCatching {
                current.second.render(current.first.parse(normalised)).trimEnd()
            }
            converted.getOrNull()?.let { return it }
            LOG.warn("Markdown conversion failed: ${converted.exceptionOrNull()?.message}")
        }
        return fallback(normalised)
    }

    private fun fallback(markdown: String): String = buildString {
        markdown.split('\n').forEach { line ->
            val heading = HEADING.find(line)
            if (heading != null) {
                val level = heading.groupValues[1].length
                append("<h$level>${inline(heading.groupValues[2])}</h$level>")
            } else {
                append(inline(line)).append("<br>")
            }
        }
    }

    private fun inline(raw: String): String {
        val escaped = raw.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;")
        val withCode = CODE.replace(escaped, "<code>$1</code>")
        val withBold = BOLD.replace(withCode, "<strong>$1</strong>")
        return LINK.replace(withBold, "<a href=\"$2\">$1</a>")
    }
}

fun String.markdownToHtml(): String = MarkdownConverter.markdownToHtml(this)
