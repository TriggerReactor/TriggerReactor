package io.github.wysohn.triggerreactor.tools.script;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;

public class ScriptEditorUser {
    private final ICommandSender commandSender;

    public ScriptEditorUser(ICommandSender commandSender) {
        this.commandSender = commandSender;
    }

    public ICommandSender getCommandSender() {
        return commandSender;
    }

    public void sendMessage(String rawMessage) {
        commandSender.sendMessage(rawMessage, true);
    }
}
