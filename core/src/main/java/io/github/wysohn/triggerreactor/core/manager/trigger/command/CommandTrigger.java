package io.github.wysohn.triggerreactor.core.manager.trigger.command;


import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import javax.inject.Inject;
import java.util.*;

public class CommandTrigger extends Trigger {
    @Inject
    Map<String, DynamicTabCompleter> tabCompleterMap = new HashMap<>();

    private ITabCompleter[] tabCompleters;

    @AssistedInject
    CommandTrigger(@Assisted TriggerInfo info, @Assisted String script) {
        super(info, script);

        tabCompleters = readTabCompleters(info);
    }

    private ITabCompleter[] readTabCompleters(TriggerInfo info) {
        if (info.getConfig().has(TABS)) {
            return convertLegacyFormat(info);
        } else {
            List<String> tabs = info.getConfig().get(TABCOMPLETER, List.class).orElse(new LinkedList());
            return completerListToCompleters(tabs);
        }
    }

    private ITabCompleter[] completerListToCompleters(List<String> tabs) {
        return tabs.stream().map(this::parseTabCompleterString).toArray(ITabCompleter[]::new);
    }

    private ITabCompleter[] convertLegacyFormat(TriggerInfo info) {
        return toTabCompleters(info.getConfig().get(TABS, List.class).orElse(new LinkedList()));
    }

    private ITabCompleter parseTabCompleterString(String candidates_str) {
        if (candidates_str.startsWith("$")) {
            return tabCompleterMap.getOrDefault(candidates_str, DynamicTabCompleter.Builder.of(candidates_str,
                            LinkedList::new)
                    .build());
        } else {
            return StaticTabCompleter.Builder.of()
                    .setCandidate(Optional.of(candidates_str).map(str -> ITabCompleter.list(str.split(",")))
                            .orElseGet(() -> ITabCompleter.list("")))
                    .build();
        }
    }

    private ITabCompleter[] toTabCompleters(List<Map<String, Object>> tabs) {
        return tabs.stream().map(this::toTabCompleter).toArray(ITabCompleter[]::new);
    }

    private ITabCompleter toTabCompleter(Map<String, Object> tabs) {
        String hint = (String) tabs.get(HINT);
        String candidates_str = (String) tabs.get(CANDIDATES);

        ITabCompleter tabCompleter;
        if (candidates_str != null && candidates_str.startsWith("$")) {
            tabCompleter = tabCompleterMap.getOrDefault(candidates_str, DynamicTabCompleter.Builder.of(candidates_str
                            , LinkedList::new)
                    .build());
        } else if (candidates_str == null && hint != null) {
            tabCompleter = StaticTabCompleter.Builder.withHint(hint).build();
        } else if (candidates_str != null && hint == null) {
            tabCompleter = StaticTabCompleter.Builder.of(candidates_str).build();
        } else {
            tabCompleter = StaticTabCompleter.Builder.withHint(hint)
                    .setCandidate(Optional.ofNullable(candidates_str)
                            .map(str -> ITabCompleter.list(str.split(",")))
                            .orElseGet(() -> ITabCompleter.list("")))
                    .build();
        }
        return tabCompleter;
    }

    public String[] getPermissions() {
        List<String> permissions = info.getConfig().get(PERMISSION, List.class).orElse(new ArrayList<>());
        return permissions.toArray(new String[0]);
    }

    public String[] getAliases() {
        List<String> aliases = info.getConfig().get(ALIASES, List.class).orElse(new ArrayList<>());
        return aliases.toArray(new String[0]);
    }

    public void setAliases(String[] aliases) {
        if (aliases == null) {
            info.getConfig().put(ALIASES, new String[0]);
        } else {
            info.getConfig().put(ALIASES, aliases);
        }

        notifyObservers();
    }

    public void setPermissions(String[] permissions) {
        if (permissions == null) {
            info.getConfig().put(PERMISSION, new String[0]);
        } else {
            info.getConfig().put(PERMISSION, permissions);
        }

        notifyObservers();
    }

    public ITabCompleter[] getTabCompleters() {
        return tabCompleters;
    }

    public void setTabCompleters(ITabCompleter[] tabCompleters) {
        if (tabCompleters == null) {
            this.tabCompleters = new ITabCompleter[0];
            info.getConfig().put(TABCOMPLETER, new String[0]);
        } else {
            this.tabCompleters = tabCompleters;
            info.getConfig().put(TABCOMPLETER, toStringArr(tabCompleters));
        }

        notifyObservers();
    }

    public void setTabCompleters(List<String> tabcompleterStrs){
        setTabCompleters(completerListToCompleters(tabcompleterStrs));
    }

    private String[] toStringArr(ITabCompleter[] tabCompleters) {
        return Arrays.stream(tabCompleters).map(ITabCompleter::asConfigString).toArray(String[]::new);
    }

    public boolean isSync() {
        return info.isSync();
    }

    public void setSync(boolean bool) {
        info.setSync(bool);

        notifyObservers();
    }

    private List<Map<String, Object>> fromTabCompleters(ITabCompleter[] completers) {
        throw new RuntimeException();
    }

    private Map<String, Object> fromTabCompleter(ITabCompleter completer) {
        Map<String, Object> out = new LinkedHashMap<>();
        //completer.
        return out;
    }
    static final String PERMISSION = "permissions";
    static final String ALIASES = "aliases";
    static final String TABCOMPLETER = "tabcompleter";
    static final String TABS = "tabs";
    static final String HINT = "hint";
    static final String CANDIDATES = "candidates";
}
