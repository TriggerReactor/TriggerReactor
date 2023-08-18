package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;

public interface ICommandExecutor {
    /**
     * Define behavior when the command is executed.
     *
     * @param sender   who executed the command
     * @param label    the label of the command
     * @param args     the arguments of the command
     * @param original the original command instance. Null if this executor is
     *                 handling a newly created command and not replacing an
     *                 existing one.
     */
    void execute(ICommandSender sender, String label, String[] args, ICommand original);

    default void execute(ICommandSender sender, String label, String[] args) {
        execute(sender, label, args, null);
    }
}
