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
package one.pkg.modpublish.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.engine.*
import io.ktor.client.engine.java.Java
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import one.pkg.modpublish.settings.ModPublishSettings
import one.tranic.t.proxy.ProxyConfigReader
import java.net.InetSocketAddress
import java.net.Proxy

object NetworkUtil {
    val client: HttpClient

    init {
        val state = requireNotNull(ModPublishSettings.getInstance().state)
        val proxy = getProxy(state)

        client = HttpClient(Java) {
            engine {
                if (!state.networkEnableSSLCheck) {
                    config {
                        sslContext(SSLSocketClient.sslContext)
                    }
                }

                if (proxy.type() != Proxy.Type.DIRECT) {
                    val address = proxy.address() as InetSocketAddress
                    val host = address.hostString
                    val port = address.port
                    
                    val username = state.proxyUsername
                    val password = state.proxyPassword
                    
                    val authStr = if (username.isNotEmpty() && password.isNotEmpty()) {
                        "$username:$password@"
                    } else ""

                    val proxyUrl = when (proxy.type()) {
                        Proxy.Type.HTTP -> "http://$authStr$host:$port"
                        Proxy.Type.SOCKS -> "socks://$authStr$host:$port"
                        else -> null
                    }
                    if (proxyUrl != null) {
                        this.proxy = ProxyBuilder.http(proxyUrl)
                    }
                }
            }

            install(HttpTimeout) {
                requestTimeoutMillis = state.networkReadTimeout.toLong() * 1000
                connectTimeoutMillis = state.networkConnectTimeout.toLong() * 1000
            }

            defaultRequest {
                header(HttpHeaders.UserAgent, "modpublish/v1 (github.com/404Setup/ModPublish)")
            }
        }
    }

    private fun isValidIpAddress(ip: String): Boolean = runCatching {
        ip.split('.').let { parts ->
            if (parts.size == 4) return parts.all { it.toInt() in 0..255 }
        }
        ip.split(':').let { parts ->
            if (parts.isNotEmpty()) return parts.all { it.isEmpty() || it.length <= 4 && it.toInt(16) >= 0 }
        }
        false
    }.getOrDefault(false)

    fun getProxy(state: ModPublishSettings.State): Proxy {
        if (state.autoProxy) return ProxyConfigReader.getProxy(Proxy.NO_PROXY)
        if (state.proxyAddress.isBlank() || state.proxyPort !in 1..65535 || !isValidIpAddress(state.proxyAddress)) return Proxy.NO_PROXY
        return Proxy(state.getProxyType(), InetSocketAddress(state.proxyAddress, state.proxyPort))
    }
}