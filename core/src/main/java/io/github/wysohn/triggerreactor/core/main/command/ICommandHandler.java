package io.github.wysohn.triggerreactor.core.main.command;

/**
 * Handle command registration.
 */
public interface ICommandHandler {
    /**
     * Register a command.
     * @param name the command name to register
     * @praam aliases aliases for the command
     * @return the command object; null if the command is already in use.
     */
    ICommand register(String name, String[] aliases);

    /**
     * Unregister a command.
     * @param name the command name to unregister
     * @return true if the command is successfully unregistered; false if the command is not registered.
     */
    boolean unregister(String name);

    /**
     * Synchronize the command map so that the changes are reflected to the server.
     * This is required for some API.
     */
    void sync();
}
