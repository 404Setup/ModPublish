package one.pkg.modpublish.data.internel;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModVersion {
    public static String extractVersionNumber(VirtualFile file) {
        String name = file.getNameWithoutExtension();
        String extractedVersion = extractVersionFromPattern(name);
        return validateAndNormalizeVersion(extractedVersion);
    }

    private static String extractVersionFromPattern(String filename) {
        int lastDash = filename.lastIndexOf('-');
        if (lastDash > 0 && lastDash < filename.length() - 1) {
            String candidate = filename.substring(lastDash + 1);
            if (isValidVersionPattern(candidate)) {
                return candidate;
            }
        }

        int lastUnderscore = filename.lastIndexOf('_');
        if (lastUnderscore > 0 && lastUnderscore < filename.length() - 1) {
            String candidate = filename.substring(lastUnderscore + 1);
            if (isValidVersionPattern(candidate)) {
                return candidate;
            }
        }

        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)*(?:[-.]?(?:alpha|beta|rc|snapshot|dev)\\d*)?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(filename);

        String lastMatch = null;
        while (matcher.find()) {
            lastMatch = matcher.group(1);
        }

        return isValidVersionPattern(lastMatch) ?  lastMatch : null;
    }

    private static boolean isValidVersionPattern(String version) {
        if (version == null || version.trim().isEmpty()) {
            return false;
        }

        version = version.replaceFirst("^[vV]", "");

        String versionPattern = "^\\d+(?:\\.\\d+)*(?:[-.]?(?:alpha|beta|rc|snapshot|dev|final|release)\\d*)?$";
        return version.matches(versionPattern);
    }

    private static String validateAndNormalizeVersion(@Nullable String version) {
        if (version == null) return "1.0.0";

        version = version.trim().replaceFirst("^[vV]", "");

        if (!isValidVersionPattern(version)) {
            return "1.0.0";
        }

        return normalizeVersionFormat(version);
    }

    private static String normalizeVersionFormat(String version) {
        String[] parts = version.split("[-.](?=alpha|beta|rc|snapshot|dev|final|release)", 2);
        String mainVersion = parts[0];
        String preRelease = parts.length > 1 ? parts[1] : null;

        String[] versionParts = mainVersion.split("\\.");
        if (versionParts.length == 1) {
            mainVersion = mainVersion + ".0.0";
        } else if (versionParts.length == 2) {
            mainVersion = mainVersion + ".0";
        }

        String[] finalParts = mainVersion.split("\\.");
        for (String part : finalParts) {
            try {
                Integer.parseInt(part);
            } catch (NumberFormatException e) {
                return "1.0.0";
            }
        }

        if (preRelease != null) {
            return mainVersion + "-" + preRelease;
        }

        return mainVersion;
    }

}
