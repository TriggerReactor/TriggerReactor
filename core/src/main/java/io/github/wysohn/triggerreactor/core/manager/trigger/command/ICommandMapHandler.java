package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import io.github.wysohn.triggerreactor.core.bridge.ICommand;

public interface ICommandMapHandler {
    /**
     * Synchronize the command map if necessary. This may or may not have
     * any effect depending on the underlying implementation (ex. Bukkit API vs Sponge
     * vs etc.)
     */
    void synchronizeCommandMap();

    /**
     * Unregister the command.
     * @param commandName name of the command
     * @return true if the command is unregistered; false if the command didn't exist
     */
    boolean unregister(String commandName);

    /**
     * Check if the given command is registered.
     * @param commandName name of the command
     * @return true if the command is registered; false if the command doesn't exist
     */
    boolean commandExist(String commandName);

    /**
     * Get the command object by the name.
     * @param commandName name of the command
     * @return the handle for the registered command.
     */
    default ICommand register(String commandName) throws Duplicated {
        return register(commandName, new String[0]);
    }

    /**
     * Register a command.
     * @param commandName name of the command
     * @param aliases aliases of the command
     * @return the handle for the registered command.
     */
    ICommand register(String commandName, String[] aliases) throws Duplicated;


    class Duplicated extends Exception {

    }
}
