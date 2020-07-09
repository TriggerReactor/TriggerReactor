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
package io.github.wysohn.triggerreactor.sponge.manager.trigger;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.AbstractCommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ITabCompleter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommandTriggerManager extends AbstractCommandTriggerManager {
    private final CommandManager commandManager;
    private final Map<String, CommandMapping> mappings = new HashMap<>();

    public CommandTriggerManager(TriggerReactorCore plugin) {
        super(plugin, new File(plugin.getDataFolder(), "CommandTrigger"));
        commandManager = Sponge.getCommandManager();
    }

    @Override
    protected boolean registerCommand(String triggerName, CommandTrigger trigger) {
        String[] commandArr = new String[1 + trigger.getAliases().length];
        commandArr[0] = triggerName;
        for (int i = 1; i < commandArr.length; i++)
            commandArr[i] = trigger.getAliases()[i - 1];

        commandManager.register(plugin.getMain(), new CommandCallable() {
            @Override
            public CommandResult process(CommandSource source, String arguments) throws CommandException {
                if (!(source instanceof Player)) {
                    source.sendMessage(Text.of("CommandTrigger works only for Players."));
                    return CommandResult.success();
                }

                ICommandSender commandSender = plugin.getPlayer(source.getName());
                execute(plugin.createEmptyPlayerEvent(commandSender),
                        (Player) source,
                        triggerName,
                        arguments.split(" "),
                        trigger);

                return CommandResult.success();
            }

            @Override
            public List<String> getSuggestions(CommandSource source, String arguments,
                                               @Nullable Location<World> targetPosition) throws CommandException {
                String[] args = arguments.split(" ");

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
            }

            @Override
            public boolean testPermission(CommandSource source) {
                for (String permission : trigger.getPermissions()) {
                    if (!source.hasPermission(permission))
                        return false;
                }
                return true;
            }

            @Override
            public Optional<Text> getShortDescription(CommandSource source) {
                return Optional.empty();
            }

            @Override
            public Optional<Text> getHelp(CommandSource source) {
                return Optional.empty();
            }

            @Override
            public Text getUsage(CommandSource source) {
                return null;
            }
        }, commandArr).ifPresent(commandMapping -> mappings.put(triggerName, commandMapping));
        return false;
    }

    @Override
    protected boolean unregisterCommand(String triggerName) {
        // CommandMapping instance seems like an unique entity regardless of the commands being same
        CommandMapping mapping = commandManager.get(triggerName).orElse(null);
        if (mapping == null)
            return false;

        commandManager.removeMapping(mapping);
        return true;
    }

//    @Listener(order = Order.EARLY)
//    public void onCommand(SendCommandEvent e) {
//        Player player = e.getCause().first(Player.class).orElse(null);
//
//        String cmd = e.getCommand();
//        String[] args = e.getArguments().split(" ");
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

    private void execute(Object e, Player player, String cmd, String[] args, CommandTrigger trigger) {
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", player);
        varMap.put("command", cmd);
        varMap.put("args", args);
        varMap.put("argslength", args.length);

        trigger.activate(e, varMap);
    }

}
