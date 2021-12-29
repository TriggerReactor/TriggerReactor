package io.github.wysohn.triggerreactor.core.manager.trigger.command;


import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

import java.util.Arrays;

public class CommandTrigger extends Trigger {
    String[] permissions = new String[0];
    String[] aliases = new String[0];
    ITabCompleter[] tabCompleters = new ITabCompleter[0];

    @AssistedInject
    CommandTrigger(@Assisted TriggerInfo info, @Assisted String script) {
        super(info, script);
    }

    public CommandTrigger(Trigger o) {
        super(o);
        ValidationUtil.assertTrue(o, v -> v instanceof CommandTrigger);
        CommandTrigger other = (CommandTrigger) o;

        this.permissions = other.permissions;
        this.aliases = other.aliases;
        this.tabCompleters = other.tabCompleters;
    }

    @Override
    public String toString() {
        return super.toString() + "{permissions=" + Arrays.toString(permissions) + ", aliases=" + Arrays.toString(
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

        notifyObservers();
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

        notifyObservers();
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

        notifyObservers();
    }
}
