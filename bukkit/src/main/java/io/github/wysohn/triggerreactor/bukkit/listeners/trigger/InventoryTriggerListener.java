package io.github.wysohn.triggerreactor.bukkit.listeners.trigger;

import io.github.wysohn.triggerreactor.bukkit.listeners.AbstractBukkitListener;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import javax.inject.Inject;

public final class InventoryTriggerListener extends AbstractBukkitListener {
    @Inject
    InventoryTriggerManager inventoryTriggerManager;
    @Inject
    IWrapper wrapper;

    @Inject
    InventoryTriggerListener() {

    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        IInventory inventory = wrapper.wrap(e.getInventory());

        if (!inventoryTriggerManager.isTrackedInventory(inventory))
            return;

        if (!(e.getWhoClicked() instanceof Player))
            return;

        // just always cancel if it's GUI
        e.setCancelled(true);

        inventoryTriggerManager.onClick(e, inventory, e.getCurrentItem(), e.getRawSlot(), e.getClick().name(),
                e.getHotbarButton());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        IPlayer bukkitPlayer = wrapper.wrap(e.getPlayer());
        IInventory inventory = wrapper.wrap(e.getInventory());

        if (!inventoryTriggerManager.isTrackedInventory(inventory))
            return;

        inventoryTriggerManager.onClose(e, bukkitPlayer, inventory);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        IInventory inventory = wrapper.wrap(e.getInventory());

        if (!inventoryTriggerManager.isTrackedInventory(inventory))
            return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        IInventory inventory = wrapper.wrap(e.getInventory());

        if (!inventoryTriggerManager.isTrackedInventory(inventory))
            return;

        inventoryTriggerManager.onOpen(e, e.getPlayer(), inventory);
    }
}
