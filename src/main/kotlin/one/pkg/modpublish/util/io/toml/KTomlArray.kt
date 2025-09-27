/*
 * Copyright (C) 2025 404Setup (https://github.com/404Setup)
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

package one.pkg.modpublish.util.io.toml

import one.pkg.modpublish.util.io.JsonParser.toJson
import java.util.*
import java.util.function.Consumer

class KTomlArray internal constructor(elements: List<Map<String, Any?>>) : Iterable<KTomlParser> {
    private val elements: MutableList<KTomlParser> = ArrayList<KTomlParser>(elements.size)

    init {
        for (element in elements) {
            this.elements.add(KTomlParser(element as MutableMap<String, Any?>))
        }
    }

    fun toJson(): String {
        return elements.toJson()
    }

    /**
     * Get the size of this array
     */
    fun size(): Int {
        return elements.size
    }

    val isEmpty: Boolean
        /**
         * Check if this array is empty
         */
        get() = elements.isEmpty()

    /**
     * Get an element as TomlParser by index
     */
    fun get(index: Int): KTomlParser? {
        if (index >= 0 && index < elements.size) {
            return elements[index]
        }
        return null
    }

    /**
     * Get all elements as List of TomlParser
     */
    fun asList(): List<KTomlParser?> {
        return Collections.unmodifiableList(ArrayList(elements))
    }

    /**
     * Get raw elements (for advanced usage)
     */
    fun asMapList(): List<Map<String, Any?>?> {
        val list: MutableList<Map<String, Any?>?> = ArrayList(elements.size)
        for (element in elements) {
            list.add(element.asMap())
        }
        return Collections.unmodifiableList<Map<String, Any?>?>(list)
    }

    override fun toString(): String {
        return "TomlArray{elements=$elements}"
    }

    override fun iterator(): Iterator<KTomlParser> {
        return elements.iterator()
    }

    override fun forEach(action: Consumer<in KTomlParser?>?) {
        elements.forEach(action)
    }

    override fun spliterator(): Spliterator<KTomlParser> {
        return elements.spliterator()
    }
}