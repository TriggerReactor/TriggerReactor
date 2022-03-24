/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.main.serialize.BukkitConfigurationSerializer;
import io.github.wysohn.triggerreactor.bukkit.manager.event.TriggerReactorStartEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.event.TriggerReactorStopEvent;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.main.*;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.IInventoryModifier;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.PluginConfigManager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import io.github.wysohn.triggerreactor.tools.ContinuingTasks;
import io.github.wysohn.triggerreactor.tools.Lag;
import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class BukkitTriggerReactor implements IPluginProcedure {
    @Inject
    Logger logger;
    @Inject
    Server server;
    @Inject
    Plugin plugin;
    @Inject
    PluginCommand command;
    @Inject
    CommandExecutor commandExecutor;
    @Inject
    IWrapper wrapper;
    @Inject
    IGameController gameController;
    @Inject
    IInventoryModifier inventoryModifier;
    @Inject
    IPluginLifecycleController pluginLifecycle;
    
    @Inject
    TriggerReactorMain main;
    @Inject
    PluginConfigManager configManager;
    @Inject
    GlobalVariableManager globalVariableManager;
    @Inject
    NamedTriggerManager namedTriggerManager;

    private final Lag tpsHelper = new Lag();

    private BukkitBungeeCordHelper bungeeHelper;
    private BukkitMysqlSupport mysqlHelper;
    private Thread bungeeConnectionThread;

    @Inject
    public BukkitTriggerReactor() {

    }

    @Override
    public void onEnable() {
        command.setExecutor(commandExecutor);

        initBungeeHelper();
        initMysql();

        try {
            main.onEnable();

//            migrateOldConfig();

            main.onReload();
        } catch (Exception ex) {
            ex.printStackTrace();
            server.getPluginManager().disablePlugin(plugin);
            return;
        }

        server.getScheduler().runTaskTimer(plugin, tpsHelper, 0L, 20L);
        server.getScheduler()
                .runTask(plugin, () -> server.getPluginManager().callEvent(new TriggerReactorStartEvent()));
    }

    @Override
    public void onReload(){
        main.onReload();
    }

    @Override
    public void onDisable() {
        new ContinuingTasks.Builder().append(() -> server.getPluginManager().callEvent(new TriggerReactorStopEvent()))
                .append(() -> bungeeConnectionThread.interrupt())
                .append(() -> main.onDisable())
                .run(Throwable::printStackTrace);
    }

    private void initBungeeHelper() {
        bungeeHelper = new BukkitBungeeCordHelper(this);
        bungeeConnectionThread = new Thread(bungeeHelper);
        bungeeConnectionThread.setPriority(Thread.MIN_PRIORITY);
        bungeeConnectionThread.start();
    }

    private void initMysql() {
        if (configManager.get("Mysql.Enable", Boolean.class).orElse(false)) {
            try {
                logger.info("Initializing Mysql support...");
                mysqlHelper = new BukkitMysqlSupport(
                        configManager.get("Mysql.Address", String.class).orElse(null),
                        configManager.get("Mysql.DbName", String.class).orElse(null),
                        "data",
                        configManager.get("Mysql.UserName", String.class).orElse(null),
                        configManager.get("Mysql.Password", String.class).orElse(null));
                logger.info(mysqlHelper.toString());
                logger.info("Done!");
            } catch (SQLException e) {
                e.printStackTrace();
                logger.warning("Failed to initialize Mysql. Check for the error above.");
            }
        } else {
            String path = "Mysql.Enable";
            if (!configManager.has(path))
                configManager.put(path, false);
            path = "Mysql.Address";
            if (!configManager.has(path))
                configManager.put(path, "127.0.0.1:3306");
            path = "Mysql.DbName";
            if (!configManager.has(path))
                configManager.put(path, "TriggerReactor");
            path = "Mysql.UserName";
            if (!configManager.has(path))
                configManager.put(path, "root");
            path = "Mysql.Password";
            if (!configManager.has(path))
                configManager.put(path, "1234");
        }
    }

    public BukkitBungeeCordHelper getBungeeHelper() {
        return bungeeHelper;
    }

    public BukkitMysqlSupport getMysqlHelper() {
        return mysqlHelper;
    }

    public void registerEvents(Manager manager) {
        if (manager instanceof Listener)
            server.getPluginManager().registerEvents((Listener) manager, plugin);
    }

    public void addItemLore(IItemStack iS, String lore) {
        inventoryModifier.addItemLore(iS, lore);
    }

    public void callEvent(IEvent event) {
        gameController.callEvent(event);
    }

    public <T> Future<T> callSyncMethod(Callable<T> call) {
        return gameController.callSyncMethod(call);
    }

    public Object createEmptyPlayerEvent(ICommandSender sender) {
        return gameController.createEmptyPlayerEvent(sender);
    }

    public ProcessInterrupter createInterrupter(Map<UUID, Long> cooldowns) {
        return gameController.createInterrupter(cooldowns);
    }

    public ProcessInterrupter createInterrupterForInv(Map<UUID, Long> cooldowns,
                                                      Map<IInventory, InventoryTrigger> inventoryMap) {
        return gameController.createInterrupterForInv(cooldowns, inventoryMap);
    }

    public Object createPlayerCommandEvent(ICommandSender sender,
                                           String label, String[] args) {
        return gameController.createPlayerCommandEvent(sender, label, args);
    }

    public IPlayer extractPlayerFromContext(Object e) {
        return gameController.extractPlayerFromContext(e);
    }

    public ICommandSender getConsoleSender() {
        return gameController.getConsoleSender();
    }

    public Map<String, Object> getCustomVarsForTrigger(Object event) {
        return gameController.getCustomVarsForTrigger(event);
    }

    public IPlayer getPlayer(String string) {
        return gameController.getPlayer(string);
    }

    public boolean removeLore(IItemStack iS, int index) {
        return inventoryModifier.removeLore(iS, index);
    }

    public void setItemTitle(IItemStack iS, String title) {
        inventoryModifier.setItemTitle(iS, title);
    }

    public boolean setLore(IItemStack iS, int index, String lore) {
        return inventoryModifier.setLore(iS, index, lore);
    }

    public void showGlowStones(ICommandSender sender,
                               Set<Map.Entry<SimpleLocation, Trigger>> set) {
        gameController.showGlowStones(sender, set);
    }

    public Iterable<? extends IPlayer> getOnlinePlayers() {
        return gameController.getOnlinePlayers();
    }

    public Iterable<? extends IWorld> getWorlds() {
        return gameController.getWorlds();
    }

    public IInventory createInventory(int size, String name) {
        return inventoryModifier.createInventory(size, name);
    }

    public void disablePlugin() {
        pluginLifecycle.disablePlugin();
    }

    public String getAuthor() {
        return pluginLifecycle.getAuthor();
    }

    public <T> T getPlugin(String pluginName) {
        return pluginLifecycle.getPlugin(pluginName);
    }

    public String getPluginDescription() {
        return pluginLifecycle.getPluginDescription();
    }

    public String getVersion() {
        return pluginLifecycle.getVersion();
    }

    public boolean isDebugging() {
        return pluginLifecycle.isDebugging();
    }

    public void setDebugging(boolean bool) {
        pluginLifecycle.setDebugging(bool);
    }

    public boolean isEnabled(String pluginName) {
        return pluginLifecycle.isEnabled(pluginName);
    }

    public boolean isEnabled() {
        return pluginLifecycle.isEnabled();
    }

    static {
        GsonConfigSource.registerSerializer(ConfigurationSerializable.class, new BukkitConfigurationSerializer());
        GsonConfigSource.registerValidator(obj -> obj instanceof ConfigurationSerializable);
    }
}
