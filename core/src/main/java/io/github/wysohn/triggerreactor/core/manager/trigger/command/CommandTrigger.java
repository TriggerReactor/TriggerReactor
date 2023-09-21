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

import com.google.inject.assistedinject.Assisted;
import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.manager.annotation.TriggerRuntimeDependency;
import io.github.wysohn.triggerreactor.core.manager.trigger.*;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public class CommandTrigger extends Trigger {
    @Inject
    private ICommandTriggerFactory factory;

    @TriggerRuntimeDependency
    Map<Integer, Set<ITabCompleter>> tabCompleterMap = new HashMap<>();
    @TriggerRuntimeDependency
    private ICommand command;

    @Inject
    private CommandTrigger(@Assisted TriggerInfo info,
                           @Assisted String script) throws AbstractTriggerManager.TriggerInitFailedException {
        super(info, script);
    }

    public String[] getPermissions() {
        return info.get(TriggerConfigKey.KEY_TRIGGER_COMMAND_PERMISSION, List.class)
                .map(list -> list.toArray(new String[0]))
                .map(list -> (String[]) list)
                .orElse(new String[0]);
    }

    public void setPermissions(String[] permissions) {
        if (permissions == null) {
            info.put(TriggerConfigKey.KEY_TRIGGER_COMMAND_PERMISSION, new ArrayList<>());
        } else {
            info.put(TriggerConfigKey.KEY_TRIGGER_COMMAND_PERMISSION, permissions);
        }
    }

    public String[] getAliases() {
        return info.get(TriggerConfigKey.KEY_TRIGGER_COMMAND_ALIASES, List.class)
                .map(aliasList -> (((List<String>) aliasList).stream()
                        .filter(alias -> !alias.equalsIgnoreCase(info.getTriggerName()))
                        .collect(Collectors.toList())))
                .map(list -> list.toArray(new String[0]))
                .orElse(new String[0]);
    }

    public void setAliases(String[] aliases) {
        if (aliases == null) {
            info.put(TriggerConfigKey.KEY_TRIGGER_COMMAND_ALIASES, new ArrayList<>());
        } else {
            info.put(TriggerConfigKey.KEY_TRIGGER_COMMAND_ALIASES, aliases);
        }
    }

    public Map<Integer, Set<ITabCompleter>> getTabCompleterMap() {
        return tabCompleterMap;
    }

    public void setTabCompleterMap(Map<Integer, Set<ITabCompleter>> tabCompleterMap) {
        if (tabCompleterMap == null) {
            this.tabCompleterMap = new HashMap<>();
        } else {
            this.tabCompleterMap = tabCompleterMap;
        }
    }

    public ICommand getCommand() {
        return command;
    }

    public void setCommand(ICommand command) {
        this.command = command;
    }

    @Override
    public CommandTriggerFacade getTriggerFacade() {
        return new CommandTriggerFacade(this);
    }

    @Override
    public CommandTrigger clone() {
        return factory.create(getInfo(), getScript());
    }

    @Override
    public String toString() {
        return super.toString() + "{" +
                "permissions=" + Arrays.toString(getPermissions()) +
                ", aliases=" + Arrays.toString(getAliases()) +
                '}';
    }
}
