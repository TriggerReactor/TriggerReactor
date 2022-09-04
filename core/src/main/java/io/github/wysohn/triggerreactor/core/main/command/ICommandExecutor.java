package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;

public interface ICommandExecutor {
    void execute(ICommandSender sender, String label, String[] args);
}
