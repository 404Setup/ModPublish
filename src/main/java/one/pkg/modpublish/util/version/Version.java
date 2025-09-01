package one.pkg.modpublish.util.version;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class Version implements Comparable<Version> {
    private static final Pattern RELEASE_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)$");
    private static final Pattern PRE_RELEASE_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)-(pre|rc)(\\d+)$");
    private static final Pattern BETA_PATTERN = Pattern.compile("^b(\\d+)\\.(\\d+)\\.(\\d+)$");
    private static final Pattern SNAPSHOT_PATTERN = Pattern.compile("^(\\d{2})w(\\d{2})([a-z])(?:_or_([a-z]))?$");
    private final String original;
    private final VersionType type;
    private int major;
    private int minor;
    private int patch;
    private String preRelease;

    public Version(String version) {
        this.original = version.trim();

        if (parseReleaseVersion()) {
            this.type = VersionType.RELEASE;
            this.preRelease = null;
        } else if (parsePreReleaseVersion()) {
            this.type = determinePreReleaseType();
        } else if (parseBetaVersion()) {
            this.type = VersionType.BETA;
            this.preRelease = null;
        } else if (parseSnapshotVersion()) {
            this.type = VersionType.SNAPSHOT;
            this.preRelease = null;
        } else {
            String[] parts = this.original.split("\\.");
            this.major = parts.length > 0 ? parseVersionPart(parts[0]) : 0;
            this.minor = parts.length > 1 ? parseVersionPart(parts[1]) : 0;
            this.patch = parts.length > 2 ? parseVersionPart(parts[2]) : 0;
            this.type = VersionType.UNKNOWN;
            this.preRelease = null;
        }
    }

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.original = major + "." + minor + "." + patch;
        this.type = VersionType.RELEASE;
        this.preRelease = null;
    }

    private boolean parseReleaseVersion() {
        Matcher matcher = RELEASE_PATTERN.matcher(original);
        if (matcher.matches()) {
            this.major = Integer.parseInt(matcher.group(1));
            this.minor = Integer.parseInt(matcher.group(2));
            this.patch = Integer.parseInt(matcher.group(3));
            return true;
        }
        return false;
    }

    private boolean parsePreReleaseVersion() {
        Matcher matcher = PRE_RELEASE_PATTERN.matcher(original);
        if (matcher.matches()) {
            this.major = Integer.parseInt(matcher.group(1));
            this.minor = Integer.parseInt(matcher.group(2));
            this.patch = Integer.parseInt(matcher.group(3));
            this.preRelease = matcher.group(4) + matcher.group(5);
            return true;
        }
        return false;
    }

    private boolean parseBetaVersion() {
        Matcher matcher = BETA_PATTERN.matcher(original);
        if (matcher.matches()) {
            this.major = Integer.parseInt(matcher.group(1));
            this.minor = Integer.parseInt(matcher.group(2));
            this.patch = Integer.parseInt(matcher.group(3));
            return true;
        }
        return false;
    }

    private boolean parseSnapshotVersion() {
        Matcher matcher = SNAPSHOT_PATTERN.matcher(original);
        if (matcher.matches()) {
            int year = Integer.parseInt(matcher.group(1));
            int week = Integer.parseInt(matcher.group(2));
            String letter = matcher.group(3);

            this.major = 2000 + year;
            this.minor = week;
            this.patch = letter.charAt(0) - 'a';
            return true;
        }
        return false;
    }

    private VersionType determinePreReleaseType() {
        if (preRelease != null) {
            if (preRelease.startsWith("rc")) {
                return VersionType.RELEASE_CANDIDATE;
            } else if (preRelease.startsWith("pre")) {
                return VersionType.PRE_RELEASE;
            }
        }
        return VersionType.UNKNOWN;
    }

    private int parseVersionPart(String part) {
        StringBuilder sb = new StringBuilder();
        for (char c : part.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            } else {
                break;
            }
        }
        return !sb.isEmpty() ? Integer.parseInt(sb.toString()) : 0;
    }

    @Override
    public int compareTo(Version other) {
        int result = Integer.compare(this.major, other.major);
        if (result != 0) return result;

        result = Integer.compare(this.minor, other.minor);
        if (result != 0) return result;

        result = Integer.compare(this.patch, other.patch);
        if (result != 0) return result;

        result = Integer.compare(getTypePriority(), other.getTypePriority());
        if (result != 0) return result;

        if (this.preRelease != null && other.preRelease != null) {
            return this.preRelease.compareTo(other.preRelease);
        }

        return 0;
    }

    private int getTypePriority() {
        return switch (type) {
            case BETA -> 1;
            case SNAPSHOT -> 2;
            case PRE_RELEASE -> 3;
            case RELEASE_CANDIDATE -> 4;
            case RELEASE -> 5;
            case UNKNOWN -> 0;
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Version version = (Version) obj;
        return major == version.major &&
                minor == version.minor &&
                patch == version.patch &&
                type == version.type &&
                Objects.equals(preRelease, version.preRelease);
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, type, preRelease);
    }

    @Override
    public String toString() {
        return original;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public String getOriginal() {
        return original;
    }

    public VersionType getType() {
        return type;
    }

    public String getPreRelease() {
        return preRelease;
    }

    public enum VersionType {
        RELEASE,
        PRE_RELEASE,
        RELEASE_CANDIDATE,
        BETA,
        SNAPSHOT,
        UNKNOWN
    }
}