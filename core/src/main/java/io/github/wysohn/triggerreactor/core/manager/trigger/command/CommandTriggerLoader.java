package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

class CommandTriggerLoader implements ITriggerLoader<CommandTrigger> {
    private final Map<String, ITabCompleter> tabCompleterMap = new HashMap<>();

    {
        tabCompleterMap.put("$playerlist", ITabCompleter.Builder.of(ITabCompleter.Template.PLAYER).build());
    }

    private ITabCompleter toTabCompleter(Map<String, Object> tabs) {
        String hint = (String) tabs.get(CommandTriggerManager.HINT);
        String candidates_str = (String) tabs.get(CommandTriggerManager.CANDIDATES);

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
