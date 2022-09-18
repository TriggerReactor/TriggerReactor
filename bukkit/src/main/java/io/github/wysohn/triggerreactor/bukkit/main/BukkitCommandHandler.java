package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.ICommandMapHandler;
import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.main.command.ICommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BukkitCommandHandler implements ICommandHandler {
    private final AbstractJavaPlugin plugin;
    private final ICommandMapHandler commandMapHandler;
    private final Map<String, Command> commandMap;
    private final Map<String, Command> overridens = new HashMap<>();

    public BukkitCommandHandler(AbstractJavaPlugin plugin, ICommandMapHandler commandMapHandler) {
        this.plugin = plugin;
        this.commandMapHandler = commandMapHandler;
        this.commandMap = commandMapHandler.getCommandMap();
    }

    @Override
    public ICommand register(String name, String[] aliases) {
        if(commandMap.containsKey(name) && overridens.containsKey(name))
            return null;

        PluginCommand command = createCommand(plugin, name);

        Optional.ofNullable(commandMap.get(name))
                .ifPresent(c -> overridens.put(name, c));
        commandMap.put(name, command);
        // register aliases manually here
        for (String alias : aliases) {
            Optional.ofNullable(commandMap.get(alias))
                    .ifPresent(c -> overridens.put(alias, c));
            commandMap.put(alias, command);
        }

        commandMapHandler.synchronizeCommandMap();
        return new BukkitCommand(command);
    }

    @Override
    public boolean unregister(String name) {
        Command command = commandMap.remove(name);
        if (command == null)
            return false;

        if (overridens.containsKey(name))
            commandMap.put(name, overridens.remove(name));
        else
            commandMap.remove(name);

        // also un-register aliases manually here
        for (String alias : command.getAliases()) {
            if (overridens.containsKey(alias))
                commandMap.put(alias, overridens.remove(alias));
            else
                commandMap.remove(alias);
        }

        return true;
    }

    @Override
    public void unregisterAll() {
        commandMap.clear();
    }

    @Override
    public void sync() {
        commandMapHandler.synchronizeCommandMap();
    }

    private static PluginCommand createCommand(AbstractJavaPlugin plugin, String commandName) {
        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            return c.newInstance(commandName, plugin);
        } catch (Exception ex) {
            if (plugin.core.isDebugging())
                ex.printStackTrace();

            plugin.getLogger().warning("Couldn't construct 'PluginCommand'. This may indicate that you are using very very old" +
                                             " version of Bukkit. Please report this to TR team, so we can work on it.");
            plugin.getLogger().warning("Use /trg debug to see more details.");
            return null;
        }
    }
}
