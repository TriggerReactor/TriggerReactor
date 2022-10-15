package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import org.bukkit.command.Command;

import java.util.Map;

public interface ICommandMapHandler {
    Map<String, Command> getCommandMap();

    void synchronizeCommandMap();
}
