package one.pkg.modpublish.protect;

import org.jetbrains.annotations.NotNull;

import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
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
            String secureIdentifier = String.join("|", platformInfo);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(secureIdentifier.getBytes(StandardCharsets.UTF_8));
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
            String osVersion = System.getProperty("os.version", "unknown");
            String osArch = System.getProperty("os.arch", "unknown");
            platformInfo.append("OS:").append(osName).append("-").append(osVersion).append("-").append(osArch);

            String userName = System.getProperty("user.name", "unknown");
            String userHome = System.getProperty("user.home", "unknown");
            platformInfo.append("|USER:").append(userName).append("-").append(userHome.hashCode());

            String macAddress = getMacAddress();
            if (macAddress != null && !macAddress.isEmpty()) {
                platformInfo.append("|MAC:").append(macAddress);
            }

            String javaVersion = System.getProperty("java.version", "unknown");
            String javaVendor = System.getProperty("java.vendor", "unknown");
            platformInfo.append("|JAVA:").append(javaVersion).append("-").append(javaVendor.hashCode());

            String tempDir = System.getProperty("java.io.tmpdir", "");
            platformInfo.append("|MACHINE:").append(tempDir.hashCode());
        } catch (Exception e) {
            platformInfo.append("FALLBACK:").append(System.currentTimeMillis() / (1000L * 60 * 60 * 24)); // 按天计算
        }

        return platformInfo.toString();
    }

    private static String getMacAddress() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isLoopback() && ni.isUp() && ni.getHardwareAddress() != null) {
                    byte[] mac = ni.getHardwareAddress();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X", mac[i]));
                        if (i < mac.length - 1) {
                            sb.append(":");
                        }
                    }
                    return sb.toString();
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static boolean validateEnvironmentBinding(@NotNull String expectedKey) {
        String currentKey = generateSecureProjectKey();
        return currentKey.equals(expectedKey);
    }

    public static String getEnvironmentFingerprint() {
        return getPlatformBindingInfo();
    }

    private static String generateFallbackKey() {
        String userName = System.getProperty("user.name", "unknown");

        int hash = Objects.hash(userName);
        return String.format("%032d", Math.abs(hash)).substring(0, 32);
    }
}
