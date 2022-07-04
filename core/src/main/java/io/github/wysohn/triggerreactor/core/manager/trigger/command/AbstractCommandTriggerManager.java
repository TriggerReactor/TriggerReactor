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
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ITabCompleter.Template;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;



public abstract class AbstractCommandTriggerManager extends AbstractTriggerManager<CommandTrigger> {
    private static final String SYNC = "sync";
    private static final String PERMISSION = "permissions";
    private static final String ALIASES = "aliases";
    public static final String TABS = "tabs";

    public static final String HINT = "hint";
    public static final String CANDIDATES = "candidates";
    public static final String CONDITIONS = "conditions";
    public static final String INDEX = "index";

    public static final String REGEX = "regex";

    public AbstractCommandTriggerManager(TriggerReactorCore plugin, File folder) {
        super(plugin, folder, new ITriggerLoader<CommandTrigger>() {
            private final Map<String, Template> PreDefinedCompleterLabelMap = new HashMap<>();

            {
                PreDefinedCompleterLabelMap.put("$playerlist", Template.PLAYER);
            }

            private ITabCompleter toTabCompleter(Map<String, Object> tabs) {
                String hint = (String) tabs.get(HINT);
                String candidates_str = (String) tabs.get(CANDIDATES);
                List<Map<String, Object>> conditions = tabs.get(CONDITIONS) != null ? (List<Map<String, Object>>) tabs.get(CONDITIONS) : null;

                ITabCompleter.Builder builder;

                if(candidates_str == null){
                    if(hint == null) {
                        builder = ITabCompleter.Builder.of();
                    }else{
                        builder = ITabCompleter.Builder.of().setHint(ITabCompleter.list(hint.split(",")));
                    }
                }else {
                    if (candidates_str.startsWith("$") && PreDefinedCompleterLabelMap.containsKey(candidates_str)) {
                        if (hint == null)
                            builder = ITabCompleter.Builder.of(PreDefinedCompleterLabelMap.get(candidates_str));
                        else
                            builder = ITabCompleter.Builder.of(PreDefinedCompleterLabelMap.get(candidates_str)).setHint(ITabCompleter.list(hint.split(",")));

                    } else {
                        if (hint == null)
                            builder = ITabCompleter.Builder.of().setHint(ITabCompleter.list(candidates_str.split(","))).setCandidate(ITabCompleter.list(candidates_str.split(",")));
                        else
                            builder = ITabCompleter.Builder.of().setHint(ITabCompleter.list(hint.split(","))).setCandidate(ITabCompleter.list(candidates_str.split(",")));
                    }
                }
                if(conditions != null){
                    builder = builder.setConditions(toTabCompleterConditionMap(conditions));
                }
                return builder.build();
            }

            private Map<Integer, Set<ITabCompleter>> toTabCompleterMap(List<Map<String, Object>> tabs) {
                Map<Integer, Set<ITabCompleter>> tabMap = new HashMap<>();
                tabs.forEach((t) -> {
                    int idx;
                    if(t.containsKey(INDEX) && t.get(INDEX) instanceof Integer){
                        idx = (int) t.get(INDEX);
                    }else{
                        idx = tabs.indexOf(t);
                    }
                    if(tabMap.containsKey(idx))
                        tabMap.get(idx).add(toTabCompleter(t));
                    else{
                        Set<ITabCompleter> _set = new HashSet<>();
                        _set.add(toTabCompleter(t));
                        tabMap.put(idx, _set);
                    }
                });
                return tabMap;
            }

            private Map<Integer, Pattern> toTabCompleterConditionMap(List<Map<String, Object>> conditions) {
                Map<Integer, Pattern> conditionMap = new HashMap<>();
                conditions.forEach((c) -> {
                    int idx;
                    Pattern regexPattern;
                    if(c.containsKey(INDEX) && c.get(INDEX) instanceof Integer)
                        idx = (int) c.get(INDEX);
                    else
                        return;

                    if(c.containsKey(REGEX) && c.get(REGEX) instanceof String) {
                        try {
                            regexPattern = Pattern.compile((String) c.get(REGEX));
                        }catch(PatternSyntaxException e){
                            e.printStackTrace();
                            return;
                        }
                    }else {
                        return;
                    }
                    conditionMap.put(idx, regexPattern);
                });
                return conditionMap;
            }

            @Override
            public CommandTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
                List<String> permissions = info.getConfig().get(PERMISSION, List.class).orElse(new ArrayList<>());
                List<String> aliases = info.getConfig().get(ALIASES, List.class)
                        .map(aliasList -> (((List<String>)aliasList).stream()
                                .filter(alias -> !alias.equalsIgnoreCase(info.getTriggerName()))
                                .collect(Collectors.toList())))
                        .orElse(new ArrayList<>());
                List<Map<String, Object>> tabs = info.getConfig().get(TABS, List.class).orElse(new ArrayList<>());


                try {
                    String script = FileUtil.readFromFile(info.getSourceCodeFile());
                    CommandTrigger trigger = new CommandTrigger(info, script);
                    trigger.setPermissions(permissions.toArray(new String[0]));
                    trigger.setAliases(aliases.toArray(new String[0]));
                    trigger.setTabCompleterMap(toTabCompleterMap(tabs));
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

                    trigger.getInfo().getConfig().put(PERMISSION, trigger.getPermissions());
                    trigger.getInfo().getConfig().put(ALIASES, Arrays.stream(trigger.getAliases())
                            .filter(alias -> !alias.equalsIgnoreCase(trigger.getInfo().getTriggerName()))
                            .toArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void reload() {
        getAllTriggers().stream()
                .map(Trigger::getInfo)
                .map(TriggerInfo::getTriggerName)
                .forEach(this::unregisterCommand);

        super.reload();

        for (CommandTrigger trigger : getAllTriggers()) {
            if(!registerCommand(trigger.getInfo().getTriggerName(), trigger)){
                plugin.getLogger().warning("Attempted to register command trigger "+trigger.getInfo()+" but failed.");
                plugin.getLogger().warning("Probably, the command is already in use by another command trigger.");
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
            IConfigSource config = configSourceFactory.create(folder, name);
            TriggerInfo info = TriggerInfo.defaultInfo(file, config);
            trigger = new CommandTrigger(info, script);
        } catch (TriggerInitFailedException e1) {
            plugin.handleException(adding, e1);
            return false;
        }

        put(cmd, trigger);
        if(!registerCommand(cmd, trigger))
            return false;

        synchronizeCommandMap();
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

    /**
     * Register this command to command map. If the command is already in use by another plugin,
     * the original command will be overriden, and the original command will be recovered when
     * the trigger is un-registered. However, if the trigger's name is already registered and
     * also overriden by another command trigger, this method does nothing and return false.
     *
     * @param triggerName name of the trigger to register
     * @param trigger the actual trigger instance
     * @return true if registered; false if the command is already overriden by another command trigger and
     * is also already registered trigger, it will return false.
     */
    protected abstract boolean registerCommand(String triggerName, CommandTrigger trigger);

    /**
     * Unregister this command from command map.
     *
     * @param triggerName name of trigger to remove
     * @return true if unregistered; false if can't find the registered command.
     */
    protected abstract boolean unregisterCommand(String triggerName);

    protected abstract void synchronizeCommandMap();

    public void reregisterCommand(String triggerName) {
        Optional.ofNullable(get(triggerName))
                .ifPresent(trigger -> {
                    unregisterCommand(triggerName);
                    registerCommand(triggerName, trigger);

                    synchronizeCommandMap();
                });
    }
}