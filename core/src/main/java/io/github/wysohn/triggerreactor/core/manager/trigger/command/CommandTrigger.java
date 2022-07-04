package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import java.util.*;

public class CommandTrigger extends Trigger {
    String[] permissions = new String[0];
    String[] aliases = new String[0];
    Map<Integer, Set<ITabCompleter>> tabCompleterMap = new HashMap<>();

    public CommandTrigger(TriggerInfo info, String script) throws AbstractTriggerManager.TriggerInitFailedException {
        super(info, script);

        init();
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
