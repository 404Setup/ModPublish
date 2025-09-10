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

package one.pkg.modpublish.util.proxy;

import one.pkg.modpublish.settings.ModPublishSettings;
import one.tranic.t.proxy.ProxyConfigReader;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;

public class MProxy {
    private static boolean isValidIpAddress(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                for (String part : parts) {
                    int value = Integer.parseInt(part);
                    if (value < 0 || value > 255) return false;
                }
                return true;
            }
            parts = ip.split(":");
            if (parts.length > 0) {
                for (String part : parts) {
                    if (part.length() > 4) return false;
                    if (!part.isEmpty()) Integer.parseInt(part, 16);
                }
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Proxy getProxy() {
        ModPublishSettings.State state = Objects.requireNonNull(ModPublishSettings.getInstance().getState());
        if (state.autoProxy) return ProxyConfigReader.getProxy(Proxy.NO_PROXY);
        if (state.proxyAddress.isBlank() || state.proxyPort < 1 || state.proxyPort > 65535 || !isValidIpAddress(state.proxyAddress))
            return Proxy.NO_PROXY;

        return new Proxy(state.getProxyType(), new InetSocketAddress(state.proxyAddress, state.proxyPort));
    }
}
