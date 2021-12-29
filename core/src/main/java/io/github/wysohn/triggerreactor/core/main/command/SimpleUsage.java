package io.github.wysohn.triggerreactor.core.main.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.tools.StringUtils;

public class SimpleUsage implements Usage{
    private final String message;

    public SimpleUsage(String message) {
        this.message = message;
    }

    @Override
    public void printUsage(ICommandSender sender, int spaces) {
        sender.sendMessage(StringUtils.spaces(Math.max(0, spaces)) + message);
    }
}
