/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.tools;

import java.util.Arrays;
import java.util.regex.Pattern;

public class StringUtils {
    public static final Pattern NAME_PATTERN = Pattern.compile("^[0-9a-zA-Z_]+$");
    public static final Pattern DECIMAL_PATTERN = Pattern.compile("^[0-9]+.[0-9]{0,}$");
    public static final Pattern INTEGER_PATTERN = Pattern.compile("^[0-9]+$");

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
        System.arraycopy(s.toCharArray(), 0, multiple, 0, len);
        int copied = len;
        for (; copied < limit - copied; copied <<= 1) {
            System.arraycopy(multiple, 0, multiple, copied, copied);
        }
        System.arraycopy(multiple, 0, multiple, copied, limit - copied);
        return String.valueOf(multiple);
    }
}