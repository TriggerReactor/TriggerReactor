package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommand;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;

public interface ICommandExecutor {
    boolean onCommand(ICommandSender sender, ICommand command, String label, String[] args);
}
