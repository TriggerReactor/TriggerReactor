package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractInventoryEditManager extends Manager {
    protected static final char X = '\u2718';
    protected static final char CHECK = '\u2713';
    protected static final char PENCIL = '\u270E';

    //map of player uuids to inventories representing the inventories currently being edited
    protected Map<UUID, InventoryTrigger> sessions = new HashMap<>();

    //inventories currently awaiting a save/discard/continue command
    protected Map<UUID, IInventory> suspended = new HashMap<>();

    //called by the "continue edit" button
    public abstract void continueEdit(IPlayer player);

    //called by the "don't save" button
    public abstract void discardEdit(IPlayer player);

    //called by the "save" button
    public abstract void saveEdit(IPlayer player);

    public abstract void startEdit(IPlayer player, InventoryTrigger trigger);

    //helper method to remove the player from both maps
    protected void stopEdit(IPlayer player) {
        UUID u = player.getUniqueId();
        sessions.remove(u);
        suspended.remove(u);
    }
}
