package io.github.wysohn.triggerreactor.tools;

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
        if (paths.length < 2) return paths[0];

        StringBuilder builder = new StringBuilder(paths[0]);
        for (int i = 1; i < paths.length; i++) {
            builder.append('.');
            builder.append(paths[i]);
        }

        return builder.toString();
    }
}
