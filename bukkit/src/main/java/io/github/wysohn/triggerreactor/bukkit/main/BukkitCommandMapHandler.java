package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitCommand;
import io.github.wysohn.triggerreactor.core.bridge.ICommand;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandMapHandler;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class BukkitCommandMapHandler implements ICommandMapHandler {
    @Inject
    @Named("PluginInstance")
    Object pluginInstance;
    @Inject
    Logger logger;
    @Inject
    Map<String, Command> commandMap;
    @Inject
    IPluginLifecycleController pluginLifecycleController;

    private final Map<String, Command> overridens = new HashMap<>();
    private Constructor<PluginCommand> pluginCommandConstructor;

    @Inject
    BukkitCommandMapHandler() {

    }

    @Override
    public void synchronizeCommandMap() {

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
    public ICommand register(String triggerName, String[] aliases) throws Duplicated {
        if (commandExist(triggerName))
            throw new Duplicated();

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

    private PluginCommand createCommand(String commandName) {
        try {
            if (pluginCommandConstructor == null) {
                pluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                pluginCommandConstructor.setAccessible(true);
            }
            return pluginCommandConstructor.newInstance(commandName, pluginInstance);
        } catch (Exception ex) {
            if (pluginLifecycleController.isDebugging())
                ex.printStackTrace();

            logger.warning(
                    "Couldn't construct 'PluginCommand'. This may indicate that you are using very very old" + " "
                            + "version of Bukkit. Please report this to TR team, so we can work on it.");
            logger.warning("Use /trg debug to see more details.");
            return null;
        }
    }
}
