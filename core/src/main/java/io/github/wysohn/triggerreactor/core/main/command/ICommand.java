package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.manager.trigger.command.ITabCompleter;

import java.util.Map;
import java.util.Set;

/**
 * This interface is used to define a command. This is not the Command Trigger but
 * the actual command instance that is registered to the corresponding API.
 */
public interface ICommand {
    String getName();

    void setTabCompleterMap(Map<Integer, Set<ITabCompleter>> tabCompleterMap);

    ICommandExecutor getExecutor();

    void setExecutor(ICommandExecutor executor);
}
