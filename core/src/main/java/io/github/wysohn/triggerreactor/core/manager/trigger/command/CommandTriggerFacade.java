package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerFacade;

import java.util.Map;
import java.util.Set;

public class CommandTriggerFacade extends TriggerFacade {
    public CommandTriggerFacade(CommandTrigger trigger) {
        super(trigger);
    }

    public String[] getPermissions() {
        return ((CommandTrigger) trigger).getPermissions();
    }

    public void setPermissions(String[] permissions) {
        ((CommandTrigger) trigger).setPermissions(permissions);
    }

    public String[] getAliases() {
        return ((CommandTrigger) trigger).getAliases();
    }

    public Map<Integer, Set<ITabCompleter>> getTabCompleterMap() {
        return ((CommandTrigger) trigger).getTabCompleterMap();
    }

    public ICommand getCommand() {
        return ((CommandTrigger) trigger).getCommand();
    }
}
