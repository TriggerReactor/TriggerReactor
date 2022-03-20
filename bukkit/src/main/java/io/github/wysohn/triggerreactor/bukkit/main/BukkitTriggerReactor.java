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
import io.github.wysohn.triggerreactor.bukkit.main.serialize.BukkitConfigurationSerializer;
import io.github.wysohn.triggerreactor.bukkit.manager.event.TriggerReactorStartEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.event.TriggerReactorStopEvent;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.main.*;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.PluginConfigManager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import io.github.wysohn.triggerreactor.tools.ContinuingTasks;
import io.github.wysohn.triggerreactor.tools.Lag;
import io.github.wysohn.triggerreactor.tools.mysql.MiniConnectionPoolManager;
import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.inject.Inject;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class BukkitTriggerReactor implements IPluginProcedure, IPluginLifecycleController {
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
    TriggerReactorMain main;
    @Inject
    PluginConfigManager configManager;
    @Inject
    GlobalVariableManager globalVariableManager;
    @Inject
    NamedTriggerManager namedTriggerManager;

    private final Lag tpsHelper = new Lag();

    private BungeeCordHelper bungeeHelper;
    private MysqlSupport mysqlHelper;
    private Thread bungeeConnectionThread;

    private boolean debugging = false;

    @Inject
    public BukkitTriggerReactor() {

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
        BukkitTriggerReactor.this.debugging = bool;
    }

    @Override
    public boolean isEnabled(String pluginName) {
        return server.getPluginManager().isPluginEnabled(pluginName);
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
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
        bungeeHelper = new BungeeCordHelper();
        bungeeConnectionThread = new Thread(bungeeHelper);
        bungeeConnectionThread.setPriority(Thread.MIN_PRIORITY);
        bungeeConnectionThread.start();
    }

    private void initMysql() {
        if (configManager.get("Mysql.Enable", Boolean.class).orElse(false)) {
            try {
                logger.info("Initializing Mysql support...");
                mysqlHelper = new MysqlSupport(
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

    public BungeeCordHelper getBungeeHelper() {
        return bungeeHelper;
    }

    public MysqlSupport getMysqlHelper() {
        return mysqlHelper;
    }

    public void registerEvents(Manager manager) {
        if (manager instanceof Listener)
            server.getPluginManager().registerEvents((Listener) manager, plugin);
    }

    public void addItemLore(IItemStack iS, String lore) {
        gameController.addItemLore(iS, lore);
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
        return gameController.removeLore(iS, index);
    }

    public void setItemTitle(IItemStack iS, String title) {
        gameController.setItemTitle(iS, title);
    }

    public boolean setLore(IItemStack iS, int index, String lore) {
        return gameController.setLore(iS, index, lore);
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
        return gameController.createInventory(size, name);
    }

    public class MysqlSupport {
        private final String KEY = "dbkey";
        private final String VALUE = "dbval";

        private final MysqlConnectionPoolDataSource ds;
        private final MiniConnectionPoolManager pool;

        private final String dbName;
        private final String tablename;

        private final String address;
        private final String CREATETABLEQUARY =
                "CREATE TABLE IF NOT EXISTS %s (" + KEY + " CHAR(128) PRIMARY KEY," + VALUE
                        + " MEDIUMBLOB)";

        private MysqlSupport(String address, String dbName, String tablename, String userName, String password) throws
                SQLException {
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

        private void initTable(Connection conn) throws SQLException {
            PreparedStatement pstmt = conn.prepareStatement(String.format(CREATETABLEQUARY, tablename));
            pstmt.executeUpdate();
            pstmt.close();
        }

        @Override
        public String toString() {
            return "Mysql Connection(" + address + ") to [dbName=" + dbName + ", tablename=" + tablename + "]";
        }

        public Object get(String key) throws SQLException {
            Object out = null;

            try (Connection conn = createConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT " + VALUE + " FROM " + tablename + " WHERE " + KEY + " = ?")) {
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
            server.getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
            server.getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, this);
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

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                Player player = Iterables.getFirst(BukkitUtil.getOnlinePlayers(), null);
                if (player == null)
                    return;

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(SUB_SERVERLIST);
                out.writeUTF("GetServers");
                player.sendPluginMessage(plugin, SUB_SERVERLIST, out.toByteArray());

                if (!playerCounts.isEmpty()) {
                    for (Map.Entry<String, Integer> entry : playerCounts.entrySet()) {
                        ByteArrayDataOutput out2 = ByteStreams.newDataOutput();
                        out2.writeUTF(SUB_USERCOUNT);
                        out2.writeUTF("PlayerCount");
                        out2.writeUTF(entry.getKey());
                        player.sendPluginMessage(plugin, SUB_USERCOUNT, out2.toByteArray());
                    }
                }

                try {
                    Thread.sleep(5 * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public int getPlayerCount(String serverName) {
            return playerCounts.getOrDefault(serverName, -1);
        }

        public String[] getServerNames() {
            String[] servers = playerCounts.keySet().toArray(new String[playerCounts.size()]);
            return servers;
        }

        public void sendToServer(Player player, String serverName) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(serverName);

            player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
        }
    }

    static {
        GsonConfigSource.registerSerializer(ConfigurationSerializable.class, new BukkitConfigurationSerializer());
        GsonConfigSource.registerValidator(obj -> obj instanceof ConfigurationSerializable);
    }
}
