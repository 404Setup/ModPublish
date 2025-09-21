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
package one.pkg.modpublish.settings

import one.pkg.modpublish.data.internal.Info
import one.pkg.modpublish.util.protect.HardwareFingerprint
import one.pkg.modpublish.util.protect.Protect
import one.pkg.modpublish.util.protect.Protect.decryptString

open class StateBase {
    fun getDecryptedToken(encryptedToken: String): Info {
        if (encryptedToken.isEmpty()) return Info.INSTANCE
        val v = decryptString(encryptedToken, HardwareFingerprint.secureProjectKey)
        if (encryptedToken == v) return Info.of(v, true, true)
        return Info.of(v, false, true)
    }

    fun encryptToken(token: String?): String {
        return (if (token == null || token.isBlank()) "" else Protect.encryptString(
            token,
            HardwareFingerprint.secureProjectKey
        ))
    }
}
