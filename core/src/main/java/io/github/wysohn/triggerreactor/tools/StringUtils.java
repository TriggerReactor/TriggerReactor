package io.github.wysohn.triggerreactor.tools;

import java.util.Locale;

public class StringUtils {
    public static String spaces(int n) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++)
            builder.append(' ');
        return builder.toString();
    }

    /**
     * Combine all strings joined together with .(dot)
     * <p>
     * dottedPath("Some", "Thing", "Path") would yield "Some.Thing.Path"
     *
     * @param paths
     * @return
     */
    public static String dottedPath(String... paths) {
        if (paths.length < 2)
            return paths[0];

        StringBuilder builder = new StringBuilder(paths[0]);
        for (int i = 1; i < paths.length; i++) {
            builder.append('.');
            builder.append(paths[i]);
        }

        return builder.toString();
    }

    public static boolean hasUpperCase(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isUpperCase(str.charAt(i)))
                return true;
        }
        return false;
    }

    /**
     * Compares the given string to the given value, ignoring case considerations.
     *
     * @param str The string to compare
     * @param value The value to compare
     * @return {@code True} if the argument is not {@code null} and they are match, ignoring case; {@code false}
     *          otherwise
     */
    public static boolean compareTokenCaseInsensitive(String str, Object value) {
        if (str == null) return false;

        return str.equalsIgnoreCase(value.toString().toLowerCase(Locale.ROOT));
    }
}
