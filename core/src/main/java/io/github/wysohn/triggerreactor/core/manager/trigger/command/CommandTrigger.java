package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import io.github.wysohn.triggerreactor.core.main.ITriggerReactorAPI;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import java.util.Arrays;

public class CommandTrigger extends Trigger {
    String[] permissions = new String[0];
    String[] aliases = new String[0];
    ITabCompleter[] tabCompleters = new ITabCompleter[0];

    public CommandTrigger(ITriggerReactorAPI api,
                          TriggerInfo info,
                          String script) throws AbstractTriggerManager.TriggerInitFailedException {
        super(api, info, script);

        init();
    }

    @Override
    public CommandTrigger clone() {
        try {
            return new CommandTrigger(api, info, getScript());
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "{" + "permissions=" + Arrays.toString(permissions) + ", aliases=" + Arrays.toString(
                aliases) + '}';
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

    public ITabCompleter[] getTabCompleters() {
        return tabCompleters;
    }

    public void setTabCompleters(ITabCompleter[] tabCompleters) {
        if (tabCompleters == null) {
            this.tabCompleters = new ITabCompleter[0];
        } else {
            this.tabCompleters = tabCompleters;
        }
    }
}
