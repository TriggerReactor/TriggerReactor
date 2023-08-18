package io.github.wysohn.triggerreactor.core.util;

public final class ClassUtils {

    private ClassUtils() {
    }

    /**
     * Tests whether the given class exists or not in the class hierarchy.
     *
     * @param className the {@link Class#getCanonicalName() canonical name} of the class
     * @return {@code true} if the given class exists, {@code false} otherwise
     */
    public static boolean classExists(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
    }

}
