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
package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.IEventManagement;
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.main.command.ICommandHandler;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Singleton
public final class CommandTriggerManager extends AbstractTriggerManager<CommandTrigger> {
    public static final String TAB_HINT = "hint";
    public static final String TAB_CANDIDATES = "candidates";
    public static final String TAB_CONDITIONS = "conditions";
    public static final String TAB_INDEX = "index";

    public static final String TAB_REGEX = "regex";

    @Inject
    private Logger logger;
    @Inject
    private ICommandHandler commandHandler;
    @Inject
    private IPluginManagement pluginManagement;
    @Inject
    private IEventManagement eventManagement;
    @Inject
    private IExceptionHandle exceptionHandle;
    @Inject
    private ICommandTriggerFactory factory;
    @Inject
    private ITriggerLoader<CommandTrigger> loader;

    @Inject
    private CommandTriggerManager(@Named("DataFolder") File dataFolder,
                                  @Named("CommandTriggerManagerFolder") String folderName) {
        super(new File(dataFolder, folderName));
    }

    @Override
    public void initialize() {

    }

    @Override
    public void reload() {
        getAllTriggers().stream()
                .map(Trigger::getInfo)
                .map(TriggerInfo::getTriggerName)
                .forEach(commandHandler::unregister);

        super.reload();

        for (CommandTrigger trigger : getAllTriggers()) {
            if (!registerToAPI(trigger)) {
                logger.warning("Attempted to register command trigger " + trigger.getInfo() + " but failed.");
                logger.warning("Probably, the command is already in use by another command trigger.");
            }
        }

        commandHandler.sync();
    }

    @Override
    public void reload(String triggerName) {
        commandHandler.unregister(triggerName);
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
     *
     * @param trigger target command trigger
     * @return true if the command is successfully registered. false if the command is already in use.
     */
    private boolean registerToAPI(CommandTrigger trigger) {
        ICommand command = commandHandler.register(trigger.getInfo().getTriggerName(),
                trigger.getAliases());
        if (command == null)
            return false;

        trigger.setCommand(command);
        command.setTabCompleterMap(trigger.getTabCompleterMap());
        command.setExecutor((sender, label, args) -> {
            //TODO: remove this if we allow to use the command trigger in the console.
            if (!(sender instanceof IPlayer)) {
                sender.sendMessage("CommandTrigger works only for Players.");
                return;
            }

            execute(eventManagement.createPlayerCommandEvent(sender, label, args),
                    sender,
                    label,
                    args,
                    trigger);
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
        try {
            String name = TriggerInfo.extractName(file);
            IConfigSource config = getConfigSource(folder, name);
            TriggerInfo info = TriggerInfo.defaultInfo(file, config);
            trigger = factory.create(info, script);
            trigger.init();

            if (!registerToAPI(trigger))
                return false;
        } catch (Exception e1) {
            commandHandler.unregister(cmd);
            exceptionHandle.handleException(adding, e1);
            return false;
        }

        put(cmd, trigger);
        commandHandler.sync();
        return true;
    }

    public CommandTrigger createTempCommandTrigger(String script) throws TriggerInitFailedException {
        CommandTrigger commandTrigger = factory.create(new TriggerInfo(null,
                IConfigSource.empty(),
                "temp") {
            @Override
            public boolean isValid() {
                return false;
            }
        }, script);
        commandTrigger.init();
        return commandTrigger;
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
                if (pluginManagement.isDebugging()) {
                    logger.info("Player " + sender.getName() + " executed command " + cmd
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
