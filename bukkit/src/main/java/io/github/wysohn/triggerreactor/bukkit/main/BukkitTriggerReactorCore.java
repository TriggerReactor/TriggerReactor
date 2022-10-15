/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.bridge.AbstractBukkitWrapper;
import io.github.wysohn.triggerreactor.bukkit.manager.*;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.AreaTriggerListener;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.ClickTriggerListener;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.InventoryTriggerListener;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.WalkTriggerListener;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.core.main.IEventRegistry;
import io.github.wysohn.triggerreactor.core.main.IInventoryHandle;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.main.command.ICommandHandler;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.*;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.Lag;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Abstract class to reduce writing boilerplate codes in latest and legacy bukkit project.
 * <p>
 * Try <b>not</b> to import Bukkit related classes in here for sake of code cohesiveness
 * if it's not necessary.
 * (Put them in the AbstractJavaPlugin or its child instead. Plugin class is exception since
 * the BukkitTriggerReactorCore wants to act as delegate class of JavaPlugin)
 *
 * @author wysohn
 */
public class BukkitTriggerReactorCore extends TriggerReactorCore implements Plugin {
    protected static AbstractBukkitWrapper WRAPPER = null;
    private io.github.wysohn.triggerreactor.bukkit.main.AbstractJavaPlugin bukkit;
    private ScriptEngineManager sem;
    private Lag tpsHelper;
    private ExecutorManager executorManager;
    private PlaceholderManager placeholderManager;
    private AbstractScriptEditManager scriptEditManager;
    private AbstractPlayerLocationManager locationManager;
    private AbstractPermissionManager permissionManager;
    private AreaSelectionManager selectionManager;
    private AbstractInventoryEditManager invEditManager;
    private ClickTriggerManager clickManager;
    private WalkTriggerManager walkManager;
    private CommandTriggerManager cmdManager;
    private InventoryTriggerManager invManager;
    private AreaTriggerManager areaManager;
    private CustomTriggerManager customManager;
    private RepeatingTriggerManager repeatManager;
    private NamedTriggerManager namedTriggerManager;
    private ICommandHandler commandHandler;
    private IEventRegistry eventRegistry;
    private IInventoryHandle<ItemStack> inventoryHandle;

    public static AbstractBukkitWrapper getWrapper() {
        return WRAPPER;
    }

    @Override
    public SelfReference getSelfReference() {
        return bukkit.getSelfReference();
    }

    @Override
    public ExecutorManager getExecutorManager() {
        return executorManager;
    }

    @Override
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    @Override
    public AbstractScriptEditManager getScriptEditManager() {
        return scriptEditManager;
    }

    @Override
    public AbstractPlayerLocationManager getLocationManager() {
        return locationManager;
    }

    @Override
    public AbstractPermissionManager getPermissionManager() {
        return permissionManager;
    }

    @Override
    public AreaSelectionManager getSelectionManager() {
        return selectionManager;
    }

    @Override
    public AbstractInventoryEditManager getInvEditManager() {
        return invEditManager;
    }

    @Override
    public LocationBasedTriggerManager<ClickTrigger> getClickManager() {
        return clickManager;
    }

    @Override
    public LocationBasedTriggerManager<WalkTrigger> getWalkManager() {
        return walkManager;
    }

    @Override
    public CommandTriggerManager getCmdManager() {
        return cmdManager;
    }

    @Override
    public InventoryTriggerManager getInvManager() {
        return invManager;
    }

    @Override
    public AreaTriggerManager getAreaManager() {
        return areaManager;
    }

    @Override
    public CustomTriggerManager getCustomManager() {
        return customManager;
    }

    @Override
    public RepeatingTriggerManager getRepeatManager() {
        return repeatManager;
    }

    @Override
    public NamedTriggerManager getNamedTriggerManager() {
        return namedTriggerManager;
    }

    @Override
    public ICommandHandler getCommandHandler() {
        return commandHandler;
    }

    @Override
    public IEventRegistry getEventRegistry() {
        return eventRegistry;
    }

    @Override
    public IInventoryHandle<?> getInventoryHandle() {
        return inventoryHandle;
    }

    public AbstractJavaPlugin.BungeeCordHelper getBungeeHelper() {
        return bukkit.getBungeeHelper();
    }

    public Lag getTpsHelper() {
        return tpsHelper;
    }

    public AbstractJavaPlugin.MysqlSupport getMysqlHelper() {
        return bukkit.getMysqlHelper();
    }

    public void onCoreEnable(AbstractJavaPlugin plugin) {
        Thread.currentThread().setContextClassLoader(plugin.getClass().getClassLoader());
        this.bukkit = plugin;
        ValidationUtil.notNull(WRAPPER);

        super.onCoreEnable();

        for (Entry<String, Class<? extends AbstractAPISupport>> entry : APISupport.getSharedVars().entrySet()) {
            AbstractAPISupport.addSharedVar(sharedVars, entry.getKey(), entry.getValue());
        }

        sem = bukkit.getScriptEngineManager();
        try {
            ScriptEngineInitializer.initScriptEngine(sem);
            initScriptEngine(sem);
        } catch (ScriptException e) {
            initFailed(e);
            return;
        }

        try {
            executorManager = new ExecutorManager(this, sem, new HashMap<String, Executor>(){{
                put("CMDOP", (timing, variables, e, args) -> {
                    Object player = variables.get("player");
                    if (!(player instanceof Player))
                        return null;

                    DispatchCommandAsOP call = new DispatchCommandAsOP((Player) player, String.valueOf(args[0]));
                    if (plugin.isServerThread()) {
                        call.call();
                    } else {
                        try {
                            plugin.callSyncMethod(call).get();
                        } catch (Exception ex) {
                            //to double check
                            call.deOpIfWasNotOp();
                        }
                    }

                    return null;
                });
            }});
        } catch (IOException e) {
            initFailed(e);
            return;
        }

        try {
            placeholderManager = new PlaceholderManager(this, sem);
        } catch (IOException e) {
            initFailed(e);
            return;
        }

        try {
            commandHandler = new BukkitCommandHandler(plugin, plugin);
        } catch (Exception e) {
            initFailed(e);
            return;
        }

        try {
            inventoryHandle = new BukkitInventoryHandle();
        } catch (Exception e) {
            initFailed(e);
            return;
        }

        try {
            eventRegistry = new BukkitEventRegistry();
        } catch (Exception e) {
            initFailed(e);
            return;
        }

        // managers
        scriptEditManager = new ScriptEditManager(this);
        locationManager = new PlayerLocationManager(this);
        permissionManager = new PermissionManager(this);
        selectionManager = new AreaSelectionManager(this);
        invEditManager = new InventoryEditManager(this);

        clickManager = new ClickTriggerManager(this);
        walkManager = new WalkTriggerManager(this);
        cmdManager = new CommandTriggerManager(this, commandHandler);
        invManager = new InventoryTriggerManager<>(this, inventoryHandle);
        areaManager = new AreaTriggerManager(this, this, this);
        customManager = new CustomTriggerManager(this, eventRegistry);
        repeatManager = new RepeatingTriggerManager(this, this);

        namedTriggerManager = new NamedTriggerManager(this);

        // listeners
        bukkit.registerEvents(new ClickTriggerListener(clickManager));
        bukkit.registerEvents(new WalkTriggerListener(walkManager));
        bukkit.registerEvents(new InventoryTriggerListener(invManager));

        bukkit.registerEvents(new AreaTriggerListener(areaManager));


        bukkit.registerEvents(new AreaSelectionListener(selectionManager));

        // TODO: Once managers are refactored, this should be removed.
        Manager.getManagers().stream()
                .filter(Listener.class::isInstance)
                .forEach(listener -> bukkit.registerEvents((Listener) listener));

        // etc
        tpsHelper = new Lag();
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(50L * 100);

                    while (isAlive() && !isInterrupted()) {
                        submitSync(() -> {
                            tpsHelper.run();
                            return null;
                        }).get();
                        Thread.sleep(50L);
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    getLogger().info("TPS Helper stopped working." + ex);
                }

            }
        }.start();
    }

    private void initScriptEngine(ScriptEngineManager sem) {
        sem.put("plugin", this);

        for (Entry<String, AbstractAPISupport> entry : this.getSharedVars().entrySet()) {
            sem.put(entry.getKey(), entry.getValue());
        }

        sem.put("get", new Function<String, Object>() {
            @Override
            public Object apply(String t) {
                return getVariableManager().get(t);
            }
        });

        sem.put("put", new BiFunction<String, Object, Void>() {
            @Override
            public Void apply(String a, Object b) {
                if (!GlobalVariableManager.isValidName(a))
                    throw new RuntimeException("[" + a + "] cannot be used as key");

                if (a != null && b == null) {
                    getVariableManager().remove(a);
                } else {
                    try {
                        getVariableManager().put(a, b);
                    } catch (Exception e) {
                        throw new RuntimeException("Executor -- put(" + a + "," + b + ")", e);
                    }
                }

                return null;
            }
        });

        sem.put("has", new Function<String, Boolean>() {
            @Override
            public Boolean apply(String t) {
                return getVariableManager().has(t);
            }
        });
    }

    private void initFailed(Exception e) {
        e.printStackTrace();
        getLogger().severe("Initialization failed!");
        getLogger().severe(e.getMessage());
        disablePlugin();
    }

    public void onCoreDisable(AbstractJavaPlugin plugin) {
        super.onCoreDisable();

        getLogger().info("Finalizing the scheduled script executions...");
        CACHED_THREAD_POOL.shutdown();
        getLogger().info("Shut down complete!");
    }

    @Override
    protected void sendCommandDesc(ICommandSender sender, String command, String desc) {
        sender.sendMessage("&b" + command + " &8- &7" + desc);
    }

    @Override
    protected void sendDetails(ICommandSender sender, String detail) {
        sender.sendMessage("  &7" + detail);
    }

    @Override
    public String getPluginDescription() {
        return bukkit.getDescription().getFullName();
    }

    @Override
    public String getVersion() {
        return bukkit.getDescription().getVersion();
    }

    @Override
    public String getAuthor() {
        return bukkit.getDescription().getAuthors().toString();
    }

    @Override
    public void showGlowStones(ICommandSender sender, Set<Entry<SimpleLocation, Trigger>> set) {
        bukkit.showGlowStones(sender, set);
    }

    @Override
    public File getDataFolder() {
        return bukkit.getDataFolder();
    }

    @Override
    public Logger getLogger() {
        return bukkit.getLogger();
    }

    @Override
    public boolean isEnabled() {
        return bukkit.isEnabled();
    }

    @Override
    public <T> T getMain() {
        return (T) bukkit;
    }

    @Override
    public boolean isConfigSet(String key) {
        return bukkit.getConfig().isSet(key);
    }

    @Override
    public void setConfig(String key, Object value) {
        bukkit.getConfig().set(key, value);
    }

    @Override
    public Object getConfig(String key) {
        return bukkit.getConfig().get(key);
    }

    @Override
    public <T> T getConfig(String key, T def) {
        return (T) bukkit.getConfig().get(key, def);
    }

    @Override
    public void saveConfig() {
        bukkit.saveConfig();
    }

    @Override
    public void reloadConfig() {
        bukkit.reloadConfig();
    }

    @Override
    public void runTask(Runnable runnable) {
        bukkit.runTask(runnable);
    }

    @Override
    public void saveAsynchronously(Manager manager) {
        bukkit.saveAsynchronously(manager);
    }

    @Override
    public ProcessInterrupter createInterrupter(Map<UUID, Long> cooldowns) {
        return bukkit.createInterrupter(cooldowns);
    }

    @Override
    public ProcessInterrupter createInterrupterForInv(Map<UUID, Long> cooldowns,
                                                      Map<IInventory, InventoryTrigger> inventoryMap) {
        return bukkit.createInterrupterForInv(cooldowns, inventoryMap);
    }

    @Override
    public IPlayer extractPlayerFromContext(Object e) {
        return bukkit.extractPlayerFromContext(e);
    }

    @Override
    public <T> Future<T> callSyncMethod(Callable<T> call) {
        return bukkit.callSyncMethod(call);
    }

    @Override
    public void disablePlugin() {
        bukkit.disablePlugin();
    }

    @Override
    public void callEvent(IEvent event) {
        bukkit.callEvent(event);
    }

    @Override
    public IPlayer getPlayer(String string) {
        return bukkit.getPlayer(string);
    }

    @Override
    public Object createEmptyPlayerEvent(ICommandSender sender) {
        return bukkit.createEmptyPlayerEvent(sender);
    }

    @Override
    public Object createPlayerCommandEvent(ICommandSender sender, String label, String[] args) {
        return bukkit.createPlayerCommandEvent(sender, label, args);
    }

    @Override
    public Iterable<IWorld> getWorlds() {
        return bukkit.getWorlds();
    }

    @Override
    public IWorld getWorld(String world) {
        return bukkit.getWorld(world);
    }

    @Override
    protected void setItemTitle(IItemStack iS, String title) {
        bukkit.setItemTitle(iS, title);
    }

    @Override
    protected void addItemLore(IItemStack iS, String lore) {
        bukkit.addItemLore(iS, lore);
    }

    @Override
    protected boolean setLore(IItemStack iS, int index, String lore) {
        return bukkit.setLore(iS, index, lore);
    }

    @Override
    protected boolean removeLore(IItemStack iS, int index) {
        return bukkit.removeLore(iS, index);
    }

    @Override
    public boolean isServerThread() {
        return bukkit.isServerThread();
    }

    @Override
    public Thread newThread(Runnable runnable, String name, int priority) {
        Thread thread = new Thread(runnable, name);
        thread.setPriority(priority);
        return thread;
    }

    @Override
    public Map<String, Object> getCustomVarsForTrigger(Object e) {
        return bukkit.getCustomVarsForTrigger(e);
    }

    @Override
    public ICommandSender getConsoleSender() {
        return bukkit.getConsoleSender();
    }

    //DO NOT TOUCH AREA
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //The codes below are merely to implement the 'Plugin' class and not meant to do anything other than
    //delegating the methods to AbstractJavaPlugin. These methods are usually not called since we use JavaPlugin
    // to implement these methods, and they will be in the AbstractJavaPlugin or its children classes
    //TriggerCore class must implement Plugin so that it can be treated as Plugin; when javascript code
    // (Executors and Placeholders) calls the method which requires Plugin argument, we can simply use
    // 'plugin' variable inside the javascript since 'plugin' is indeed a Plugin since we implement it.
    //For example, the Bukkit API's scheduler method (such as #runTask(Plugin, Runnable)) requires Plugin
    //instance specifically, and we can do that directly using the 'plugin' variable, which is in fact
    //TriggerReactorCore, without doing extra works like 'plugin.bukkit'.
    //
    //This extra work is due to JavaPlugin being an abstract class, so child class cannot extend both JavaPlugin
    //and TriggerReactorCore at the same time. Plus, Bukkit's class loader only accepts JavaPlugin, not Plugin.
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return bukkit.onTabComplete(sender, command, alias, args);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return bukkit.onCommand(sender, command, label, args);
    }

    @Override
    public PluginDescriptionFile getDescription() {
        return bukkit.getDescription();
    }

    @Override
    public FileConfiguration getConfig() {
        return bukkit.getConfig();
    }

    @Override
    public InputStream getResource(String filename) {
        return bukkit.getResource(filename);
    }

    @Override
    public void saveDefaultConfig() {
        bukkit.saveDefaultConfig();
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        bukkit.saveResource(resourcePath, replace);
    }

    @Override
    public PluginLoader getPluginLoader() {
        return bukkit.getPluginLoader();
    }

    @Override
    public Server getServer() {
        return bukkit.getServer();
    }

    @Override
    public void onDisable() {
        bukkit.onDisable();
    }

    @Override
    public void onLoad() {
        bukkit.onLoad();
    }

    @Override
    public void onEnable() {
        bukkit.onEnable();
    }

    @Override
    public boolean isNaggable() {
        return bukkit.isNaggable();
    }

    @Override
    public void setNaggable(boolean canNag) {
        bukkit.setNaggable(canNag);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return bukkit.getDefaultWorldGenerator(worldName, id);
    }

    @Override
    public String getName() {
        return bukkit.getName();
    }
}
