package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;

public class BukkitPluginLifecycle implements IPluginLifecycleController {
    @Inject
    JavaPlugin plugin;
    @Inject
    Server server;

    private boolean debugging = false;

    @Inject
    public BukkitPluginLifecycle() {
    }

    @Override
    public void disablePlugin() {
        server.getPluginManager().disablePlugin(plugin);
    }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public <T> T getPlugin(String pluginName) {
        return (T) server.getPluginManager().getPlugin(pluginName);
    }

    @Override
    public String getPluginDescription() {
        return plugin.getDescription().getFullName();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean isDebugging() {
        return debugging;
    }

    @Override
    public void setDebugging(boolean bool) {
        debugging = bool;
    }

    @Override
    public boolean isEnabled(String pluginName) {
        return server.getPluginManager().isPluginEnabled(pluginName);
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }
}
