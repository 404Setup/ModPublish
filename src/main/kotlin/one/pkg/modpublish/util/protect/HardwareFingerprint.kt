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
package one.pkg.modpublish.util.protect

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import java.security.SecureRandom
import java.util.Base64

@Suppress("UNUSED")
class HardwareFingerprint private constructor() {
    init {
        val about =
            "This class is used to retrieve or generate a unique security ID from PasswordSafe to protect API keys."
    }

    companion object {
        private var key: String? = null

        val secureProjectKey: String
            get() {
                if (key == null) key = getOrGenerateKey()
                return key!!
            }

        private fun getOrGenerateKey(): String {
            val attributes = CredentialAttributes("ModPublish-SecureProjectKey")
            var password = PasswordSafe.instance.getPassword(attributes)
            if (password == null) {
                val bytes = ByteArray(32)
                SecureRandom().nextBytes(bytes)
                password = Base64.getEncoder().encodeToString(bytes)
                PasswordSafe.instance.setPassword(attributes, password)
            }
            return password
        }

        fun getEnvironmentFingerprint(): String = "SecureRandomKey"

        fun validateEnvironmentBinding(expectedKey: String): Boolean =
            secureProjectKey == expectedKey
    }
}
