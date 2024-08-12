package io.github.wysohn.triggerreactor.core.manager;

import com.google.inject.Inject;
import io.github.wysohn.triggerreactor.core.main.Platform;

import javax.inject.Singleton;

import static io.github.wysohn.triggerreactor.core.util.ClassUtils.classExists;

@Singleton
public class PlatformManager extends Manager {
    private Platform currentPlatform = null;

    @Inject
    public PlatformManager() {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void reload() {

    }

    @Override
    public void shutdown() {

    }

    /**
     * Obtains the current running platform type for the server implementation.
     *
     * @return the current running platform type
     */
    public Platform getCurrentPlatform() {
        if (currentPlatform == null) {
            if (classExists("org.spongepowered.api.Sponge")) {
                currentPlatform = Platform.Sponge;
            } else if (classExists("io.papermc.paper.plugin.loader.PluginLoader") || classExists("com.destroystokyo.paper.utils.PaperPluginLogger")) {
                currentPlatform = Platform.Paper;
            } else if (classExists("org.spigotmc.SpigotConfig")) {
                currentPlatform = Platform.Spigot;
            } else if (classExists("org.bukkit.craftbukkit.CraftServer") || classExists("org.bukkit.craftbukkit.Main")) {
                currentPlatform = Platform.CraftBukkit;
            } else {
                currentPlatform = Platform.Unknown;
            }
        }

        return currentPlatform;
    }
}
