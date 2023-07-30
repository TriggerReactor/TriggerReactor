package io.github.wysohn.triggerreactor.core.main;

import static io.github.wysohn.triggerreactor.core.util.ClassUtils.classExists;

public enum Dependency {

    Adventure("net.kyori.adventure.Adventure", Platform.Paper),
    MiniMessage("net.kyori.adventure.text.minimessage.MiniMessage", Platform.Paper);

    private final boolean classExists;
    private final Platform[] nativeSupportPlatforms;

    Dependency(final String classPath, final Platform... nativeSupportPlatforms) {
        this.classExists = classExists(classPath);
        this.nativeSupportPlatforms = nativeSupportPlatforms;
    }

    /**
     * Returns whether this dependency is supported or not.
     *
     * @return {@code true} if this dependency is supported, {@code false} otherwise
     */
    public boolean supports() {
        return this.classExists;
    }

    /**
     * Returns whether this dependency is supported by the specified platform.
     *
     * @param platform the platform
     * @return {@code true} if this dependency is supported by the specified platform, {@code false} otherwise
     */
    public boolean supportsBy(final Platform platform) {
        for (final Platform nativePlatform : nativeSupportPlatforms) {
            return nativePlatform == platform;
        }

        return false;
    }

}
