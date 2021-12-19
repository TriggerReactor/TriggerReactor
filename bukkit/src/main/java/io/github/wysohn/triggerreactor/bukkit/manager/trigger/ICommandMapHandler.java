package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorMain;
import org.bukkit.command.Command;

import java.util.Map;

public interface ICommandMapHandler {
    Map<String, Command> getCommandMap(TriggerReactorMain plugin);

    void synchronizeCommandMap();
}
