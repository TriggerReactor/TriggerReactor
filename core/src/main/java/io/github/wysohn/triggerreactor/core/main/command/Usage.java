package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;

@FunctionalInterface
public interface Usage {
    void printUsage(ICommandSender sender, int spaces);
}
