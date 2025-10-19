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

import java.net.NetworkInterface
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.math.abs

@Suppress("UNUSED")
class HardwareFingerprint private constructor() {
    init {
        val about =
            "This class is used to generate a unique security ID to prevent the user set API keys from being stolen."
    }

    companion object {
        private var key: String? = null

        val secureProjectKey: String
            get() {
                if (key == null) key = generateKeyBase()
                return key!!
            }

        private fun generateKeyBase(): String = runCatching {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(getPlatformBindingInfo().toByteArray(StandardCharsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }.substring(0, 32)
        }.getOrDefault(generateFallbackKey())

        fun getEnvironmentFingerprint(): String = getPlatformBindingInfo()

        fun validateEnvironmentBinding(expectedKey: String): Boolean =
            secureProjectKey == expectedKey

        private fun getPlatformBindingInfo(): String = runCatching {
            val osInfo = "OS:${System.getProperty("os.name", "unknown")}-${System.getProperty("os.arch", "unknown")}"
            val userInfo =
                "USER:${System.getProperty("user.name", "unknown")}-${
                    System.getProperty("user.home", "unknown").hashCode()
                }"
            val macInfo = getMacAddress()?.let { "MAC:$it" }
            val javaInfo = "JAVA:${extractMajorJavaVersion(System.getProperty("java.version", "unknown"))}"
            val machineInfo = "MACHINE:${getStableMachineId()}"

            listOfNotNull(osInfo, userInfo, macInfo, javaInfo, machineInfo)
                .joinToString("|")
        }.getOrDefault("FALLBACK:${getStableFallback()}")

        private fun getMacAddress(): String? =
            NetworkInterface.getNetworkInterfaces().toList()
                .asSequence()
                .filter { !it.isLoopback && it.hardwareAddress != null }
                .mapNotNull { ni ->
                    val mac = ni.hardwareAddress
                    val name = ni.name.lowercase()
                    val display = ni.displayName.lowercase()
                    if (listOf("virtual", "vmware", "virtualbox", "hyper-v", "bluetooth", "loopback")
                            .any { display.contains(it) } || listOf("veth", "docker").any { name.startsWith(it) } ||
                        name.startsWith("br-") || name.startsWith("virbr") || (mac[0].toInt() and 0x02 != 0)
                    ) return@mapNotNull null
                    mac.joinToString(":") { "%02X".format(it) }
                }
                .sorted()
                .firstOrNull()

        private fun extractMajorJavaVersion(version: String): String = runCatching {
            val parts = version.split(".")
            if (version.startsWith("1.") && parts.size >= 2) "1.${parts[1]}" else parts[0]
        }.getOrDefault(version)

        private fun getStableMachineId(): String = listOfNotNull(
            System.getProperty("user.home")?.takeIf { it.isNotEmpty() }?.let { "HOME:${it.hashCode()}" },
            "CPU:${Runtime.getRuntime().availableProcessors()}",
            "ARCH:${System.getProperty("os.arch", "unknown")}",
            "SEP:${System.getProperty("file.separator", "/").hashCode()}"
        ).joinToString("-")

        private fun getStableFallback(): String {
            val userName = System.getProperty("user.name", "unknown")
            val userHomeHash = System.getProperty("user.home", "unknown").hashCode()
            val osName = System.getProperty("os.name", "unknown")
            return listOf(userName, userHomeHash, osName).hashCode().toString()
        }

        private fun generateFallbackKey(): String {
            val hash = abs(getStableFallback().hashCode())
            return "%032d".format(hash).substring(0, 32)
        }

        private fun <T> java.util.Enumeration<T>.toList(): List<T> {
            val list = mutableListOf<T>()
            while (hasMoreElements()) list.add(nextElement())
            return list
        }
    }
}