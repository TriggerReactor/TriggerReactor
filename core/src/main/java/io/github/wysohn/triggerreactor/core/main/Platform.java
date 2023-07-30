package io.github.wysohn.triggerreactor.core.main;

import static io.github.wysohn.triggerreactor.core.util.ClassUtils.classExists;

public enum Platform {

    CraftBukkit,
    Spigot,
    Paper,
    Sponge,
    Unknown;

    public boolean isBukkit() {
        return this == CraftBukkit || this == Spigot || this == Paper;
    }

    public boolean isSponge() {
        return this == Sponge;
    }

    public boolean isKnown() {
        return this != Unknown;
    }

    public boolean supports(final Dependency dependency) {
        return dependency.supportsBy(this) || dependency.supports();
    }

    private static final Platform CURRENT = getCurrentPlatform();
    public static Platform current() {
        return CURRENT;
    }

    /**
     * Obtains the current running platform type for the server implementation.
     *
     * @return the current running platform type
     */
    public static Platform getCurrentPlatform() {
        if (classExists("org.spongepowered.api.Sponge")) {
            return Platform.Sponge;
        } else if (classExists("io.papermc.paper.plugin.loader.PluginLoader") || classExists("com.destroystokyo.paper.utils.PaperPluginLogger")) {
            return Platform.Paper;
        } else if (classExists("org.spigotmc.SpigotConfig")) {
            return Platform.Spigot;
        } else if (classExists("org.bukkit.craftbukkit.CraftServer") || classExists("org.bukkit.craftbukkit.Main")) {
            return Platform.CraftBukkit;
        }

        return Platform.Unknown;
    }

}
