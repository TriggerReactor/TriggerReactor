package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryEditManager extends Manager {
    @Inject
    IInventoryModifier inventoryModifier;
    @Inject
    InventoryTriggerManager invManager;
    //map of player uuids to inventories representing the inventories currently being edited
    protected Map<UUID, InventoryTrigger> sessions = new HashMap<>();
    //inventories currently awaiting a save/discard/continue command
    protected Map<UUID, IInventory> suspended = new HashMap<>();

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() throws Exception {

    }

    @Override
    public void onReload() {
    }

    @Override
    public void saveAll() {
    }

    public void onContinueEdit(IPlayer player) {
        UUID u = player.getUniqueId();
        if (!suspended.containsKey(u)) {
            return;
        }
        IInventory inv = suspended.remove(u);
        player.openInventory(inv);
    }

    public void onDiscardEdit(IPlayer player) {
        if (!sessions.containsKey(player.getUniqueId())) {
            return;
        }
        stopEdit(player);
        player.sendMessage("Discarded edits");
    }

    //helper method to remove the player from both maps
    public void stopEdit(IPlayer player) {
        UUID u = player.getUniqueId();
        sessions.remove(u);
        suspended.remove(u);
    }

    public void onSaveEdit(IPlayer player) {
        UUID u = player.getUniqueId();
        if (!sessions.containsKey(u)) {
            return;
        }
        IInventory inv = suspended.get(u);
        InventoryTrigger trigger = sessions.get(u);

        IItemStack[] iitems = inv.getStorageContents();

        invManager.replaceItems(trigger, iitems);
        stopEdit(player);
        player.sendMessage("Saved edits");
    }

    public void onStartEdit(IPlayer player, InventoryTrigger trigger) {
        UUID u = player.getUniqueId();
        if (sessions.containsKey(u)) {
            return;
        }
        sessions.put(u, trigger);

        IItemStack[] iitems = trigger.getItems();

        IInventory inv = inventoryModifier.createInventory(trigger.getInfo().getTriggerName(), iitems);
        player.openInventory(inv);
    }

    public boolean hasSession(UUID u) {
        return sessions.containsKey(u);
    }

    public boolean hasSuspended(UUID u) {
        return suspended.containsKey(u);
    }

    public void putSuspend(UUID u, IInventory inv) {
        suspended.put(u, inv);
    }

    public interface IInventoryModifier {
        IInventory createInventory(String name, IItemStack[] contents);
    }
}
