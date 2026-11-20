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

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet

object MarkdownConverter {
    private val options = MutableDataSet()
    private val parser: Parser = Parser.builder(options).build()
    private val renderer: HtmlRenderer = HtmlRenderer.builder(options).build()

    /**
     * Converts a Markdown string to HTML.
     *
     * Double newlines in the input are normalised to single newlines before
     * parsing so that CurseForge receives clean, compact HTML.
     */
    fun markdownToHtml(markdown: String): String {
        val normalised = markdown.replace("\r\n", "\n").replace("\n\n", "\n")
        val document = parser.parse(normalised)
        return renderer.render(document).trimEnd()
    }
}

/** Convenience extension – converts this Markdown string to HTML. */
fun String.markdownToHtml(): String = MarkdownConverter.markdownToHtml(this)
