package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.core.bridge.ICommand;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandMapHandler;
import org.bukkit.event.Listener;

public class LegacyBukkitCommandMapHandler implements ICommandMapHandler, Listener {
    private final CustomCommandHandle customCommandHandle;

    public LegacyBukkitCommandMapHandler(CustomCommandHandle customCommandHandle) {
        this.customCommandHandle = customCommandHandle;
    }

    @Override
    public void synchronizeCommandMap() throws NoSuchMethodException {
        // do nothing. Not really necessary atm for legacy versions
    }

    @Override
    public boolean unregister(String triggerName) {
        return customCommandHandle.remove(triggerName) != null;
    }

    @Override
    public boolean commandExist(String triggerName) {
        return customCommandHandle.containsKey(triggerName);
    }

    @Override
    public ICommand register(String triggerName, String[] aliases) throws Duplicated, NotInstantiated {
        return null;
    }
}
