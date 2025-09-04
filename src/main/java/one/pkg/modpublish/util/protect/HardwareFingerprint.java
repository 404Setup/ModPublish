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

package one.pkg.modpublish.util.protect;

import org.jetbrains.annotations.NotNull;

import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HardwareFingerprint {
    private static String key;

    private HardwareFingerprint() {
        final String about = "This class is used to generate a unique security ID to prevent the user set API keys from being stolen.";
    }

    public static String generateSecureProjectKey() {
        if (key == null) key = getKeyBase();
        return key;
    }

    private static String getKeyBase() {
        try {
            String platformInfo = getPlatformBindingInfo();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(platformInfo.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.substring(0, 32);
        } catch (Exception e) {
            return generateFallbackKey();
        }
    }

    private static String getPlatformBindingInfo() {
        StringBuilder platformInfo = new StringBuilder();

        try {
            String osName = System.getProperty("os.name", "unknown");
            String osArch = System.getProperty("os.arch", "unknown");
            platformInfo.append("OS:").append(osName).append("-").append(osArch);

            String userName = System.getProperty("user.name", "unknown");
            String userHome = System.getProperty("user.home", "unknown");
            platformInfo.append("|USER:").append(userName).append("-").append(userHome.hashCode());

            String macAddress = getMacAddress();
            if (macAddress != null && !macAddress.isEmpty()) {
                platformInfo.append("|MAC:").append(macAddress);
            }

            String javaVersion = System.getProperty("java.version", "unknown");
            String javaMajorVersion = extractMajorVersion(javaVersion);
            platformInfo.append("|JAVA:").append(javaMajorVersion);

            String machineId = getStableMachineId();
            platformInfo.append("|MACHINE:").append(machineId);
        } catch (Exception e) {
            platformInfo.append("FALLBACK:").append(getStableFallback());
        }

        return platformInfo.toString();
    }

    private static String getMacAddress() {
        try {
            List<String> macAddresses = new ArrayList<>();

            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isLoopback() && ni.getHardwareAddress() != null) {
                    byte[] mac = ni.getHardwareAddress();

                    String displayName = ni.getDisplayName().toLowerCase();
                    String name = ni.getName().toLowerCase();

                    if (displayName.contains("virtual") || displayName.contains("vmware") ||
                            displayName.contains("virtualbox") || displayName.contains("hyper-v") ||
                            displayName.contains("bluetooth") || displayName.contains("loopback") ||
                            name.startsWith("veth") || name.startsWith("docker") ||
                            name.startsWith("br-") || name.startsWith("virbr")) {
                        continue;
                    }

                    if ((mac[0] & 0x02) != 0) {
                        continue;
                    }

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X", mac[i]));
                        if (i < mac.length - 1) {
                            sb.append(":");
                        }
                    }
                    macAddresses.add(sb.toString());
                }
            }

            if (!macAddresses.isEmpty()) {
                Collections.sort(macAddresses);
                return macAddresses.get(0);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String extractMajorVersion(String javaVersion) {
        try {
            String[] parts = javaVersion.split("\\.");
            if (javaVersion.startsWith("1.")) {
                if (parts.length >= 2) {
                    return "1." + parts[1];
                }
            } else {
                if (parts.length >= 1) {
                    return parts[0];
                }
            }
        } catch (Exception ignored) {
        }
        return javaVersion;
    }

    private static String getStableMachineId() {
        try {
            List<String> identifiers = new ArrayList<>();

            String userHome = System.getProperty("user.home", "");
            if (!userHome.isEmpty()) {
                identifiers.add("HOME:" + userHome.hashCode());
            }

            int processors = Runtime.getRuntime().availableProcessors();
            identifiers.add("CPU:" + processors);

            String osArch = System.getProperty("os.arch", "unknown");
            identifiers.add("ARCH:" + osArch);

            String fileSeparator = System.getProperty("file.separator", "/");
            identifiers.add("SEP:" + fileSeparator.hashCode());

            return String.join("-", identifiers);
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private static String getStableFallback() {
        String userName = System.getProperty("user.name", "unknown");
        String userHome = System.getProperty("user.home", "unknown");
        String osName = System.getProperty("os.name", "unknown");

        return String.valueOf(Objects.hash(userName, userHome.hashCode(), osName));
    }

    public static boolean validateEnvironmentBinding(@NotNull String expectedKey) {
        String currentKey = generateSecureProjectKey();
        return currentKey.equals(expectedKey);
    }

    public static String getEnvironmentFingerprint() {
        return getPlatformBindingInfo();
    }

    private static String generateFallbackKey() {
        String fallback = getStableFallback();
        int hash = Math.abs(fallback.hashCode());
        return String.format("%032d", hash).substring(0, 32);
    }

}
