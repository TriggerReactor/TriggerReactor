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
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.config.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.manager.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommandTriggerManager extends AbstractTriggerManager<CommandTrigger> {
    protected final Map<String, CommandTrigger> aliasesMap = new CommandMap();

    public AbstractCommandTriggerManager(TriggerReactorCore plugin, File folder) {
        super(plugin, folder, new ITriggerLoader<CommandTrigger>() {
            @Override
            public CommandTrigger instantiateTrigger(TriggerInfo info) throws InvalidTrgConfigurationException {
                boolean sync = info.getConfig().get("sync", Boolean.class).orElse(false);
                List<String> permissions = info.getConfig().get("permissions", List.class).orElse(new ArrayList<>());
                List<String> aliases = info.getConfig().get("aliases", List.class).orElse(new ArrayList<>());

                try {
                    String script = FileUtil.readFromFile(info.getSourceCodeFile());
                    CommandTrigger trigger = new CommandTrigger(info, script);
                    trigger.setPermissions(permissions.toArray(new String[0]));
                    trigger.setAliases(aliases.toArray(new String[0]));
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

                    trigger.getInfo().getConfig().put("sync", trigger.isSync());
                    trigger.getInfo().getConfig().put("permissions", trigger.getPermissions());
                    trigger.getInfo().getConfig().put("aliases", trigger.getAliases());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void reload() {
        super.reload();

        aliasesMap.clear();

        for (CommandTrigger trigger : getAllTriggers()) {
            registerAliases(trigger);
        }
    }

    @Override
    protected void deleteInfo(CommandTrigger trigger) {
        removeAliases(trigger);
        super.deleteInfo(trigger);
    }

    @Override
    protected CommandTrigger remove(String name) {
        CommandTrigger remove = super.remove(name);
        removeAliases(remove);
        return remove;
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
        CommandTrigger trigger = null;
        try {
            String name = TriggerInfo.extractName(file);
            IConfigSource config = ConfigSourceFactory.gson(folder, name + ".json");
            TriggerInfo info = TriggerInfo.defaultInfo(file, config);
            trigger = new CommandTrigger(info, script);
        } catch (TriggerInitFailedException e1) {
            plugin.handleException(adding, e1);
            return false;
        }

        put(cmd, trigger);

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

    public void removeAliases(CommandTrigger trigger) {
        for (String alias : trigger.getAliases()) {
            aliasesMap.remove(alias);
        }
    }

    public void registerAliases(CommandTrigger trigger) {
        for (String alias : trigger.getAliases()) {
            CommandTrigger prev = aliasesMap.get(alias);
            if (prev != null) {
                plugin.getLogger().warning("CommandTrigger " + trigger.getInfo() + "'s alias "
                        + alias + " couldn't be registered.");
                plugin.getLogger().warning(alias + " is already used by " + prev.getInfo() + ".");
                continue;
            }

            aliasesMap.put(alias, trigger);
        }
    }

    private static class CommandMap extends HashMap<String, CommandTrigger> {
        @Override
        public CommandTrigger get(Object o) {
            if (o instanceof String)
                return super.get(((String) o).toLowerCase());
            else
                return super.get(o);
        }

        @Override
        public CommandTrigger put(String s, CommandTrigger commandTrigger) {
            return super.put(s.toLowerCase(), commandTrigger);
        }

        @Override
        public boolean containsKey(Object o) {
            if (o instanceof String)
                return super.containsKey(((String) o).toLowerCase());
            else
                return super.containsKey(o);
        }

        @Override
        public CommandTrigger remove(Object o) {
            if (o instanceof String)
                return super.remove(((String) o).toLowerCase());
            else
                return super.remove(o);
        }
    }
}