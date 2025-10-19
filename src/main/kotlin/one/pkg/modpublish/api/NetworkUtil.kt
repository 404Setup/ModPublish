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
package one.pkg.modpublish.api

import okhttp3.Authenticator
import okhttp3.Credentials.basic
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.Route
import one.pkg.modpublish.settings.ModPublishSettings
import one.tranic.t.proxy.ProxyConfigReader
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object NetworkUtil {
    val client: OkHttpClient

    init {
        val state = requireNotNull(ModPublishSettings.getInstance().state)
        val proxy = getProxy(state)

        client = OkHttpClient.Builder().apply {
            proxy(proxy)
            connectTimeout(state.networkConnectTimeout.toLong(), TimeUnit.SECONDS)
            readTimeout(state.networkReadTimeout.toLong(), TimeUnit.SECONDS)
            writeTimeout(state.networkWriteTimeout.toLong(), TimeUnit.SECONDS)

            if (!state.networkEnableSSLCheck) {
                hostnameVerifier(SSLSocketClient.hostnameVerifier)
                sslSocketFactory(SSLSocketClient.sslSocketFactory, SSLSocketClient.x509TrustManager)
            }

            (state.proxyUsername.takeIf { it.isNotEmpty() }?.let { username ->
                state.proxyPassword.takeIf { it.isNotEmpty() }?.let { password ->
                    proxy.takeIf { it != Proxy.NO_PROXY || state.proxyAddress.isNotBlank() && state.proxyPort > 0 }?.let {
                        Authenticator { _: Route?, response: Response ->
                            response.request.header("Proxy-Authorization")?.let {
                                response.request.newBuilder()
                                    .header("Proxy-Authorization", basic(username, password))
                                    .build()
                            }
                        }
                    }
                }
            })?.also { proxyAuthenticator(it) }
        }.build()
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