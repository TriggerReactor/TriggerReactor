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

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitCommandSender;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitInventory;
import io.github.wysohn.triggerreactor.bukkit.manager.*;
import io.github.wysohn.triggerreactor.bukkit.manager.event.TriggerReactorStartEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.event.TriggerReactorStopEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.*;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.IWrapper;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.config.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.config.IMigrationHelper;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.*;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter.ProcessInterrupter;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.Lag;
import io.github.wysohn.triggerreactor.tools.mysql.MiniConnectionPoolManager;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.plugin.Plugin;

import javax.script.ScriptException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * Abstract class to reduce writing boilerplate codes in latest and legacy bukkit project.
 * 
 * Try <b>not</b> to import Bukkit related classes in here for sake of code cohesiveness
 *  if it's not necessary.
 * (Put them in the AbstractJavaPlugin or its child instead. Plugin class is exception since
 *  the BukkitTriggerReactorCore wants to act as delegate class of JavaPlugin)
 * 
 * @author wysohn
 *
 */
public class BukkitTriggerReactorCore extends TriggerReactorCore implements Plugin {
    private io.github.wysohn.triggerreactor.bukkit.main.AbstractJavaPlugin bukkit;

    private BungeeCordHelper bungeeHelper;
    private Lag tpsHelper;

    private AbstractExecutorManager executorManager;
    private AbstractPlaceholderManager placeholderManager;
    private AbstractVariableManager variableManager;
    private AbstractScriptEditManager scriptEditManager;
    private AbstractPlayerLocationManager locationManager;
    private AbstractPermissionManager permissionManager;
    private AbstractAreaSelectionManager selectionManager;

    private AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.ClickTrigger> clickManager;
    private AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.WalkTrigger> walkManager;
    private AbstractCommandTriggerManager cmdManager;
    private AbstractInventoryTriggerManager invManager;
    private AbstractAreaTriggerManager areaManager;
    private AbstractCustomTriggerManager customManager;
    private AbstractRepeatingTriggerManager repeatManager;

    private AbstractNamedTriggerManager namedTriggerManager;

    @Override
    public SelfReference getSelfReference() {
        return bukkit.createSelfReference();
    }
    
    @Override
	public IWrapper getWrapper() {
		return bukkit.createWrapper();
	}

	@Override
    public AbstractExecutorManager getExecutorManager() {
        return executorManager;
    }

    @Override
    public AbstractPlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    @Override
    public AbstractVariableManager getVariableManager() {
        return variableManager;
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
    public AbstractAreaSelectionManager getSelectionManager() {
        return selectionManager;
    }

    @Override
    public AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.ClickTrigger> getClickManager() {
        return clickManager;
    }

    @Override
    public AbstractLocationBasedTriggerManager<AbstractLocationBasedTriggerManager.WalkTrigger> getWalkManager() {
        return walkManager;
    }

    @Override
    public AbstractCommandTriggerManager getCmdManager() {
        return cmdManager;
    }

    @Override
    public AbstractInventoryTriggerManager getInvManager() {
        return invManager;
    }

    @Override
    public AbstractAreaTriggerManager getAreaManager() {
        return areaManager;
    }

    @Override
    public AbstractCustomTriggerManager getCustomManager() {
        return customManager;
    }

    @Override
    public AbstractRepeatingTriggerManager getRepeatManager() {
        return repeatManager;
    }

    @Override
    public AbstractNamedTriggerManager getNamedTriggerManager() {
        return namedTriggerManager;
    }

    public BungeeCordHelper getBungeeHelper() {
        return bungeeHelper;
    }

    public Lag getTpsHelper() {
        return tpsHelper;
    }

    public AbstractJavaPlugin.MysqlSupport getMysqlHelper() {
        return bukkit.getMysqlHelper();
    }

    private Thread bungeeConnectionThread;

    public void onEnable(AbstractJavaPlugin plugin) {
        Thread.currentThread().setContextClassLoader(plugin.getClass().getClassLoader());

        File file = new File(getDataFolder(), "config.yml");
        getConfigManager().setMigrationHelper(new IMigrationHelper() {
            private void traversal(String parentNode, Map<String, Object> map, BiConsumer<String, Object> consumer) {
                map.forEach(((s, o) -> {
//                    if (o instanceof ConfigurationSection) {
//                        Map<String, Object> section = ((ConfigurationSection) o).getValues(false);
//                        if (parentNode == null) {
//                            traversal(s, section, consumer);
//                        } else {
//                            traversal(parentNode + "." + s, section, consumer);
//                        }
//                    } else {
//                        consumer.accept(s, o);
//                    }
                }));
            }

            @Override
            public void migrate(IConfigSource current) {
//                FileConfiguration config = getConfig();
//
//                traversal(null, config.getValues(false), current::put);
//
//                if (file.exists())
//                    file.renameTo(new File(file.getParentFile(), "config.yml.bak"));
            }
        });

        this.bukkit = plugin;

        for (Entry<String, Class<? extends AbstractAPISupport>> entry : APISupport.getSharedVars().entrySet()) {
            AbstractAPISupport.addSharedVar(sharedVars, entry.getKey(), entry.getValue());
        }

        try {
            executorManager = new ExecutorManager(this);
        } catch (ScriptException | IOException e) {
            initFailed(e);
            return;
        }

        try {
            placeholderManager = new PlaceholderManager(this);
        } catch (ScriptException | IOException e) {
            initFailed(e);
            return;
        }

        try {
            variableManager = new VariableManager(this);
        } catch (Exception e) {
            initFailed(e);
            return;
        }

        scriptEditManager = new ScriptEditManager(this);
        locationManager = new PlayerLocationManager(this);
        permissionManager = new PermissionManager(this);
        selectionManager = new AreaSelectionManager(this);

        clickManager = new ClickTriggerManager(this);
        walkManager = new WalkTriggerManager(this);
        cmdManager = new CommandTriggerManager(this);
        invManager = new InventoryTriggerManager(this);
        areaManager = new AreaTriggerManager(this);
        customManager = new CustomTriggerManager(this);
        repeatManager = new RepeatingTriggerManager(this);

        namedTriggerManager = new NamedTriggerManager(this);

        for (Manager manager : Manager.getManagers()) {
            manager.reload();
        }

        bungeeHelper = new BungeeCordHelper();
        bungeeConnectionThread = new Thread(bungeeHelper);
        bungeeConnectionThread.setPriority(Thread.MIN_PRIORITY);
        bungeeConnectionThread.start();

        tpsHelper = new Lag();
        new Thread() {
        	@Override
        	public void run() {
        		try {
        			Thread.sleep(50L * 100);
                	
                	while(isAlive() && !isInterrupted()) {
                		submitSync(()->{
                			tpsHelper.run();
                			return null;
                		}).get();
                		Thread.sleep(50L);
                	}	
        		}catch(ExecutionException | InterruptedException ex) {
        			getLogger().info("TPS Helper stopped working." + ex);
        		}
        		
        	}
        }.start();

        System.setProperty("bstats.relocatecheck", "false");
        MetricsLite metrics = new MetricsLite(this);
    }

    private void initFailed(Exception e) {
        e.printStackTrace();
        getLogger().severe("Initialization failed!");
        getLogger().severe(e.getMessage());
        disablePlugin();
    }

    public void onDisable(AbstractJavaPlugin plugin) {
        getLogger().info("Finalizing the scheduled script executions...");
        cachedThreadPool.shutdown();
        bungeeConnectionThread.interrupt();
        getLogger().info("Shut down complete!");
    }

    @Override
    protected void sendCommandDesc(ICommandSender sender, String command, String desc) {
        sender.sendMessage(ChatColor.AQUA + command + " " + ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + desc);
    }

    @Override
    protected void sendDetails(ICommandSender sender, String detail) {
        detail = ChatColor.translateAlternateColorCodes('&', detail);
        sender.sendMessage("  " + ChatColor.GRAY + detail);
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
    @SuppressWarnings("deprecation")
	public void showGlowStones(ICommandSender sender, Set<Entry<SimpleLocation, Trigger>> set) {
        for (Entry<SimpleLocation, Trigger> entry : set) {
            SimpleLocation sloc = entry.getKey();
            Player player = sender.get();
            player.sendBlockChange(
                    new Location(Bukkit.getWorld(sloc.getWorld()), sloc.getX(), sloc.getY(), sloc.getZ()),
                    Material.GLOWSTONE, (byte) 0);
        }
    }

    @Override
    public void registerEvents(Manager manager) {
        if (manager instanceof Listener)
            Bukkit.getPluginManager().registerEvents((Listener) manager, this.bukkit);
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
        Bukkit.getScheduler().runTask(bukkit, runnable);
    }

    @Override
    public void saveAsynchronously(Manager manager) {
        bukkit.saveAsynchronously(manager);
    }

    @Override
    public ProcessInterrupter createInterrupter(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns) {
        return new ProcessInterrupter() {
            @Override
            public boolean onNodeProcess(Node node) {
                return false;
            }

            @Override
            public boolean onCommand(Object context, String command, Object[] args) {
                if ("CALL".equalsIgnoreCase(command)) {
                    if (args.length < 1)
                        throw new RuntimeException("Need parameter [String] or [String, boolean]");

                    if (args[0] instanceof String) {
                        Trigger trigger = getNamedTriggerManager().getTriggerForName((String) args[0]);
                        if (trigger == null)
                            throw new RuntimeException("No trigger found for Named Trigger " + args[0]);

                        if (args.length > 1 && args[1] instanceof Boolean) {
                            trigger.setSync((boolean) args[1]);
                        } else {
                            trigger.setSync(true);
                        }

                        if (trigger.isSync()) {
                            trigger.activate(e, interpreter.getVars());
                        } else {//use snapshot to avoid concurrent modification
                            trigger.activate(e, new HashMap<>(interpreter.getVars()));
                        }

                        return true;
                    } else {
                        throw new RuntimeException("Parameter type not match; it should be a String."
                                + " Make sure to put double quotes, if you provided String literal.");
                    }
                } else if ("CANCELEVENT".equalsIgnoreCase(command)) {
                    if (!interpreter.isSync())
                        throw new RuntimeException("CANCELEVENT is illegal in async mode!");

                    if (context instanceof Cancellable) {
                        ((Cancellable) context).setCancelled(true);
                        return true;
                    } else {
                        throw new RuntimeException(context + " is not a Cancellable event!");
                    }
                } else if ("COOLDOWN".equalsIgnoreCase(command)) {
                    if (!(args[0] instanceof Number))
                        throw new RuntimeException(args[0] + " is not a number!");

                    if (e instanceof PlayerEvent) {
                        long mills = (long) (((Number) args[0]).doubleValue() * 1000L);
                        Player player = ((PlayerEvent) e).getPlayer();
                        UUID uuid = player.getUniqueId();
                        cooldowns.put(uuid, System.currentTimeMillis() + mills);
                    }
                    return true;
                }

                return false;
            }

            @Override
            public Object onPlaceholder(Object context, String placeholder, Object[] args) {
//                if("cooldown".equals(placeholder)){
//                    if(e instanceof PlayerEvent){
//                        return cooldowns.getOrDefault(((PlayerEvent) e).getPlayer().getUniqueId(), 0L);
//                    }else{
//                        return 0;
//                    }
//                }else{
//                    return null;
//                }
                return null;
            }
        };
    }

    @Override
    public ProcessInterrupter createInterrupterForInv(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns,
                                                      Map<IInventory, InventoryTrigger> inventoryMap) {
        return new ProcessInterrupter() {
            @Override
            public boolean onNodeProcess(Node node) {
                //safety feature to stop all trigger immediately if executing on 'open' or 'click'
                //  is still running after the inventory is closed.
                if (e instanceof InventoryOpenEvent
                        || e instanceof InventoryClickEvent) {
                    Inventory inv = ((InventoryEvent) e).getInventory();

                    //it's not GUI so stop execution
                    return !inventoryMap.containsKey(new BukkitInventory(inv));
                }

                return false;
            }

            @Override
            public boolean onCommand(Object context, String command, Object[] args) {
                if ("CALL".equalsIgnoreCase(command)) {
                    if (args.length < 1)
                        throw new RuntimeException("Need parameter [String] or [String, boolean]");

                    if (args[0] instanceof String) {
                        Trigger trigger = getNamedTriggerManager().getTriggerForName((String) args[0]);
                        if (trigger == null)
                            throw new RuntimeException("No trigger found for Named Trigger " + args[0]);

                        if (args.length > 1 && args[1] instanceof Boolean) {
                            trigger.setSync((boolean) args[1]);
                        } else {
                            trigger.setSync(true);
                        }

                        if (trigger.isSync()) {
                            trigger.activate(e, interpreter.getVars());
                        } else {//use snapshot to avoid concurrent modification
                            trigger.activate(e, new HashMap<>(interpreter.getVars()));
                        }

                        return true;
                    } else {
                        throw new RuntimeException("Parameter type not match; it should be a String."
                                + " Make sure to put double quotes, if you provided String literal.");
                    }
                } else if ("CANCELEVENT".equalsIgnoreCase(command)) {
                    if (!interpreter.isSync())
                        throw new RuntimeException("CANCELEVENT is illegal in async mode!");

                    if (context instanceof Cancellable) {
                        ((Cancellable) context).setCancelled(true);
                        return true;
                    } else {
                        throw new RuntimeException(context + " is not a Cancellable event!");
                    }
                } else if ("COOLDOWN".equalsIgnoreCase(command)) {
                    if (!(args[0] instanceof Number))
                        throw new RuntimeException(args[0] + " is not a number!");

                    if (e instanceof PlayerEvent) {
                        long mills = (long) (((Number) args[0]).doubleValue() * 1000L);
                        Player player = ((PlayerEvent) e).getPlayer();
                        UUID uuid = player.getUniqueId();
                        cooldowns.put(uuid, System.currentTimeMillis() + mills);
                    }
                    return true;
                }

                return false;
            }

            @Override
            public Object onPlaceholder(Object context, String placeholder, Object[] args) {
//                if("cooldown".equals(placeholder)){
//                    if(e instanceof PlayerEvent){
//                        return cooldowns.getOrDefault(((PlayerEvent) e).getPlayer().getUniqueId(), 0L);
//                    }else{
//                        return 0L;
//                    }
//                }else{
//                    return null;
//                }
                return null;
            }

        };
    }

    @Override
    public IPlayer extractPlayerFromContext(Object e) {
        if (e instanceof PlayerEvent) {
            Player player = ((PlayerEvent) e).getPlayer();
            return bukkit.createWrapper().wrap(player);
        } else if (e instanceof InventoryInteractEvent) {
            HumanEntity he = ((InventoryInteractEvent) e).getWhoClicked();
            if (he instanceof Player)
                return bukkit.createWrapper().wrap((Player) he);
        }

        return null;
    }

    public class BungeeCordHelper implements PluginMessageListener, Runnable {
        private final String CHANNEL = "BungeeCord";

        private final String SUB_SERVERLIST = "ServerList";
        private final String SUB_USERCOUNT = "UserCount";

        private final Map<String, Integer> playerCounts = new ConcurrentHashMap<>();

        /**
         * constructor should only be called from onEnable()
         */
        private BungeeCordHelper() {
            Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(bukkit, CHANNEL);
            Bukkit.getServer().getMessenger().registerIncomingPluginChannel(bukkit, CHANNEL, this);
        }

        @Override
        public void onPluginMessageReceived(String channel, Player player, byte[] message) {
            if (!channel.equals(CHANNEL)) {
                return;
            }

            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subchannel = in.readUTF();
            if (subchannel.equals(SUB_SERVERLIST)) {
                String[] serverList = in.readUTF().split(", ");
                Set<String> serverListSet = Sets.newHashSet(serverList);

                for (String server : serverListSet) {
                    if (!playerCounts.containsKey(server))
                        playerCounts.put(server, -1);
                }

                Set<String> deleteServer = new HashSet<>();
                for (Entry<String, Integer> entry : playerCounts.entrySet()) {
                    if (!serverListSet.contains(entry.getKey()))
                        deleteServer.add(entry.getKey());
                }

                for (String delete : deleteServer) {
                    playerCounts.remove(delete);
                }
            } else if (subchannel.equals(SUB_USERCOUNT)) {
                String server = in.readUTF(); // Name of server, as given in the arguments
                int playercount = in.readInt();

                playerCounts.put(server, playercount);
            }
        }

        public void sendToServer(Player player, String serverName) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(serverName);

            player.sendPluginMessage(bukkit, CHANNEL, out.toByteArray());
        }

        public String[] getServerNames() {
            String[] servers = playerCounts.keySet().toArray(new String[playerCounts.size()]);
            return servers;
        }

        public int getPlayerCount(String serverName) {
            return playerCounts.getOrDefault(serverName, -1);
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                Player player = Iterables.getFirst(BukkitUtil.getOnlinePlayers(), null);
                if (player == null)
                    return;

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(SUB_SERVERLIST);
                out.writeUTF("GetServers");
                player.sendPluginMessage(bukkit, SUB_SERVERLIST, out.toByteArray());

                if (!playerCounts.isEmpty()) {
                    for (Entry<String, Integer> entry : playerCounts.entrySet()) {
                        ByteArrayDataOutput out2 = ByteStreams.newDataOutput();
                        out2.writeUTF(SUB_USERCOUNT);
                        out2.writeUTF("PlayerCount");
                        out2.writeUTF(entry.getKey());
                        player.sendPluginMessage(bukkit, SUB_USERCOUNT, out2.toByteArray());
                    }
                }

                try {
                    Thread.sleep(5 * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public <T> Future<T> callSyncMethod(Callable<T> call) {
        try {
            return Bukkit.getScheduler().callSyncMethod(bukkit, call);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(bukkit);
    }

    @Override
    public void callEvent(IEvent event) {
        Bukkit.getPluginManager().callEvent(event.get());
    }

    @Override
    protected IPlayer getPlayer(String string) {
        Player player = Bukkit.getPlayer(string);
        if (player != null)
            return bukkit.createWrapper().wrap(player);
        else
            return null;
    }

    @Override
    protected Object createEmptyPlayerEvent(ICommandSender sender) {
        Object unwrapped = sender.get();

        if (unwrapped instanceof Player) {
            return new PlayerEvent((Player) unwrapped) {
                @Override
                public HandlerList getHandlers() {
                    return null;
                }
            };
        } else if (unwrapped instanceof CommandSender) {
            return new CommandSenderEvent((CommandSender) unwrapped);
        } else {
            throw new RuntimeException("Cannot create empty PlayerEvent for " + sender);
        }
    }

    @Override
    protected void setItemTitle(IItemStack iS, String title) {
        ItemStack IS = iS.get();
        ItemMeta IM = IS.getItemMeta();
        IM.setDisplayName(title);
        IS.setItemMeta(IM);
    }

    @Override
    protected void addItemLore(IItemStack iS, String lore) {
        ItemStack IS = iS.get();

        ItemMeta IM = IS.getItemMeta();
        List<String> lores = IM.hasLore() ? IM.getLore() : new ArrayList<>();
        lores.add(lore);
        IM.setLore(lores);
        IS.setItemMeta(IM);
    }

    @Override
    protected boolean setLore(IItemStack iS, int index, String lore) {
        ItemStack IS = iS.get();

        ItemMeta IM = IS.getItemMeta();
        List<String> lores = IM.hasLore() ? IM.getLore() : new ArrayList<>();
        if (lore == null || index < 0 || index > lores.size() - 1)
            return false;

        lores.set(index, lore);
        IM.setLore(lores);
        IS.setItemMeta(IM);

        return true;
    }

    @Override
    protected boolean removeLore(IItemStack iS, int index) {
        ItemStack IS = iS.get();

        ItemMeta IM = IS.getItemMeta();
        List<String> lores = IM.getLore();
        if (lores == null || index < 0 || index > lores.size() - 1)
            return false;

        lores.remove(index);
        IM.setLore(lores);
        IS.setItemMeta(IM);

        return true;
    }

    @Override
    public boolean isServerThread() {
        boolean result = false;

        synchronized (this) {
            result = Bukkit.isPrimaryThread();
        }

        return result;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return bukkit.onTabComplete(sender, command, alias, args);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return super.onCommand(new BukkitCommandSender(sender), command.getName(), args);
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

    @Override
    public Map<String, Object> getCustomVarsForTrigger(Object e) {
        Map<String, Object> variables = new HashMap<String, Object>();
        //this should be fine as script loosely check the variable type
        if (e instanceof CommandSenderEvent) {
            variables.put("player", ((CommandSenderEvent) e).sender);
        } else if (e instanceof PlayerEvent) {
            variables.put("player", ((PlayerEvent) e).getPlayer());
        } else if (e instanceof InventoryInteractEvent) {
            if (((InventoryInteractEvent) e).getWhoClicked() instanceof Player)
                variables.put("player", ((InventoryInteractEvent) e).getWhoClicked());
        } else if (e instanceof InventoryCloseEvent) {
            if (((InventoryCloseEvent) e).getPlayer() instanceof Player)
                variables.put("player", ((InventoryCloseEvent) e).getPlayer());
        } else if (e instanceof InventoryOpenEvent) {
            if (((InventoryOpenEvent) e).getPlayer() instanceof Player)
                variables.put("player", ((InventoryOpenEvent) e).getPlayer());
        } else if (e instanceof PlayerDeathEvent) {
            variables.put("player", ((PlayerDeathEvent) e).getEntity());
        } else if (e instanceof EntityEvent) { // Some EntityEvent use entity field to store Player instance.
            Entity entity = ((EntityEvent) e).getEntity();
            variables.put("entity", entity);

            if (entity instanceof Player) {
                variables.put("player", entity);
            }
        } else if (e instanceof BlockEvent) {
            variables.put("block", ((BlockEvent) e).getBlock());

            try {
                Method m = e.getClass().getMethod("getPlayer");
                variables.put("player", m.invoke(e));
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e1) {
                return variables;
            }
        }

        return variables;
    }

    @Override
    public ICommandSender getConsoleSender() {
        return new BukkitCommandSender(Bukkit.getConsoleSender());
    }

    private class CommandSenderEvent extends Event {
        private final CommandSender sender;

        public CommandSenderEvent(CommandSender sender) {
            super();
            this.sender = sender;
        }

        @Override
        public HandlerList getHandlers() {
            return null;
        }

    }
}
