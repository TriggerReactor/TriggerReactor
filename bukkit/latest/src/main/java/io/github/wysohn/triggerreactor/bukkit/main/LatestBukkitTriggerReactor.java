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

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitCommand;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitCommandSender;
import io.github.wysohn.triggerreactor.bukkit.components.BukkitTriggerReactorComponent;
import io.github.wysohn.triggerreactor.core.bridge.ICommand;
import io.github.wysohn.triggerreactor.core.main.CommandHandler;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandMapHandler;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class LatestBukkitTriggerReactor extends JavaPlugin implements ICommandMapHandler {
    private BukkitTriggerReactorComponent component = DaggerLatestBukkitTriggerReactorComponent.builder().build();
    private BukkitTriggerReactor bukkitTriggerReactor;
    private CommandHandler commandHandler;
    private IWrapper wrapper;

    private Method syncMethod = null;
    private boolean notFound = false;

    /**
     * Reference to the command map of Bukkit API. Accessing this
     * map has direct effect on the Bukkit API.
     */
    private Map<String, Command> rawCommandMap;

    @Override
    public void onEnable() {
        bukkitTriggerReactor = component.bukkitTriggerReactor();
        commandHandler = component.commandHandler();
        wrapper = component.wrapper();

        bukkitTriggerReactor.onEnable();

        rawCommandMap = getCommandMap();
    }

    @Override
    public void onDisable() {
        bukkitTriggerReactor.onDisable();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandHandler.onCommand(wrapper.wrap(sender), command.getName(), args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return commandHandler.onTabComplete(new BukkitCommandSender(sender), args);
    }

    @Override
    public void synchronizeCommandMap() {
        if (notFound) // in case of the syncCommands method doesn't exist, just skip it
            return; // command still works without synchronization anyway

        Server server = Bukkit.getServer();
        if (syncMethod == null) {
            try {
                syncMethod = server.getClass().getDeclaredMethod("syncCommands");
                syncMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                if (bukkitTriggerReactor.isDebugging())
                    e.printStackTrace();

                getLogger().warning("Couldn't find syncCommands(). This is not an error! Though, tab-completer"
                        + " may not work with this error. Report to us if you believe this version has to support it.");
                getLogger().warning("Use /trg debug to see more details.");
                notFound = true;
                return;
            }
        }

        try {
            syncMethod.invoke(server);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean unregister(String commandName) {
        return rawCommandMap.remove(commandName) != null;
    }

    @Override
    public boolean commandExist(String commandName) {
        return rawCommandMap.containsKey(commandName);
    }

    @Override
    public ICommand register(String commandName, String[] aliases) throws Duplicated {
        if(rawCommandMap.containsKey(commandName))
            throw new Duplicated(commandName);

        PluginCommand pluginCommand = this.getCommand("triggerreactor");
        ICommand command = new BukkitCommand(pluginCommand);
        rawCommandMap.put(commandName, pluginCommand);

        return command;
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
            if (bukkitTriggerReactor.isDebugging())
                ex.printStackTrace();

            getLogger().warning("Couldn't find 'commandMap'. This may indicate that you are using very very old"
                    + " version of Bukkit. Please report this to TR team, so we can work on it.");
            getLogger().warning("Use /trg debug to see more details.");
            return null;
        }
    }

    public BukkitBungeeCordHelper getBungeeHelper() {
        return bukkitTriggerReactor.getBungeeHelper();
    }

    public BukkitMysqlSupport getMysqlHelper() {
        return bukkitTriggerReactor.getMysqlHelper();
    }
}
