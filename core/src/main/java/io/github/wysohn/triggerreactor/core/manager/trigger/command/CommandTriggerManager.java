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

import io.github.wysohn.triggerreactor.core.bridge.ICommand;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactories;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.scope.ManagerScope;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ManagerScope
public class CommandTriggerManager extends AbstractTriggerManager<CommandTrigger> {
    @Inject
    CommandTriggerFactory factory;
    @Inject
    ICommandMapHandler commandMapHandler;
    @Inject
    Logger logger;
    @Inject
    IGameController gameController;
    @Inject
    IPluginLifecycleController pluginLifecycleController;
    @Inject
    ConfigSourceFactories configSourceFactories;

    @Inject
    CommandTriggerManager() {
        super("CommandTrigger");
    }

    @Override
    public CommandTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            return factory.create(info, script);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(CommandTrigger trigger) {
        super.save(trigger);

        reregisterCommand(trigger.getTriggerName());
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onReload() {
        getAllTriggers().stream()
                .map(Trigger::getTriggerName)
                .forEach(this::unregisterCommand);

        super.onReload();

        for (CommandTrigger trigger : getAllTriggers()) {
            if (!registerCommand(trigger.getTriggerName(), trigger)) {
                logger.warning("Attempted to register command trigger " + trigger + " but failed.");
                logger.warning("Probably, the command is already in use by another command trigger.");
            }
        }

        synchronizeCommandMap();
    }

    @Override
    public void reload(String triggerName) {
        super.reload(triggerName);
        reregisterCommand(triggerName);
    }

    @Override
    public CommandTrigger remove(String name) {
        CommandTrigger remove = super.remove(name);
        unregisterCommand(name);

        synchronizeCommandMap();
        return remove;
    }

    public void reregisterCommand(String triggerName) {
        Optional.ofNullable(get(triggerName)).ifPresent(trigger -> {
            unregisterCommand(triggerName);
            registerCommand(triggerName, trigger);

            synchronizeCommandMap();
        });
    }

    /**
     * Unregister this command from command map.
     *
     * @param triggerName name of trigger to remove
     * @return true if unregistered; false if can't find the registered command.
     */
    protected boolean unregisterCommand(String triggerName) {
        return commandMapHandler.unregister(triggerName);
    }

    /**
     * Register this command to command map. If the command is already in use by another plugin,
     * the original command will be overriden, and the original command will be recovered when
     * the trigger is un-registered. However, if the trigger's name is already registered and
     * also overriden by another command trigger, this method does nothing and return false.
     *
     * @param triggerName name of the trigger to register
     * @param trigger     the actual trigger instance
     * @return true if registered; false if the command is already overriden by another command trigger and
     * is also already registered trigger, it will return false.
     */
    protected boolean registerCommand(String triggerName, CommandTrigger trigger) {
        if (commandMapHandler.commandExist(triggerName))
            return false;

        ICommand command;
        try {
            command = commandMapHandler.register(triggerName, trigger.getAliases());
        } catch (ICommandMapHandler.Duplicated e) {
            e.printStackTrace();
            return false;
        }

        command.setAliases(Arrays.stream(trigger.getAliases()).collect(Collectors.toList()));
        command.setTabCompleter(
                Optional.ofNullable(trigger.getTabCompleters()).orElse(StaticTabCompleter.Builder.of().buildAsArray()));
        command.setExecutor((sender, command1, label, args) -> {
            if (!(sender instanceof IPlayer)) {
                sender.sendMessage("CommandTrigger works only for Players.");
                return true;
            }

            execute(gameController.createPlayerCommandEvent(sender, label, args), (IPlayer) sender, triggerName, args,
                    trigger);
            return true;
        });

        return true;
    }

    protected void synchronizeCommandMap() {
        commandMapHandler.synchronizeCommandMap();
    }

    private void execute(Object event, IPlayer player, String cmd, String[] args, CommandTrigger trigger) {
        for (String permission : trigger.getPermissions()) {
            if (!player.hasPermission(permission)) {
                player.sendMessage("&c[TR] You don't have permission!");
                if (pluginLifecycleController.isDebugging()) {
                    logger.info(
                            "Player " + player.getName() + " executed command " + cmd + " but didn't have permission "
                                    + permission + "");
                }
                return;
            }
        }

        Map<String, Object> varMap = new HashMap<>();
        varMap.put(Trigger.VAR_NAME_EVENT, event);
        varMap.put("player", player.get());
        varMap.put("command", cmd);
        varMap.put("args", args);
        varMap.put("argslength", args.length);

        trigger.activate(varMap);
    }

    /**
     * @param cmd    command to intercept
     * @param script script to be executed
     * @return true on success; false if cmd already binded.
     */
    public boolean createCommandTrigger(String cmd, String script) {
        if (has(cmd))
            return false;

        File file = getTriggerFile(folder, cmd, true);
        String name = TriggerInfo.extractName(file);
        IConfigSource config = configSourceFactories.create(folder, name);
        TriggerInfo info = TriggerInfo.defaultInfo(file, config);
        CommandTrigger trigger = factory.create(info, script);

        put(cmd, trigger);
        if (!registerCommand(cmd, trigger))
            return false;

        synchronizeCommandMap();
        return true;
    }

    public CommandTrigger createTempCommandTrigger(String script) {
        return factory.create(new TriggerInfo(null, null, "temp") {
            @Override
            public boolean isValid() {
                return false;
            }
        }, script);
    }
}