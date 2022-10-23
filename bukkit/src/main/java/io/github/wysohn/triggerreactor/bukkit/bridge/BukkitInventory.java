/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.wysohn.triggerreactor.bukkit.bridge;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.tools.memento.IMemento;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

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
            return other.inventory == null;
        } else
            return inventory.equals(other.inventory);
    }

    @Override
    public void open(IPlayer player) {
        player.openInventory(this);
    }

    @Override
    public IItemStack getItem(int slot) {
        return Optional.ofNullable(inventory.getItem(slot))
                .map(BukkitItemStack::new)
                .orElse(null);
    }

    @Override
    public void setItem(int slot, IItemStack item) {
        inventory.setItem(slot, Optional.ofNullable(item)
                .map(IItemStack::get)
                .filter(ItemStack.class::isInstance)
                .map(ItemStack.class::cast)
                .orElse(null));
    }

    @Override
    public int getSize() {
        return inventory.getSize();
    }

    @Override
    public IMemento saveState() {
        return new InventoryState(inventory.getContents());
    }

    @Override
    public void restoreState(IMemento memento) {
        if (memento instanceof InventoryState) {
            inventory.setContents(((InventoryState) memento).items);
        } else {
            throw new IllegalArgumentException("Invalid memento type: " + memento.getClass());
        }
    }

    private static class InventoryState implements IMemento {
        private final ItemStack[] items;

        public InventoryState(ItemStack[] items) {
            this.items = items;
        }
    }
}
