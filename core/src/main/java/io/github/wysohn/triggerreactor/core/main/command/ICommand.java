package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.manager.trigger.command.ITabCompleter;

/**
 * This interface is used to define a command. This is not the Command Trigger but
 * the actual command instance that is registered to the corresponding API.
 */
public interface ICommand {
    void setTabCompleters(ITabCompleter[] tabCompleters);

    void setExecutor(ICommandExecutor executor);
}
