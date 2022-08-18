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
import io.github.wysohn.triggerreactor.bukkit.main.serialize.BukkitConfigurationSerializer;
import io.github.wysohn.triggerreactor.bukkit.manager.event.TriggerReactorStartEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.event.TriggerReactorStopEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.ICommandMapHandler;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.bukkit.tools.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.bukkit.tools.migration.InvTriggerMigrationHelper;
import io.github.wysohn.triggerreactor.bukkit.tools.migration.NaiveMigrationHelper;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.ContinuingTasks;
import io.github.wysohn.triggerreactor.tools.mysql.MiniConnectionPoolManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.script.ScriptEngineManager;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public abstract class AbstractJavaPlugin extends JavaPlugin implements ICommandMapHandler {
    public final BukkitTriggerReactorCore core;

    private ScriptEngineManager scriptEngineManager;
    private BungeeCordHelper bungeeHelper;
    private MysqlSupport mysqlHelper;

    public AbstractJavaPlugin() {
        core = new BukkitTriggerReactorCore();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        PluginCommand trg = this.getCommand("triggerreactor");
        trg.setExecutor(this);

        registerAPIs();
        initBungeeHelper();
        initMysql();

        core.onCoreEnable(this);
        migrateOldConfig();

        for (Manager manager : Manager.getManagers()) {
            manager.reload();
        }

        Bukkit.getScheduler().runTask(this, () -> Bukkit.getPluginManager().callEvent(new TriggerReactorStartEvent()));
    }

    private void migrateOldConfig() {
        new ContinuingTasks.Builder()
                .append(() -> {
                    if (core.getPluginConfigManager().isMigrationNeeded()) {
                        core.getPluginConfigManager().migrate(new NaiveMigrationHelper(getConfig(),
                                new File(getDataFolder(), "config.yml")));
                    }
                })
                .append(() -> {
                    if (core.getVariableManager().isMigrationNeeded()) {
                        File file = new File(getDataFolder(), "var.yml");
                        FileConfiguration conf = new Utf8YamlConfiguration();
                        try {
                            conf.load(file);
                        } catch (IOException | InvalidConfigurationException e) {
                            e.printStackTrace();
                        }
                        core.getVariableManager().migrate(new NaiveMigrationHelper(conf, file));
                    }
                })
                .append(() -> {
                    Optional.of(core.getInvManager())
                            .map(AbstractTriggerManager::getTriggerInfos)
                            .ifPresent(triggerInfos -> Arrays.stream(triggerInfos)
                                    .filter(TriggerInfo::isMigrationNeeded)
                                    .forEach(triggerInfo -> {
                                        File folder = triggerInfo.getSourceCodeFile().getParentFile();
                                        File oldFile = new File(folder, triggerInfo.getTriggerName() + ".yml");
                                        FileConfiguration oldFileConfig = YamlConfiguration.loadConfiguration(oldFile);
                                        triggerInfo.migrate(new InvTriggerMigrationHelper(oldFile, oldFileConfig));
                                    }));
                })
                .append(() -> {
                    Manager.getManagers().stream()
                            .filter(AbstractTriggerManager.class::isInstance)
                            .map(AbstractTriggerManager.class::cast)
                            .map(AbstractTriggerManager::getTriggerInfos)
                            .forEach(triggerInfos -> Arrays.stream(triggerInfos)
                                    .filter(TriggerInfo::isMigrationNeeded)
                                    .forEach(triggerInfo -> {
                                        File folder = triggerInfo.getSourceCodeFile().getParentFile();
                                        File oldFile = new File(folder, triggerInfo.getTriggerName() + ".yml");
                                        FileConfiguration oldFileConfig = YamlConfiguration.loadConfiguration(oldFile);
                                        triggerInfo.migrate(new NaiveMigrationHelper(oldFileConfig, oldFile));
                                    }));
                })
                .run();
    }

    protected abstract void registerAPIs();

    private Thread bungeeConnectionThread;

    private void initBungeeHelper() {
        bungeeHelper = new BungeeCordHelper();
        bungeeConnectionThread = new Thread(bungeeHelper);
        bungeeConnectionThread.setPriority(Thread.MIN_PRIORITY);
        bungeeConnectionThread.start();
    }

    private void initMysql() {
        FileConfiguration config = getConfig();
        if (config.getBoolean("Mysql.Enable", false)) {
            try {
                getLogger().info("Initializing Mysql support...");
                mysqlHelper = new MysqlSupport(config.getString("Mysql.Address"),
                        config.getString("Mysql.DbName"),
                        "data",
                        config.getString("Mysql.UserName"),
                        config.getString("Mysql.Password"));
                getLogger().info(mysqlHelper.toString());
                getLogger().info("Done!");
            } catch (SQLException e) {
                e.printStackTrace();
                getLogger().warning("Failed to initialize Mysql. Check for the error above.");
            }
        } else {
            String path = "Mysql.Enable";
            if (!config.isSet(path))
                config.set(path, false);
            path = "Mysql.Address";
            if (!config.isSet(path))
                config.set(path, "127.0.0.1:3306");
            path = "Mysql.DbName";
            if (!config.isSet(path))
                config.set(path, "TriggerReactor");
            path = "Mysql.UserName";
            if (!config.isSet(path))
                config.set(path, "root");
            path = "Mysql.Password";
            if (!config.isSet(path))
                config.set(path, "1234");

            saveConfig();
        }
    }

    @Override
    public void onDisable() {
        new ContinuingTasks.Builder()
                .append(() -> Bukkit.getPluginManager().callEvent(new TriggerReactorStopEvent()))
                .append(() -> bungeeConnectionThread.interrupt())
                .append(() -> core.onCoreDisable(this))
                .run(Throwable::printStackTrace);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            return this.core.onCommand(
                    BukkitTriggerReactorCore.WRAPPER.wrap((Player) sender),
                    command.getName(),
                    args);
        } else {
            return this.core.onCommand(
                    BukkitTriggerReactorCore.WRAPPER.wrap(sender),
                    command.getName(),
                    args);
        }
    }

    public File getJarFile() {
        return super.getFile();
    }

    public BungeeCordHelper getBungeeHelper() {
        return bungeeHelper;
    }

    public MysqlSupport getMysqlHelper() {
        return mysqlHelper;
    }

    public abstract SelfReference getSelfReference();

    private final Set<Class<? extends Manager>> savings = new HashSet<>();

    public boolean saveAsynchronously(final Manager manager) {
        if (savings.contains(manager.getClass()))
            return false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (savings) {
                        savings.add(manager.getClass());
                    }

                    getLogger().info("Saving " + manager.getClass().getSimpleName());
                    manager.saveAll();
                    getLogger().info("Saving Done!");
                } catch (Exception e) {
                    e.printStackTrace();
                    getLogger().warning("Failed to save " + manager.getClass().getSimpleName());
                } finally {
                    synchronized (savings) {
                        savings.remove(manager.getClass());
                    }
                }
            }
        }) {{
            this.setPriority(MIN_PRIORITY);
        }}.start();
        return true;
    }

    public boolean isDebugging() {
        return this.core.isDebugging();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return io.github.wysohn.triggerreactor.core.main.TriggerReactorCore.onTabComplete(new BukkitCommandSender(sender), args);
    }

    public void showGlowStones(ICommandSender sender, Set<Map.Entry<SimpleLocation, Trigger>> set) {
        for (Map.Entry<SimpleLocation, Trigger> entry : set) {
            SimpleLocation sloc = entry.getKey();
            Player player = sender.get();
            player.sendBlockChange(
                    new Location(Bukkit.getWorld(sloc.getWorld()), sloc.getX(), sloc.getY(), sloc.getZ()),
                    Material.GLOWSTONE, (byte) 0);
        }
    }

    public void registerEvents(Listener listener) {
        if (listener != null)
            Bukkit.getPluginManager().registerEvents(listener, this);
    }

    public void runTask(Runnable runnable) {
        Bukkit.getScheduler().runTask(this, runnable);
    }

    private ProcessInterrupter.Builder newInterrupterBuilder() {
        return ProcessInterrupter.Builder.begin()
                .perExecutor((context, command, args) -> {
                    if ("CALL".equalsIgnoreCase(command)) {
                        if (args.length < 1)
                            throw new RuntimeException("Need parameter [String] or [String, boolean]");

                        if (args[0] instanceof String) {
                            Trigger trigger = core.getNamedTriggerManager().get((String) args[0]);
                            if (trigger == null)
                                throw new RuntimeException("No trigger found for Named Trigger " + args[0]);

                            boolean sync = true;
                            if (args.length > 1 && args[1] instanceof Boolean) {
                                sync = (boolean) args[1];
                            }

                            if (sync) {
                                trigger.activate(context.getTriggerCause(), context.getVars(), true);
                            } else {//use snapshot to avoid concurrent modification
                                trigger.activate(context.getTriggerCause(), new HashMap<>(context.getVars()), false);
                            }

                            return true;
                        } else {
                            throw new RuntimeException("Parameter type not match; it should be a String."
                                    + " Make sure to put double quotes, if you provided String literal.");
                        }
                    }

                    return false;
                })
                .perExecutor((context, command, args) -> {
                    if ("CANCELEVENT".equalsIgnoreCase(command)) {
                        if(!core.isServerThread())
                            throw new RuntimeException("Trying to cancel event in async trigger.");

                        if (context.getTriggerCause() instanceof Cancellable) {
                            ((Cancellable) context.getTriggerCause()).setCancelled(true);
                            return true;
                        } else {
                            throw new RuntimeException(context.getTriggerCause() + " is not a Cancellable event!");
                        }
                    }

                    return false;
                });
    }

    private ProcessInterrupter.Builder appendCooldownInterrupter(ProcessInterrupter.Builder builder, Map<UUID, Long> cooldowns) {
        return builder.perExecutor(((context, command, args) -> {
            if ("COOLDOWN".equalsIgnoreCase(command)) {
                if (!(args[0] instanceof Number))
                    throw new RuntimeException(args[0] + " is not a number!");

                if (context.getTriggerCause() instanceof PlayerEvent) {
                    long mills = (long) (((Number) args[0]).doubleValue() * 1000L);
                    Player player = ((PlayerEvent) context.getTriggerCause()).getPlayer();
                    UUID uuid = player.getUniqueId();
                    cooldowns.put(uuid, System.currentTimeMillis() + mills);
                }
                return true;
            }

            return false;
        })).perPlaceholder((context, placeholder, args) -> {
//            if ("cooldown".equals(placeholder)) {
//                if (context.getTriggerCause() instanceof PlayerEvent) {
//                    return cooldowns.getOrDefault(((PlayerEvent) context.getTriggerCause()).getPlayer().getUniqueId(), 0L);
//                } else {
//                    return 0;
//                }
//            }
            return null;
        });
    }

    public ProcessInterrupter createInterrupter(Map<UUID, Long> cooldowns) {
        return appendCooldownInterrupter(newInterrupterBuilder(), cooldowns)
                .build();
    }

    public ProcessInterrupter createInterrupterForInv(Map<UUID, Long> cooldowns,
                                                      Map<IInventory, InventoryTrigger> inventoryMap) {
        return appendCooldownInterrupter(newInterrupterBuilder(), cooldowns)
                .perNode((context, node) -> {
                    //safety feature to stop all trigger immediately if executing on 'open' or 'click'
                    //  is still running after the inventory is closed.
                    if (context.getTriggerCause() instanceof InventoryOpenEvent
                            || context.getTriggerCause() instanceof InventoryClickEvent) {
                        Inventory inv = ((InventoryEvent) context.getTriggerCause()).getInventory();

                        //it's not GUI so stop execution
                        return !inventoryMap.containsKey(BukkitTriggerReactorCore.getWrapper().wrap(inv));
                    }

                    return false;
                })
                .build();
    }

    public IPlayer extractPlayerFromContext(Object e) {
        if (e instanceof PlayerEvent) {
            Player player = ((PlayerEvent) e).getPlayer();
            return BukkitTriggerReactorCore.WRAPPER.wrap(player);
        } else if (e instanceof InventoryInteractEvent) {
            HumanEntity he = ((InventoryInteractEvent) e).getWhoClicked();
            if (he instanceof Player)
                return BukkitTriggerReactorCore.WRAPPER.wrap((Player) he);
        }

        return null;
    }

    public <T> Future<T> callSyncMethod(Callable<T> call) {
        try {
            return Bukkit.getScheduler().callSyncMethod(this, call);
        } catch (Exception e) {
        }
        return null;
    }

    public void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public void callEvent(IEvent event) {
        Bukkit.getPluginManager().callEvent(event.get());
    }

    public IPlayer getPlayer(String string) {
        Player player = Bukkit.getPlayer(string);
        if (player != null)
            return BukkitTriggerReactorCore.WRAPPER.wrap(player);
        else
            return null;
    }

    public ICommandSender getConsoleSender() {
        return BukkitTriggerReactorCore.WRAPPER.wrap(Bukkit.getConsoleSender());
    }

    public Object createEmptyPlayerEvent(ICommandSender sender) {
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

    public Object createPlayerCommandEvent(ICommandSender sender, String label, String[] args) {
        Object unwrapped = sender.get();

        StringBuilder builder = new StringBuilder("/");
        builder.append(label);
        for (String arg : args) {
            builder.append(' ');
            builder.append(arg);
        }

        if (unwrapped instanceof Player) {
            return new PlayerCommandPreprocessEvent((Player) unwrapped, builder.toString());
        } else {
            throw new RuntimeException("Cannot create empty PlayerCommandPreprocessEvent for " + sender);
        }
    }

    public void setItemTitle(IItemStack iS, String title) {
        ItemStack IS = iS.get();
        ItemMeta IM = IS.getItemMeta();
        IM.setDisplayName(title);
        IS.setItemMeta(IM);
    }

    public void addItemLore(IItemStack iS, String lore) {
        ItemStack IS = iS.get();

        ItemMeta IM = IS.getItemMeta();
        List<String> lores = IM.hasLore() ? IM.getLore() : new ArrayList<>();
        lores.add(lore);
        IM.setLore(lores);
        IS.setItemMeta(IM);
    }

    public boolean setLore(IItemStack iS, int index, String lore) {
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

    public boolean removeLore(IItemStack iS, int index) {
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

    public boolean isServerThread() {
        boolean result = false;

        synchronized (this) {
            result = Bukkit.isPrimaryThread();
        }

        return result;
    }

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

    public ScriptEngineManager getScriptEngineManager() {
        if(scriptEngineManager == null)
            scriptEngineManager = Bukkit.getServicesManager().load(ScriptEngineManager.class);

        if(scriptEngineManager == null)
            scriptEngineManager = new ScriptEngineManager();

        return scriptEngineManager;
    }

    public Iterable<IWorld> getWorlds() {
        return Bukkit.getWorlds().stream()
                .map(BukkitTriggerReactorCore.WRAPPER::wrap)
                .collect(Collectors.toList());
    }

    public class MysqlSupport {
        private final String KEY = "dbkey";
        private final String VALUE = "dbval";

        private final MysqlConnectionPoolDataSource ds;
        private final MiniConnectionPoolManager pool;

        private final String dbName;
        private final String tablename;

        private final String address;

        private MysqlSupport(String address, String dbName, String tablename, String userName, String password) throws SQLException {
            this.dbName = dbName;
            this.tablename = tablename;
            this.address = address;

            ds = new MysqlConnectionPoolDataSource();
            ds.setURL("jdbc:mysql://" + address + "/" + dbName);
            ds.setUser(userName);
            ds.setPassword(password);
            ds.setCharacterEncoding("UTF-8");
            ds.setUseUnicode(true);
            ds.setAutoReconnectForPools(true);
            ds.setAutoReconnect(true);
            ds.setAutoReconnectForConnectionPools(true);

            ds.setCachePreparedStatements(true);
            ds.setCachePrepStmts(true);

            pool = new MiniConnectionPoolManager(ds, 2);

            Connection conn = createConnection();
            initTable(conn);
            conn.close();
        }

        private Connection createConnection() {
            Connection conn = null;

            try {
                conn = pool.getConnection();
            } catch (SQLException e) {
                // e.printStackTrace();
            } finally {
                if (conn == null)
                    conn = pool.getValidConnection();
            }

            return conn;
        }

        private final String CREATETABLEQUARY = "" + "CREATE TABLE IF NOT EXISTS %s (" + "" + KEY
                + " CHAR(128) PRIMARY KEY," + "" + VALUE + " MEDIUMBLOB" + ")";

        private void initTable(Connection conn) throws SQLException {
            PreparedStatement pstmt = conn.prepareStatement(String.format(CREATETABLEQUARY, tablename));
            pstmt.executeUpdate();
            pstmt.close();
        }

        public Object get(String key) throws SQLException {
            Object out = null;

            try (Connection conn = createConnection();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT " + VALUE + " FROM " + tablename + " WHERE " + KEY + " = ?")) {
                pstmt.setString(1, key);
                ResultSet rs = pstmt.executeQuery();

                if (!rs.next())
                    return null;
                InputStream is = rs.getBinaryStream(VALUE);

                try (ObjectInputStream ois = new ObjectInputStream(is)) {
                    out = ois.readObject();
                } catch (IOException | ClassNotFoundException e1) {
                    e1.printStackTrace();
                    return null;
                }
            }

            return out;
        }

        public void set(String key, Serializable value) throws SQLException {
            try (Connection conn = createConnection();
                 PreparedStatement pstmt = conn.prepareStatement("REPLACE INTO " + tablename + " VALUES (?, ?)")) {


                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                    oos.writeObject(value);

                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

                    pstmt.setString(1, key);
                    pstmt.setBinaryStream(2, bais);

                    pstmt.executeUpdate();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public String toString() {
            return "Mysql Connection(" + address + ") to [dbName=" + dbName + ", tablename=" + tablename + "]";
        }
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
            Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(AbstractJavaPlugin.this, CHANNEL);
            Bukkit.getServer().getMessenger().registerIncomingPluginChannel(AbstractJavaPlugin.this, CHANNEL, this);
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
                for (Map.Entry<String, Integer> entry : playerCounts.entrySet()) {
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

            player.sendPluginMessage(AbstractJavaPlugin.this, CHANNEL, out.toByteArray());
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
                player.sendPluginMessage(AbstractJavaPlugin.this, SUB_SERVERLIST, out.toByteArray());

                if (!playerCounts.isEmpty()) {
                    for (Map.Entry<String, Integer> entry : playerCounts.entrySet()) {
                        ByteArrayDataOutput out2 = ByteStreams.newDataOutput();
                        out2.writeUTF(SUB_USERCOUNT);
                        out2.writeUTF("PlayerCount");
                        out2.writeUTF(entry.getKey());
                        player.sendPluginMessage(AbstractJavaPlugin.this, SUB_USERCOUNT, out2.toByteArray());
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

    static {
        GsonConfigSource.registerSerializer(ConfigurationSerializable.class, new BukkitConfigurationSerializer());
        GsonConfigSource.registerValidator(obj -> obj instanceof ConfigurationSerializable);
    }
}
