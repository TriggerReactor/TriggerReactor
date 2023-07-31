package io.github.wysohn.triggerreactor.core.manager;

import com.google.inject.Inject;
import io.github.wysohn.triggerreactor.core.main.Platform;

import javax.inject.Singleton;

import static io.github.wysohn.triggerreactor.core.util.ClassUtils.classExists;

@Singleton
public class PlatformManager extends Manager {
    private Platform currentPlatform = Platform.Unknown;

    @Inject
    public PlatformManager() {

    }

    @Override
    public void initialize() {
        currentPlatform = getCurrentPlatform();
    }

    @Override
    public void reload() {

    }

    @Override
    public void shutdown() {

    }

    public Platform current() {
        return currentPlatform;
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
