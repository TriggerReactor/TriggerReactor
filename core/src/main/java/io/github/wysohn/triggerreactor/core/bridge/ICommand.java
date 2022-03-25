package io.github.wysohn.triggerreactor.core.bridge;

import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandExecutor;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ITabCompleter;

import java.util.List;

/**
 * Wrapper of command instance.
 */
public interface ICommand {
    void setAliases(List<String> collect);

    void setTabCompleter(ITabCompleter[] tabCompleter);

    void setExecutor(ICommandExecutor executor);
}
