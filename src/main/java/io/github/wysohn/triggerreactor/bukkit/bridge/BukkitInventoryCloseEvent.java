package io.github.wysohn.triggerreactor.bukkit.bridge;

import org.bukkit.event.inventory.InventoryCloseEvent;

import io.github.wysohn.triggerreactor.core.manager.event.IInventoryCloseEvent;

public class BukkitInventoryCloseEvent implements IInventoryCloseEvent {
    private final InventoryCloseEvent e;

    public BukkitInventoryCloseEvent(InventoryCloseEvent e) {
        super();
        this.e = e;
    }

}
