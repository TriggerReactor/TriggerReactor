package io.github.wysohn.triggerreactor.tools;

import java.util.Arrays;

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

    public static String repeat(final String s, final int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count is negative: " + count);
        }

        if (count == 1) {
            return s;
        }

        final int len = s.length();
        if (len == 0 || count == 0) {
            return "";
        }

        if (len == 1) {
            final char[] single = new char[count];
            Arrays.fill(single, s.charAt(0));
            return String.valueOf(single);
        }

        if (Integer.MAX_VALUE / count < len) {
            throw new OutOfMemoryError("Repeating " + len + " bytes String " + count +
                                           " times will produce a String exceeding maximum size.");
        }
        final int limit = len * count;
        final char[] multiple = new char[limit];
        System.arraycopy(s, 0, multiple, 0, len);
        int copied = len;
        for (; copied < limit - copied; copied <<= 1) {
            System.arraycopy(multiple, 0, multiple, copied, copied);
        }
        System.arraycopy(multiple, 0, multiple, copied, limit - copied);
        return String.valueOf(multiple);
    }
}
