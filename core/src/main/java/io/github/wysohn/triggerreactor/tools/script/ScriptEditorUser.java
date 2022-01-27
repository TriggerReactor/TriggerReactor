package io.github.wysohn.triggerreactor.tools.script;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ScriptEditorUser that = (ScriptEditorUser) o;
        return Objects.equals(commandSender, that.commandSender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandSender);
    }
}
