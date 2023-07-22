/*
 * Copyright (C) 2023. TriggerReactor Team
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

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Module;
import com.google.inject.*;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitCommandSender;
import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitPlayer;
import io.github.wysohn.triggerreactor.bukkit.main.serialize.BukkitConfigurationSerializer;
import io.github.wysohn.triggerreactor.bukkit.manager.AreaSelectionListener;
import io.github.wysohn.triggerreactor.bukkit.manager.PlayerLocationListener;
import io.github.wysohn.triggerreactor.bukkit.manager.ScriptEditListener;
import io.github.wysohn.triggerreactor.bukkit.manager.event.TriggerReactorStartEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.event.TriggerReactorStopEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.AreaTriggerListener;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.ClickTriggerListener;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.InventoryTriggerListener;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.WalkTriggerListener;
import io.github.wysohn.triggerreactor.bukkit.modules.BukkitExecutorModule;
import io.github.wysohn.triggerreactor.bukkit.modules.BukkitScriptEngineModule;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.main.TRGCommandHandler;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.module.CorePluginModule;
import io.github.wysohn.triggerreactor.tools.ContinuingTasks;
import io.github.wysohn.triggerreactor.tools.mysql.MiniConnectionPoolManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.inject.Named;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class AbstractJavaPlugin extends JavaPlugin {
    private TriggerReactorCore core;
    private TRGCommandHandler TRGCommandHandler;

    private ScriptEditListener scriptEditListener;
    private PlayerLocationListener playerLocationListener;
    private ClickTriggerListener clickTriggerListener;
    private WalkTriggerListener walkTriggerListener;
    private InventoryTriggerListener inventoryTriggerListener;
    private AreaTriggerListener areaTriggerListener;
    private AreaSelectionListener areaSelectionListener;

    private ScriptEngineManager scriptEngineManager;
    private BungeeCordHelper bungeeHelper;
    private MysqlSupport mysqlHelper;

    private Set<Manager> managers;

    BukkitTest test;

    /**
     * For test only.
     *
     * @param loader
     * @param description
     * @param dataFolder
     * @param file
     * @param modules
     * @deprecated For test only.
     */
    AbstractJavaPlugin(final JavaPluginLoader loader,
                       final PluginDescriptionFile description,
                       final File dataFolder,
                       final File file,
                       Module... modules) {
        super(loader, description, dataFolder, file);
        verifyPrecondition(init(modules));
    }

    protected AbstractJavaPlugin(Module... modules) {
        verifyPrecondition(init(modules));
    }

    public void verifyPrecondition(Injector injector) {
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.get(this) == null) {
                    throw new IllegalStateException("Field " + field.getName() + " is not initialized.");
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Injector init(Module[] modules) {
        List<Module> moduleList = Arrays.stream(modules).collect(Collectors.toList());
        moduleList.add(new CorePluginModule());
        moduleList.add(new BukkitExecutorModule());
        moduleList.add(new BukkitScriptEngineModule());
        moduleList.add(new AbstractModule() {
            @Provides
            @Named("DataFolder")
            public File provideDataFolder() {
                return getDataFolder();
            }

            @Provides
            @Named("PluginLogger")
            public Logger provideLogger() {
                return getLogger();
            }

            @Provides
            @Named("PluginClassLoader")
            public ClassLoader provideClassLoader() {
                return getClassLoader();
            }

            @Provides
            @Named("Plugin")
            public Object providePlugin() {
                return AbstractJavaPlugin.this;
            }

            @Provides
            public JavaPlugin javaPlugin() {
                return AbstractJavaPlugin.this;
            }

            @Provides
            public Server server() {
                return getServer();
            }
        });

        Injector injector = Guice.createInjector(moduleList);

        core = injector.getInstance(TriggerReactorCore.class);
        TRGCommandHandler = injector.getInstance(TRGCommandHandler.class);
        scriptEditListener = injector.getInstance(ScriptEditListener.class);
        playerLocationListener = injector.getInstance(PlayerLocationListener.class);
        clickTriggerListener = injector.getInstance(ClickTriggerListener.class);
        walkTriggerListener = injector.getInstance(WalkTriggerListener.class);
        inventoryTriggerListener = injector.getInstance(InventoryTriggerListener.class);
        areaTriggerListener = injector.getInstance(AreaTriggerListener.class);
        areaSelectionListener = injector.getInstance(AreaSelectionListener.class);

        managers = injector.getInstance(new Key<Set<Manager>>() {
        });

        test = injector.getInstance(BukkitTest.class);

        return injector;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Optional.ofNullable(this.getCommand("triggerreactor"))
                .ifPresent(command -> command.setExecutor(this));

        registerAPIs();
        initBungeeHelper();
        initMysql();

        // listeners
        Bukkit.getPluginManager().registerEvents(scriptEditListener, this);
        Bukkit.getPluginManager().registerEvents(playerLocationListener, this);

        Bukkit.getPluginManager().registerEvents(clickTriggerListener, this);
        Bukkit.getPluginManager().registerEvents(walkTriggerListener, this);
        Bukkit.getPluginManager().registerEvents(inventoryTriggerListener, this);

        Bukkit.getPluginManager().registerEvents(areaTriggerListener, this);
        Bukkit.getPluginManager().registerEvents(areaSelectionListener, this);

        // initiate core
        core.initialize();

        Bukkit.getScheduler().runTask(this, () -> Bukkit.getPluginManager().callEvent(new TriggerReactorStartEvent()));
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
                .append(core::shutdown)
                .run(Throwable::printStackTrace);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            return TRGCommandHandler.onCommand(
                    new BukkitPlayer((Player) sender),
                    command.getName(),
                    args);
        } else {
            return TRGCommandHandler.onCommand(
                    new BukkitCommandSender(sender),
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


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return TRGCommandHandler.onTabComplete(new BukkitCommandSender(sender),
                args);
    }

    public void registerEvents(Listener listener) {
        if (listener != null)
            Bukkit.getPluginManager().registerEvents(listener, this);
    }


    public ScriptEngineManager getScriptEngineManager() {
        if (scriptEngineManager == null) {
            scriptEngineManager = Bukkit.getServicesManager().load(ScriptEngineManager.class);
        }

        if (scriptEngineManager == null) {
            scriptEngineManager = new ScriptEngineManager(null);
        }

        return scriptEngineManager;
    }


    public class MysqlSupport {
        private final String KEY = "dbkey";
        private final String VALUE = "dbval";

        private final MysqlConnectionPoolDataSource ds;
        private final MiniConnectionPoolManager pool;

        private final String dbName;
        private final String tablename;

        private final String address;

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

    static class CommandSenderEvent extends Event {
        final CommandSender sender;

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
