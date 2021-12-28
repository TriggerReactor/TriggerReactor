package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommand;

public interface ICommandMapHandler {
    void synchronizeCommandMap();

    boolean unregister(String triggerName);

    boolean commandExist(String triggerName);

    default ICommand register(String triggerName) throws Duplicated {
        return register(triggerName, new String[0]);
    }

    ICommand register(String triggerName, String[] aliases) throws Duplicated;

    class Duplicated extends Exception {

    }
}
