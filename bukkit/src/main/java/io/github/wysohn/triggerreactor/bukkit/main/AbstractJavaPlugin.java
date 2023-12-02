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

import com.google.inject.Module;
import com.google.inject.*;
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
import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.main.TRGCommandHandler;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.module.CorePluginModule;
import io.github.wysohn.triggerreactor.tools.ContinuingTasks;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import javax.inject.Named;
import java.io.File;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class AbstractJavaPlugin extends JavaPlugin {
    private TriggerReactorCore core;
    private Server server;
    private TRGCommandHandler TRGCommandHandler;

    private ScriptEditListener scriptEditListener;
    private PlayerLocationListener playerLocationListener;
    private ClickTriggerListener clickTriggerListener;
    private WalkTriggerListener walkTriggerListener;
    private InventoryTriggerListener inventoryTriggerListener;
    private AreaTriggerListener areaTriggerListener;
    private AreaSelectionListener areaSelectionListener;

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
            public Plugin providePlugin2() {
                return AbstractJavaPlugin.this;
            }

            @Provides
            public PluginDescriptionFile providePluginDescriptionFile() {
                return getDescription();
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
        server = injector.getInstance(Server.class);

        TRGCommandHandler = injector.getInstance(TRGCommandHandler.class);
        scriptEditListener = injector.getInstance(ScriptEditListener.class);
        playerLocationListener = injector.getInstance(PlayerLocationListener.class);
        clickTriggerListener = injector.getInstance(ClickTriggerListener.class);
        walkTriggerListener = injector.getInstance(WalkTriggerListener.class);
        inventoryTriggerListener = injector.getInstance(InventoryTriggerListener.class);
        areaTriggerListener = injector.getInstance(AreaTriggerListener.class);
        areaSelectionListener = injector.getInstance(AreaSelectionListener.class);

        bungeeHelper = injector.getInstance(BungeeCordHelper.class);
        mysqlHelper = injector.getInstance(MysqlSupport.class);

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

        initBungeeHelper();
        initMysql();

        // listeners
        server.getPluginManager().registerEvents(scriptEditListener, this);
        server.getPluginManager().registerEvents(playerLocationListener, this);

        server.getPluginManager().registerEvents(clickTriggerListener, this);
        server.getPluginManager().registerEvents(walkTriggerListener, this);
        server.getPluginManager().registerEvents(inventoryTriggerListener, this);

        server.getPluginManager().registerEvents(areaTriggerListener, this);
        server.getPluginManager().registerEvents(areaSelectionListener, this);

        // initiate core
        core.initialize();

        server.getScheduler().runTask(this, () -> server.getPluginManager().callEvent(new TriggerReactorStartEvent()));
    }

    private Thread bungeeConnectionThread;

    private void initBungeeHelper() {
        bungeeConnectionThread = new Thread(bungeeHelper);
        bungeeConnectionThread.setPriority(Thread.MIN_PRIORITY);
        bungeeConnectionThread.start();
    }

    private void initMysql() {
        FileConfiguration config = getConfig();
        if (config.getBoolean("Mysql.Enable", false)) {
            try {
                getLogger().info("Initializing Mysql support...");
                mysqlHelper.connect(config.getString("Mysql.Address"),
                        config.getString("Mysql.DbName"),
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
