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
package one.pkg.modpublish.settings.properties

import com.intellij.ide.util.PropertiesComponent
import one.pkg.modpublish.data.internal.Info

@JvmRecord
data class Property(
    val modrinth: ModrinthProperty,
    val curseforge: CurseForgeProperty,
    val github: GithubProperty,
    val common: CommonProperty
) {
    @JvmRecord
    data class CommonProperty(val versionFormat: String) : PropertyBase {
        override fun isEnabled(): Boolean {
            return true
        }

        companion object {
            fun getInstance(properties: PropertiesComponent): CommonProperty {
                return CommonProperty(PID.CommonVersionFormat.get(properties))
            }
        }
    }

    @JvmRecord
    data class CurseForgeProperty(
        val token: Info, val studioToken: Info,
        val modid: String
    ) : PropertyBase {
        override fun isEnabled(): Boolean {
            return !token.data.trim { it <= ' ' }.isEmpty() && !studioToken.data.trim { it <= ' ' }
                .isEmpty() && !modid.trim { it <= ' ' }.isEmpty()
        }

        companion object {
            fun getInstance(properties: PropertiesComponent): CurseForgeProperty {
                return CurseForgeProperty(
                    PID.CurseForgeToken.getProtect(properties),
                    PID.CurseForgeStudioToken.getProtect(properties),
                    PID.CurseForgeModID.get(properties)
                )
            }
        }
    }

    @JvmRecord
    data class GithubProperty(
        val token: Info, val repo: String,
        val branch: String
    ) : PropertyBase {
        override fun isEnabled(): Boolean {
            return !token.data.trim { it <= ' ' }.isEmpty() && !repo.trim { it <= ' ' }.isEmpty()
        }

        companion object {
            fun getInstance(properties: PropertiesComponent): GithubProperty {
                return GithubProperty(
                    PID.GithubToken.getProtect(properties),
                    PID.GithubRepo.get(properties),
                    PID.GithubBranch.get(properties)
                )
            }
        }
    }

    @JvmRecord
    data class ModrinthProperty(val token: Info, val modid: String) : PropertyBase {
        override fun isEnabled(): Boolean {
            return !token.data.trim { it <= ' ' }.isEmpty() && !modid.trim { it <= ' ' }.isEmpty()
        }

        companion object {
            fun getInstance(properties: PropertiesComponent): ModrinthProperty {
                return ModrinthProperty(
                    PID.ModrinthToken.getProtect(properties),
                    PID.ModrinthModID.get(properties)
                )
            }
        }
    }

    companion object {
        fun getInstance(properties: PropertiesComponent): Property {
            return Property(
                ModrinthProperty.getInstance(properties),
                CurseForgeProperty.getInstance(properties),
                GithubProperty.getInstance(properties),
                CommonProperty.getInstance(properties)
            )
        }
    }
}