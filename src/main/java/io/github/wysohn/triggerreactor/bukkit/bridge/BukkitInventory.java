package io.github.wysohn.triggerreactor.bukkit.bridge;

import org.bukkit.inventory.Inventory;

import io.github.wysohn.triggerreactor.bridge.IInventory;

public class BukkitInventory implements IInventory {
    private final Inventory inventory;
    public BukkitInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public <T> T get() {
        return (T) inventory;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((inventory == null) ? 0 : inventory.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BukkitInventory other = (BukkitInventory) obj;
        if (inventory == null) {
            if (other.inventory != null)
                return false;
        } else if (!inventory.equals(other.inventory))
            return false;
        return true;
    }

}
