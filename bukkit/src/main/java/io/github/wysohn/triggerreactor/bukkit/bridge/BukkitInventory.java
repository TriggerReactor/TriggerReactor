/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.bukkit.bridge;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BukkitInventory implements IInventory {
    private final Inventory inventory;

    BukkitInventory(Inventory inventory) {
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
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BukkitInventory other = (BukkitInventory) obj;
        if (inventory == null) {
            return other.inventory == null;
        } else return inventory.equals(other.inventory);
    }

    @Override
    public IItemStack[] getStorageContents() {
        ItemStack[] storage = inventory.getStorageContents();
        IItemStack[] out = new IItemStack[storage.length];
        for (int i = 0; i < storage.length; i++) {
            out[i] = new BukkitItemStack(storage[i] == null ? new ItemStack(Material.AIR) : storage[i]);
        }
        return out;
    }

    @Override
    public void setContents(IItemStack[] items, boolean colorItems) {
        ItemStack[] contents = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            if(items[i] == null)
                continue;

            contents[i] = items[i].get();
            if(colorItems)
                contents[i] = getColoredItem(contents[i]);
        }
        inventory.setContents(contents);
    }

    /**
     * @param item
     * @return copy of colored item
     */
    private ItemStack getColoredItem(ItemStack item) {
        item = item.clone();
        ItemMeta IM = item.getItemMeta();

        if (IM != null) {
            if (IM.hasDisplayName()) {
                IM.setDisplayName(ChatColor.translateAlternateColorCodes('&', IM.getDisplayName()));
            }

            if (IM.hasLore()) {
                List<String> lore = new ArrayList<>(IM.getLore());
                for (int i = 0; i < IM.getLore().size(); i++) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', IM.getLore().get(i)));
                }
                IM.setLore(lore);
            }

            item.setItemMeta(IM);
        }

        return item;
    }
}
