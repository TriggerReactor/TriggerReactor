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
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandTrigger extends Trigger {
    String[] permissions = new String[0];
    String[] aliases = new String[0];
    Map<Integer, Set<ITabCompleter>> tabCompleterMap = new HashMap<>();

    private ICommand command;

    @Inject
    private CommandTrigger(@Assisted TriggerInfo info,
                           @Assisted String script) throws AbstractTriggerManager.TriggerInitFailedException {
        super(info, script);
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        if (permissions == null) {
            this.permissions = new String[0];
        } else {
            this.permissions = permissions;
        }
    }

    public String[] getAliases() {
        return aliases;
    }

    public void setAliases(String[] aliases) {
        if (aliases == null) {
            this.aliases = new String[0];
        } else {
            this.aliases = aliases;
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
    public CommandTrigger clone() {
        try {
            return new CommandTrigger(info, getScript());
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "{" +
                "permissions=" + Arrays.toString(permissions) +
                ", aliases=" + Arrays.toString(aliases) +
                '}';
    }
}
