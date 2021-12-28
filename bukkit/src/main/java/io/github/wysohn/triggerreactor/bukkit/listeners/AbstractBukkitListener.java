package io.github.wysohn.triggerreactor.bukkit.listeners;

import io.github.wysohn.triggerreactor.core.main.IPluginProcedure;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import javax.inject.Inject;
import javax.inject.Named;

public abstract class AbstractBukkitListener implements Listener, IPluginProcedure {
    @Inject
    @Named("PluginInstance")
    Object pluginInstance;
    @Inject
    PluginManager pluginManager;

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() throws Exception {
        pluginManager.registerEvents(this, (Plugin) pluginInstance);
    }

    @Override
    public void onReload() throws RuntimeException {

    }
}
