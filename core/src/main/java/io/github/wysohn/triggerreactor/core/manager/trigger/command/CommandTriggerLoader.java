package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

class CommandTriggerLoader implements ITriggerLoader<CommandTrigger> {
    private final Map<String, ITabCompleter.Template> PreDefinedCompleterLabelMap = new HashMap<>();

    {
        PreDefinedCompleterLabelMap.put("$playerlist", ITabCompleter.Template.PLAYER);
    }
    private ITabCompleter toTabCompleter(Map<String, Object> tabs) {
        String hint = (String) tabs.get(CommandTriggerManager.TAB_HINT);
        String candidates_str = (String) tabs.get(CommandTriggerManager.TAB_CANDIDATES);
        List<Map<String, Object>> conditions = tabs.get(CommandTriggerManager.TAB_CONDITIONS) != null ? (List<Map<String, Object>>) tabs.get(CommandTriggerManager.TAB_CONDITIONS) : null;

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
            builder = builder.setConditionMap(toTabCompleterConditionMap(conditions));
        }
        return builder.build();
    }
    private Map<Integer, Set<ITabCompleter>> toTabCompleterMap(List<Map<String, Object>> tabs) {
        Map<Integer, Set<ITabCompleter>> tabMap = new HashMap<>();
        tabs.forEach((t) -> {
            int idx;
            if(t.containsKey(CommandTriggerManager.TAB_INDEX) && t.get(CommandTriggerManager.TAB_INDEX) instanceof Integer){
                idx = (int) t.get(CommandTriggerManager.TAB_INDEX);
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
            if(c.containsKey(CommandTriggerManager.TAB_INDEX) && c.get(CommandTriggerManager.TAB_INDEX) instanceof Integer)
                idx = (int) c.get(CommandTriggerManager.TAB_INDEX);
            else
                return;

            if(c.containsKey(CommandTriggerManager.TAB_REGEX) && c.get(CommandTriggerManager.TAB_REGEX) instanceof String) {
                try {
                    regexPattern = Pattern.compile((String) c.get(CommandTriggerManager.TAB_REGEX));
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
            trigger.setTabCompleterMap(toTabCompleterMap(tabs));
            return trigger;
        } catch (AbstractTriggerManager.TriggerInitFailedException | IOException e) {
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
}
