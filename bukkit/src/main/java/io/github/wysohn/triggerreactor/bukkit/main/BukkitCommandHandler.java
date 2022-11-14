/*
 * Copyright (C) 2022. TriggerReactor Team
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

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.ICommandMapHandler;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.main.command.ICommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class BukkitCommandHandler implements ICommandHandler {
    @Inject
    private JavaPlugin plugin;
    @Inject
    private ICommandMapHandler commandMapHandler;
    @Inject
    private IPluginManagement pluginManagement;

    private final Map<String, Command> overridens = new HashMap<>();

    @Inject
    private BukkitCommandHandler() {
    }

    @Override
    public ICommand register(String name, String[] aliases) {
        Map<String, Command> rawCommandMap = commandMapHandler.getCommandMap();

        if (rawCommandMap.containsKey(name) && overridens.containsKey(name))
            return null;

        PluginCommand command = createCommand(name);

        Optional.ofNullable(rawCommandMap.get(name))
                .ifPresent(c -> overridens.put(name, c));
        rawCommandMap.put(name, command);
        // register aliases manually here
        for (String alias : aliases) {
            Optional.ofNullable(rawCommandMap.get(alias))
                    .ifPresent(c -> overridens.put(alias, c));
            rawCommandMap.put(alias, command);
        }

        commandMapHandler.synchronizeCommandMap();
        return new BukkitCommand(command);
    }

    @Override
    public boolean unregister(String name) {
        Map<String, Command> rawCommandMap = commandMapHandler.getCommandMap();

        Command command = rawCommandMap.remove(name);
        if (command == null)
            return false;

        if (overridens.containsKey(name))
            rawCommandMap.put(name, overridens.remove(name));
        else
            rawCommandMap.remove(name);

        // also un-register aliases manually here
        for (String alias : command.getAliases()) {
            if (overridens.containsKey(alias))
                rawCommandMap.put(alias, overridens.remove(alias));
            else
                rawCommandMap.remove(alias);
        }

        return true;
    }

    @Override
    public void sync() {
        commandMapHandler.synchronizeCommandMap();
    }

    private PluginCommand createCommand(String commandName) {
        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            return c.newInstance(commandName, plugin);
        } catch (Exception ex) {
            if (pluginManagement.isDebugging())
                ex.printStackTrace();

            plugin.getLogger()
                    .warning("Couldn't construct 'PluginCommand'. This may indicate that you are using very very old" +
                                     " version of Bukkit. Please report this to TR team, so we can work on it.");
            plugin.getLogger().warning("Use /trg debug to see more details.");
            return null;
        }
    }
}
