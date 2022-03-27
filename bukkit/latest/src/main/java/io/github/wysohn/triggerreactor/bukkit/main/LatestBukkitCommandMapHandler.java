package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitCommand;
import io.github.wysohn.triggerreactor.core.bridge.ICommand;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandMapHandler;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LatestBukkitCommandMapHandler implements ICommandMapHandler {
    private final Plugin pluginInstance;
    private final Map<String, Command> commandMap;

    private final Map<String, Command> overridens = new HashMap<>();
    private Constructor<PluginCommand> pluginCommandConstructor;

    private Method syncMethod = null;
    private boolean notFound = false;

    public LatestBukkitCommandMapHandler(Plugin pluginInstance,
                                         Map<String, Command> commandMap) {
        this.pluginInstance = pluginInstance;
        this.commandMap = commandMap;
    }

    @Override
    public void synchronizeCommandMap() throws NoSuchMethodException {
        if (notFound) // in case of the syncCommands method doesn't exist, just skip it
            return; // command still works without synchronization anyway

        Server server = Bukkit.getServer();
        if (syncMethod == null) {
            try {
                syncMethod = server.getClass().getDeclaredMethod("syncCommands");
                syncMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                notFound = true;
                throw e;
            }
        }

        try {
            syncMethod.invoke(server);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean unregister(String triggerName) {
        Command command = commandMap.remove(triggerName);
        if (command == null)
            return false;

        if (overridens.containsKey(triggerName))
            commandMap.put(triggerName, overridens.remove(triggerName));
        else
            commandMap.remove(triggerName);

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
    public boolean commandExist(String triggerName) {
        return commandMap.containsKey(triggerName);
    }

    @Override
    public ICommand register(String triggerName, String[] aliases) throws Duplicated, NotInstantiated {
        if (commandExist(triggerName))
            throw new Duplicated(triggerName);

        PluginCommand command = createCommand(triggerName);
        ICommand iCommand = new BukkitCommand(command);

        Optional.ofNullable(commandMap.get(triggerName)).ifPresent(c -> overridens.put(triggerName, c));
        commandMap.put(triggerName, command);
        // register aliases manually here
        for (String alias : aliases) {
            Optional.ofNullable(commandMap.get(alias)).ifPresent(c -> overridens.put(alias, c));
            commandMap.put(alias, command);
        }

        return iCommand;
    }

    private PluginCommand createCommand(String commandName) throws NotInstantiated {
        try {
            if (pluginCommandConstructor == null) {
                pluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                pluginCommandConstructor.setAccessible(true);
            }
            return pluginCommandConstructor.newInstance(commandName, pluginInstance);
        } catch (Exception e) {
            throw new NotInstantiated(e);
        }
    }
}
