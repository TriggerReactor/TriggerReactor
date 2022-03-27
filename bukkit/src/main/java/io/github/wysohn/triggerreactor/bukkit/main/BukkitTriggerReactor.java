package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitCommandSender;
import io.github.wysohn.triggerreactor.bukkit.components.BukkitPluginMainComponent;
import io.github.wysohn.triggerreactor.bukkit.components.DaggerBukkitPluginMainComponent;
import io.github.wysohn.triggerreactor.bukkit.main.serialize.BukkitConfigurationSerializer;
import io.github.wysohn.triggerreactor.bukkit.manager.event.TriggerReactorStartEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.event.TriggerReactorStopEvent;
import io.github.wysohn.triggerreactor.bukkit.modules.BukkitPluginMainModule;
import io.github.wysohn.triggerreactor.core.components.PluginMainComponent;
import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.main.CommandHandler;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.tools.ContinuingTasks;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public abstract class BukkitTriggerReactor extends JavaPlugin {
    private BukkitPluginMainComponent bukkitPluginMainComponent;
    private PluginMainComponent mainComponent;

    private TriggerReactor main;
    private CommandHandler commandHandler;
    private IWrapper wrapper;

    @Override
    public void onEnable() {
        String commandName = "triggerreactor";
        PluginCommand command = getCommand(commandName);
        Map<String, Command> rawCommands = getCommandMap();

        bukkitPluginMainComponent = DaggerBukkitPluginMainComponent.builder()
                .pluginMainModule(getModule(rawCommands))
                .commandName(commandName)
                .permission("triggerreactor.admin")
                .inject(rawCommands)
                .inject(command)
                .inject(getServer())
                .inject(this)
                .build();
        mainComponent = bukkitPluginMainComponent.getMainBuilder()
                .build();
        main = mainComponent.getMain();
        commandHandler = mainComponent.getCommandHandler();
        wrapper = mainComponent.getWrapper();

        command.setExecutor(this);
        command.setTabCompleter(this);

        try {
            main.onEnable();

            main.onReload();
        } catch (Exception ex) {
            ex.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getScheduler()
                .runTask(this, () -> getServer().getPluginManager().callEvent(new TriggerReactorStartEvent()));
    }

    protected abstract BukkitPluginMainModule getModule(Map<String, Command> rawCommands);

    @Override
    public void onDisable() {
        new ContinuingTasks.Builder().append(() -> getServer().getPluginManager()
                        .callEvent(new TriggerReactorStopEvent()))
                .append(() -> main.onDisable())
                .run(Throwable::printStackTrace);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandHandler.onCommand(wrapper.wrap(sender), command.getName(), args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return commandHandler.onTabComplete(new BukkitCommandSender(sender), args);
    }

    private Map<String, Command> getCommandMap() {
        try {
            Server server = Bukkit.getServer();

            Field f = server.getClass().getDeclaredField("commandMap");
            f.setAccessible(true);

            CommandMap scm = (CommandMap) f.get(server);

            Method knownCommands = scm.getClass().getDeclaredMethod("getKnownCommands");
            return (Map<String, Command>) knownCommands.invoke(scm);
        } catch (Exception ex) {
            if (main.isDebugging())
                ex.printStackTrace();

            getLogger().warning("Couldn't find 'commandMap'. This may indicate that you are using very very old"
                    + " version of Bukkit. Please report this to TR team, so we can work on it.");
            getLogger().warning("Use /trg debug to see more details.");
            return null;
        }
    }

    public BukkitBungeeCordHelper getBungeeHelper() {
        return bukkitPluginMainComponent.getBungeeCordHelper();
    }

    static {
        GsonConfigSource.registerSerializer(ConfigurationSerializable.class, new BukkitConfigurationSerializer());
        GsonConfigSource.registerValidator(obj -> obj instanceof ConfigurationSerializable);
    }
}
