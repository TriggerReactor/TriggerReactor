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
package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.main.command.ICommandHandler;
import io.github.wysohn.triggerreactor.core.manager.trigger.*;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ITabCompleter.Template;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public final class CommandTriggerManager extends AbstractTriggerManager<CommandTrigger> {
    public static final String HINT = "hint";
    public static final String CANDIDATES = "candidates";

    ICommandHandler commandHandler;

    public CommandTriggerManager(TriggerReactorCore plugin, ICommandHandler commandHandler) {
        super(plugin, new File(plugin.getDataFolder(), "CommandTrigger"), new ITriggerLoader<CommandTrigger>() {
            private final Map<String, ITabCompleter> tabCompleterMap = new HashMap<>();

            {
                tabCompleterMap.put("$playerlist", ITabCompleter.Builder.of(Template.PLAYER).build());
            }

            private ITabCompleter toTabCompleter(Map<String, Object> tabs) {
                String hint = (String) tabs.get(HINT);
                String candidates_str = (String) tabs.get(CANDIDATES);

                ITabCompleter tabCompleter;
                if (candidates_str != null && candidates_str.startsWith("$")) {
                    tabCompleter = tabCompleterMap.getOrDefault(candidates_str, ITabCompleter.Builder.of().build());
                } else if (candidates_str == null && hint != null) {
                    tabCompleter = ITabCompleter.Builder.withHint(hint).build();
                } else if (candidates_str != null && hint == null) {
                    tabCompleter = ITabCompleter.Builder.of(candidates_str).build();
                } else {
                    tabCompleter = ITabCompleter.Builder.withHint(hint)
                            .setCandidate(
                                    Optional.ofNullable(candidates_str)
                                            .map(str -> ITabCompleter.list(str.split(",")))
                                            .orElseGet(() -> ITabCompleter.list(""))
                            )
                            .build();
                }
                return tabCompleter;
            }

            private ITabCompleter[] toTabCompleters(List<Map<String, Object>> tabs) {
                return tabs.stream()
                        .map(this::toTabCompleter)
                        .toArray(ITabCompleter[]::new);
            }

            @Override
            public CommandTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
                List<String> permissions = info.get(TriggerConfigKey.KEY_TRIGGER_COMMAND_PERMISSION, List.class)
                        .orElse(new ArrayList<>());
                List<String> aliases = info.get(TriggerConfigKey.KEY_TRIGGER_COMMAND_ALIASES, List.class)
                        .map(aliasList -> (((List<String>) aliasList).stream()
                                .filter(alias -> !alias.equalsIgnoreCase(info.getTriggerName()))
                                .collect(Collectors.toList())))
                        .orElse(new ArrayList<>());
                List<Map<String, Object>> tabs = info.get(TriggerConfigKey.KEY_TRIGGER_COMMAND_TABS, List.class)
                        .orElse(new ArrayList<>());

                try {
                    String script = FileUtil.readFromFile(info.getSourceCodeFile());
                    CommandTrigger trigger = new CommandTrigger(info, script);
                    trigger.setPermissions(permissions.toArray(new String[0]));
                    trigger.setAliases(aliases.toArray(new String[0]));
                    trigger.setTabCompleters(toTabCompleters(tabs));
                    return trigger;
                } catch (TriggerInitFailedException | IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void save(CommandTrigger trigger) {
                try {
                    FileUtil.writeToFile(trigger.getInfo().getSourceCodeFile(), trigger.getScript());

                    trigger.getInfo().put(TriggerConfigKey.KEY_TRIGGER_COMMAND_PERMISSION, trigger.getPermissions());
                    trigger.getInfo().put(TriggerConfigKey.KEY_TRIGGER_COMMAND_ALIASES, Arrays.stream(trigger.getAliases())
                            .filter(alias -> !alias.equalsIgnoreCase(trigger.getInfo().getTriggerName()))
                            .toArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        this.commandHandler = commandHandler;
    }

    @Override
    public void reload() {
        getAllTriggers().stream()
                .map(Trigger::getInfo)
                .map(TriggerInfo::getTriggerName)
                .forEach(commandHandler::unregister);

        super.reload();

        for (CommandTrigger trigger : getAllTriggers()) {
            if(!registerToAPI(trigger)){
                plugin.getLogger()
                        .warning("Attempted to register command trigger " + trigger.getInfo() + " but failed.");
                plugin.getLogger().warning("Probably, the command is already in use by another command trigger.");
            }
        }

        commandHandler.sync();
    }

    @Override
    public void reload(String triggerName) {
        commandHandler.unregisterAll();
        super.reload(triggerName);
        reregisterCommand(triggerName);
    }

    @Override
    public CommandTrigger remove(String name) {
        CommandTrigger remove = super.remove(name);
        commandHandler.unregister(name);

        return remove;
    }

    /**
     * Register the trigger to the API so that it can be served as a real command.
     * @param trigger target command trigger
     * @return true if the command is successfully registered. false if the command is already in use.
     */
    private boolean registerToAPI(CommandTrigger trigger) {
        ICommand command = commandHandler.register(trigger.getInfo().getTriggerName(),
                                                   trigger.aliases);
        if(command == null)
            return false;

        trigger.setCommand(command);
        command.setTabCompleters(trigger.getTabCompleters());
        command.setExecutor((sender, label, args) -> {
            //TODO: remove this if we allow to use the command trigger in the console.
            if (!(sender instanceof IPlayer)) {
                sender.sendMessage("CommandTrigger works only for Players.");
                return;
            }

            execute(plugin.createPlayerCommandEvent(sender, label, args), sender, label, args, trigger);
        });

        return true;
    }

    /**
     * @param adding CommandSender to send error message on script error
     * @param cmd    command to intercept
     * @param script script to be executed
     * @return true on success; false if cmd already binded.
     */
    public boolean addCommandTrigger(ICommandSender adding, String cmd, String script) {
        if (has(cmd))
            return false;

        File file = getTriggerFile(folder, cmd, true);
        CommandTrigger trigger;
        ICommand command;
        try {
            String name = TriggerInfo.extractName(file);
            IConfigSource config = configSourceFactory.create(folder, name);
            TriggerInfo info = TriggerInfo.defaultInfo(file, config);
            trigger = new CommandTrigger(info, script);

            command = commandHandler.register(cmd, trigger.aliases);
            if (command == null)
                return false;

            trigger.setCommand(command);
        } catch (TriggerInitFailedException e1) {
            commandHandler.unregister(cmd);
            plugin.handleException(adding, e1);
            return false;
        }

        put(cmd, trigger);
        commandHandler.sync();
        plugin.saveAsynchronously(this);
        return true;
    }

    public CommandTrigger createTempCommandTrigger(String script) throws TriggerInitFailedException {
        return new CommandTrigger(new TriggerInfo(null, null, "temp") {
            @Override
            public boolean isValid() {
                return false;
            }
        }, script);
    }

    public void reregisterCommand(String triggerName) {
        Optional.ofNullable(get(triggerName))
                .ifPresent(trigger -> {
                    commandHandler.unregister(triggerName);
                    registerToAPI(trigger);

                    commandHandler.sync();
                });
    }

    private void execute(Object context, ICommandSender sender, String cmd, String[] args, CommandTrigger trigger) {
        for (String permission : trigger.getPermissions()) {
            if (!sender.hasPermission(permission)) {
                sender.sendMessage("&c[TR] You don't have permission!");
                if (plugin.isDebugging()) {
                    plugin.getLogger().info("Player " + sender.getName() + " executed command " + cmd
                                                    + " but didn't have permission " + permission + "");
                }
                return;
            }
        }

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", sender.get());
        varMap.put("sender", sender.get());
        varMap.put("command", cmd);
        varMap.put("args", args);
        varMap.put("argslength", args.length);

        trigger.activate(context, varMap);
    }
}