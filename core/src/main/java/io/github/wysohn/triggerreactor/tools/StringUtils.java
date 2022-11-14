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
}
