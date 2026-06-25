package one.pkg.modpublish.version.constraint;

import one.pkg.modpublish.version.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionConstraintParser {
    private static final String VERSION_PATTERN =
            "\\d+\\.\\d+\\.\\d+(?:-(?:pre|rc)\\d+)?|b\\d+\\.\\d+\\.\\d+|\\d{2}w\\d{2}[a-z](?:_or_[a-z])?|[\\w.-]+";

    private static final Pattern SIMPLE_VERSION_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+){1,2})$");
    private static final Pattern EXACT_PATTERN = Pattern.compile("^(" + VERSION_PATTERN + ")$");
    private static final Pattern RANGE_PATTERN = Pattern.compile("^(" + VERSION_PATTERN + ")-(" + VERSION_PATTERN + ")$");
    private static final Pattern EQUAL_PATTERN = Pattern.compile("^=(" + VERSION_PATTERN + ")$");
    private static final Pattern TILDE_PATTERN = Pattern.compile("^~(" + VERSION_PATTERN + ")$");
    private static final Pattern CARET_PATTERN = Pattern.compile("^\\^(" + VERSION_PATTERN + ")$");
    private static final Pattern COMPARISON_PATTERN = Pattern.compile("^(>=|<=|>|<)\\s*(" + VERSION_PATTERN + ")$");
    private static final Pattern MAVEN_RANGE_PATTERN = Pattern.compile("^[\\[(]([\\w.,\\s-]+)[])]$");
    private static final Pattern COMPOSITE_PATTERN = Pattern.compile("^(.+?)\\s+(.+)$");

    public static one.pkg.modpublish.version.constraint.VersionConstraint parse(String constraintStr) throws IllegalArgumentException {
        String trimmed = constraintStr.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Version constraint cannot be empty");
        }

        String normalized = trimmed.replaceAll("([><=])\\s+", "$1");

        Matcher simpleMatcher = SIMPLE_VERSION_PATTERN.matcher(normalized);
        if (simpleMatcher.matches()) {
            return new ExactVersionConstraint(simpleMatcher.group(1));
        }

        Matcher equalMatcher = EQUAL_PATTERN.matcher(normalized);
        if (equalMatcher.matches()) return new ExactVersionConstraint(equalMatcher.group(1));

        Matcher rangeMatcher = RANGE_PATTERN.matcher(normalized);
        if (rangeMatcher.matches()) {
            Version min = new Version(rangeMatcher.group(1));
            Version max = new Version(rangeMatcher.group(2));
            return new RangeConstraint(min, max, true, true, trimmed);
        }

        Matcher tildeMatcher = TILDE_PATTERN.matcher(normalized);
        if (tildeMatcher.matches()) return new TildeConstraint(trimmed);

        Matcher caretMatcher = CARET_PATTERN.matcher(normalized);
        if (caretMatcher.matches()) return new CaretConstraint(trimmed);

        Matcher comparisonMatcher = COMPARISON_PATTERN.matcher(normalized);
        if (comparisonMatcher.matches()) {
            return parseComparison(comparisonMatcher.group(1), comparisonMatcher.group(2), trimmed);
        }

        Matcher mavenRangeMatcher = MAVEN_RANGE_PATTERN.matcher(normalized);
        if (mavenRangeMatcher.matches()) {
            return parseMavenRange(trimmed, mavenRangeMatcher.group(1));
        }

        Matcher compositeMatcher = COMPOSITE_PATTERN.matcher(normalized);
        if (compositeMatcher.matches()) {
            try {
                VersionConstraint first = parse(compositeMatcher.group(1));
                VersionConstraint second = parse(compositeMatcher.group(2));
                return new CompositeConstraint(List.of(first, second), trimmed);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse version constraint: " + constraintStr);
            }
        }

        Matcher exactMatcher = EXACT_PATTERN.matcher(normalized);
        if (exactMatcher.matches()) return new ExactVersionConstraint(exactMatcher.group(1));

        throw new IllegalArgumentException("Unable to parse version constraint: " + constraintStr);
    }

    private static VersionConstraint parseComparison(String operator, String versionStr, String original)
            throws IllegalArgumentException {
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

        int end = content.length();
        while (end > 0 && content.charAt(end - 1) == ',') {
            end--;
        }
        String sliced = content.substring(0, end);

        int commaIndex = sliced.indexOf(',');

        if (commaIndex == -1) {
            if (original.endsWith(",)")) {
                Version min = new Version(sliced.trim());
                return new RangeConstraint(min, null, includeMin, false, original);
            } else {
                return new ExactVersionConstraint(sliced.trim());
            }
        }

        int nextCommaIndex = sliced.indexOf(',', commaIndex + 1);

        if (nextCommaIndex == -1) {
            String part1 = sliced.substring(0, commaIndex).trim();
            String part2 = sliced.substring(commaIndex + 1).trim();

            Version min = new Version(part1);
            Version max = part2.isEmpty() ? null : new Version(part2);
            return new RangeConstraint(min, max, includeMin, includeMax, original);
        } else {
            String[] parts = sliced.split(",");
            String part1 = parts[0].trim();
            String partLast = parts[parts.length - 1].trim();

            Version min = new Version(part1);
            Version max = partLast.isEmpty() ? null : new Version(partLast);
            RangeConstraint range = new RangeConstraint(min, max, includeMin, includeMax, original);

            List<VersionConstraint> constraints = new ArrayList<>();
            constraints.add(range);

            for (int i = 1; i < parts.length - 1; i++) {
                String mid = parts[i].trim();
                if (!mid.isEmpty()) {
                    constraints.add(new ExactVersionConstraint(mid));
                }
            }

            return new OrConstraint(constraints, original);
        }
    }
}
