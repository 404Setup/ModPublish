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
package one.pkg.modpublish.util.protect

import com.intellij.ui.components.JBTextField
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

@Suppress("UNUSED")
object Protect {
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 16

    private fun deriveKey(hash: String): SecretKey {
        val salt = ByteArray(16).apply {
            val hashBytes = hash.toByteArray(StandardCharsets.UTF_8)
            System.arraycopy(hashBytes, 0, this, 0, hashBytes.size.coerceAtMost(16))
        }
        val spec: KeySpec = PBEKeySpec(hash.toCharArray(), salt, 100_000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val key = factory.generateSecret(spec).encoded
        return SecretKeySpec(key, "AES")
    }

    fun encryptString(data: String, hash: String): String = runCatching {
        val key = deriveKey(hash)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(GCM_IV_LENGTH).apply { SecureRandom().nextBytes(this) }
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH * 8, iv))
        val encryptedBytes = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        (iv + encryptedBytes).let { Base64.getEncoder().encodeToString(it) }
    }.getOrDefault(data)

    fun JBTextField.decryptString(): String = decryptString(this.text)

    fun decryptString(encryptedData: String): String =
        decryptString(encryptedData, HardwareFingerprint.secureProjectKey)

    fun decryptString(encryptedData: String, hash: String): String = runCatching {
        val key = deriveKey(hash)
        val encryptedWithIv = Base64.getDecoder().decode(encryptedData)
        val iv = encryptedWithIv.copyOfRange(0, GCM_IV_LENGTH)
        val encrypted = encryptedWithIv.copyOfRange(GCM_IV_LENGTH, encryptedWithIv.size)
        Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH * 8, iv))
        }.doFinal(encrypted).toString(StandardCharsets.UTF_8)
    }.getOrDefault(encryptedData)
}
