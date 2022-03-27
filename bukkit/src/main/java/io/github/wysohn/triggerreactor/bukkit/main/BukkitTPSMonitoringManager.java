package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.tools.Lag;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;

public class BukkitTPSMonitoringManager extends Manager {
    @Inject
    Plugin plugin;
    @Inject
    Server server;

    private final Lag tpsHelper = new Lag();

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() throws Exception {
        server.getScheduler().runTaskTimer(plugin, tpsHelper, 0L, 20L);
    }

    @Override
    public void onReload() throws RuntimeException {

    }
}
