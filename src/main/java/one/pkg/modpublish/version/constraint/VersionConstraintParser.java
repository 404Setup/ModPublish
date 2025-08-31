package one.pkg.modpublish.version.constraint;

import one.pkg.modpublish.version.Version;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@ApiStatus.Experimental
@SuppressWarnings("unused")
public class VersionConstraintParser {
    private static final String VERSION_PATTERN = "\\d+\\.\\d+\\.\\d+(?:-(?:pre|rc)\\d+)?|b\\d+\\.\\d+\\.\\d+|\\d{2}w\\d{2}[a-z](?:_or_[a-z])?|[\\w.-]+";

    private static final Pattern EXACT_PATTERN = Pattern.compile("^(" + VERSION_PATTERN + ")$");
    private static final Pattern RANGE_PATTERN = Pattern.compile("^(" + VERSION_PATTERN + ")-(" + VERSION_PATTERN + ")$");
    private static final Pattern EQUAL_PATTERN = Pattern.compile("^=(" + VERSION_PATTERN + ")$");
    private static final Pattern TILDE_PATTERN = Pattern.compile("^~(" + VERSION_PATTERN + ")$");
    private static final Pattern CARET_PATTERN = Pattern.compile("^\\^(" + VERSION_PATTERN + ")$");
    private static final Pattern COMPARISON_PATTERN = Pattern.compile("^(>=|<=|>|<)\\s*(" + VERSION_PATTERN + ")$");
    private static final Pattern MAVEN_RANGE_PATTERN = Pattern.compile("^[\\[(]([\\w.,-]+)[])]$");
    private static final Pattern COMPOSITE_PATTERN = Pattern.compile("^(.+?)\\s+(.+)$");

    public static VersionConstraint parse(String constraintStr) {
        if (constraintStr == null || constraintStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Version constraint cannot be empty");
        }

        String trimmed = constraintStr.trim();

        Matcher exactMatcher = EXACT_PATTERN.matcher(trimmed);
        if (exactMatcher.matches()) return new ExactVersionConstraint(exactMatcher.group(1));

        Matcher equalMatcher = EQUAL_PATTERN.matcher(trimmed);
        if (equalMatcher.matches()) return new ExactVersionConstraint(equalMatcher.group(1));

        Matcher rangeMatcher = RANGE_PATTERN.matcher(trimmed);
        if (rangeMatcher.matches()) {
            Version min = new Version(rangeMatcher.group(1));
            Version max = new Version(rangeMatcher.group(2));
            return new RangeConstraint(min, max, true, true, trimmed);
        }

        Matcher tildeMatcher = TILDE_PATTERN.matcher(trimmed);
        if (tildeMatcher.matches()) return new TildeConstraint(trimmed);

        Matcher caretMatcher = CARET_PATTERN.matcher(trimmed);
        if (caretMatcher.matches()) return new CaretConstraint(trimmed);

        Matcher comparisonMatcher = COMPARISON_PATTERN.matcher(trimmed);
        if (comparisonMatcher.matches())
            return parseComparison(comparisonMatcher.group(1), comparisonMatcher.group(2), trimmed);

        Matcher mavenMatcher = MAVEN_RANGE_PATTERN.matcher(trimmed);
        if (mavenMatcher.matches())
            return parseMavenRange(trimmed, mavenMatcher.group(1));

        Matcher compositeMatcher = COMPOSITE_PATTERN.matcher(trimmed);
        if (compositeMatcher.matches()) {
            try {
                VersionConstraint first = parse(compositeMatcher.group(1));
                VersionConstraint second = parse(compositeMatcher.group(2));
                return new CompositeConstraint(Arrays.asList(first, second), trimmed);
            } catch (Exception ignored) {
            }
        }

        throw new IllegalArgumentException("Unable to parse version constraint: " + constraintStr);
    }

    private static VersionConstraint parseComparison(String operator, String versionStr, String original) throws IllegalArgumentException {
        Version version = new Version(versionStr);

        return switch (operator) {
            case ">=" -> new RangeConstraint(version, null, true, false, original);
            case "<=" -> new RangeConstraint(null, version, false, true, original);
            case ">" -> new RangeConstraint(version, null, false, false, original);
            case "<" -> new RangeConstraint(null, version, false, false, original);
            default -> throw new IllegalArgumentException("Unknown comparison operator: " + operator);
        };
    }

    private static VersionConstraint parseMavenRange(String original, String content) {
        boolean includeMin = original.startsWith("[");
        boolean includeMax = original.endsWith("]");

        String[] parts = content.split(",");

        if (parts.length == 1) {
            if (original.endsWith(",)")) {
                Version min = new Version(parts[0]);
                return new RangeConstraint(min, null, includeMin, false, original);
            } else {
                return new ExactVersionConstraint(parts[0]);
            }
        } else if (parts.length == 2) {
            Version min = new Version(parts[0]);
            Version max = parts[1].isEmpty() ? null : new Version(parts[1]);
            return new RangeConstraint(min, max, includeMin, includeMax, original);
        } else {
            List<VersionConstraint> constraints = new ArrayList<>();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    constraints.add(new ExactVersionConstraint(part));
                }
            }
            return new OrConstraint(constraints, original);
        }
    }
}