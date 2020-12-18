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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.AbstractCommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ITabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class CommandTriggerManager extends AbstractCommandTriggerManager implements BukkitTriggerManager {
    private final Map<String, Command> commandMap;
    private final Map<String, Command> overridens = new HashMap<>();

    public CommandTriggerManager(TriggerReactorCore plugin) {
        super(plugin, new File(plugin.getDataFolder(), "CommandTrigger"));
        commandMap = getCommandMap(plugin);
    }

    @Override
    protected boolean registerCommand(String triggerName, CommandTrigger trigger) {
        Optional.ofNullable(commandMap.get(triggerName))
                .ifPresent(command -> overridens.put(triggerName, command));

        PluginCommand command = createCommand(plugin, triggerName);
        command.setAliases(Arrays.stream(trigger.getAliases())
                .filter(triggerName::equalsIgnoreCase)
                .collect(Collectors.toList()));
        command.setTabCompleter((sender, command12, alias, args) -> {
            ITabCompleter tabCompleter = Optional.ofNullable(trigger.getTabCompleters())
                    .filter(iTabCompleters -> iTabCompleters.length >= args.length)
                    .map(iTabCompleters -> iTabCompleters[args.length - 1])
                    .orElse(ITabCompleter.EMPTY);

            String partial = args[args.length - 1];
            if (partial.length() < 1) { // show hint if nothing is entered yet
                return tabCompleter.getHint();
            } else {
                return tabCompleter.getCandidates(partial);
            }
        });
        command.setExecutor((sender, command1, label, args) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("CommandTrigger works only for Players.");
                return true;
            }

            ICommandSender commandSender = plugin.getPlayer(sender.getName());
            execute(plugin.createEmptyPlayerEvent(commandSender), (Player) sender, triggerName, args, trigger);
            return true;
        });

        commandMap.put(triggerName, command);
        return true;
    }

    @Override
    protected boolean unregisterCommand(String triggerName) {
        Command command = commandMap.remove(triggerName);
        if (command == null)
            return false;

        if (overridens.containsKey(triggerName)) {
            commandMap.put(triggerName, overridens.remove(triggerName));
        }

        return true;
    }

    private Method syncMethod = null;
    private boolean notFound = false;

    private void synchronizeAliases(Server server) throws IllegalAccessException, InvocationTargetException {
        if (notFound)
            return;

        Field craftCommandMapField = null;
        try {
            craftCommandMapField = server.getClass().getDeclaredField("commandMap");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();

            plugin.getLogger().warning("Couldn't locate CraftCommandMap. This may indicate that you are using very very old" +
                    " version of Bukkit. Please report this to TR team, so we can work on it.");
            plugin.getLogger().warning("Use /trg debug to see more details.");
            notFound = true;
            return;
        }

        craftCommandMapField.setAccessible(true);
        Object craftCommandMap = craftCommandMapField.get(server);

        Method registerServerAliasesMethod = null;
        try {
            registerServerAliasesMethod = craftCommandMap.getClass().getMethod("registerServerAliases");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();

            plugin.getLogger().warning("Couldn't find registerServerAliases(). This may indicate that you are using very very old" +
                    " version of Bukkit. Please report this to TR team, so we can work on it.");
            plugin.getLogger().warning("Use /trg debug to see more details.");
            notFound = true;
            return;
        }

        registerServerAliasesMethod.setAccessible(true);
        registerServerAliasesMethod.invoke(craftCommandMap);
    }

    @Override
    protected void synchronizeCommandMap() {
        if (notFound) // in case of the syncCommands method doesn't exist, just skip it
            return; // command still works without synchronization anyway

        Server server = Bukkit.getServer();
        if (syncMethod == null) {
            try {
                syncMethod = server.getClass().getMethod("syncCommands");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();

                plugin.getLogger().warning("Couldn't find syncCommands(). This may indicate that you are using very very old" +
                        " version of Bukkit. Please report this to TR team, so we can work on it.");
                plugin.getLogger().warning("Use /trg debug to see more details.");
                notFound = true;
                return;
            }
        }

        try {
            synchronizeAliases(server);
            syncMethod.invoke(server);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onCommand(PlayerCommandPreprocessEvent e) {
//        Player player = e.getPlayer();
//        String[] split = e.getMessage().split(" ");
//
//        String cmd = split[0];
//        cmd = cmd.replaceAll("/", "");
//        String[] args = new String[split.length - 1];
//        for (int i = 0; i < args.length; i++)
//            args[i] = split[i + 1];
//
//        CommandTrigger trigger = get(cmd);
//        if (trigger == null)
//            trigger = aliasesMap.get(cmd);
//        if (trigger == null)
//            return;
//        e.setCancelled(true);
//
//        execute(e, player, cmd, args, trigger);
//    }

    private void execute(Object context, Player player, String cmd, String[] args, CommandTrigger trigger) {
        for (String permission : trigger.getPermissions()) {
            if (!player.hasPermission(permission)) {
                player.sendMessage(ChatColor.RED + "[TR] You don't have permission!");
                if (plugin.isDebugging()) {
                    plugin.getLogger().info("Player " + player.getName() + " executed command " + cmd
                            + " but didn't have permission " + permission + "");
                }
                return;
            }
        }

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", player);
        varMap.put("command", cmd);
        varMap.put("args", args);
        varMap.put("argslength", args.length);

        trigger.activate(context, varMap);
    }

    private static Map<String, Command> getCommandMap(TriggerReactorCore core) {
        try {
            Server server = Bukkit.getServer();

            Field f = server.getClass().getDeclaredField("commandMap");
            f.setAccessible(true);

            CommandMap scm = (CommandMap) f.get(server);

            Method knownCommands = scm.getClass().getDeclaredMethod("getKnownCommands");
            return (Map<String, Command>) knownCommands.invoke(scm);
        } catch (Exception ex) {
            if (core.isDebugging())
                ex.printStackTrace();

            core.getLogger().warning("Couldn't find 'commandMap'. This may indicate that you are using very very old" +
                    " version of Bukkit. Please report this to TR team, so we can work on it.");
            core.getLogger().warning("Use /trg debug to see more details.");
            return null;
        }
    }

    private static PluginCommand createCommand(TriggerReactorCore core, String commandName) {
        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            return c.newInstance(commandName, core.getMain());
        } catch (Exception ex) {
            if (core.isDebugging())
                ex.printStackTrace();

            core.getLogger().warning("Couldn't construct 'PluginCommand'. This may indicate that you are using very very old" +
                    " version of Bukkit. Please report this to TR team, so we can work on it.");
            core.getLogger().warning("Use /trg debug to see more details.");
            return null;
        }
    }
}
