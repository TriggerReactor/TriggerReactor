package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;

public class SimpleUsage implements Usage{
    private final String message;

    public SimpleUsage(String message) {
        this.message = message;
    }

    @Override
    public void printUsage(ICommandSender sender) {
        sender.sendMessage(message);
    }
}
