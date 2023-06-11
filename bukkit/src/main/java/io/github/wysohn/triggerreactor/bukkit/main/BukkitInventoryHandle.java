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

package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitInventory;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitItemStack;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.main.IInventoryHandle;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.memento.IMemento;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class BukkitInventoryHandle implements IInventoryHandle {

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

    @Override
    public void fillInventory(InventoryTrigger trigger, int size, IInventory inventory) {
        Inventory inv = inventory.get();
        for (int i = 0; i < size; i++) {
            IItemStack item = trigger.getItems()[i];
            if (item != null) {
                inv.setItem(i, getColoredItem(item.get()));
            }
        }
    }

    @Override
    public Class<ItemStack> getItemClass() {
        return ItemStack.class;
    }

    @Override
    public IItemStack wrapItemStack(Object item) {
        ValidationUtil.validate(item instanceof ItemStack,
                "ItemStack is not an instance of " + ItemStack.class.getName());

        return new BukkitItemStack((ItemStack) item);
    }

    @Override
    public void setContents(IInventory inv, IItemStack[] items) {
        IMemento memento = inv.saveState();
        try {
            for (int i = 0; i < items.length; i++) {
                inv.setItem(i, items[i]);
            }
        } catch (Exception e) {
            // all or nothing
            inv.restoreState(memento);
            throw e;
        }
    }

    @Override
    public IItemStack[] getContents(IInventory inv) {
        IItemStack[] items = new IItemStack[inv.getSize()];
        for (int i = 0; i < items.length; i++) {
            items[i] = inv.getItem(i);
        }
        return items;
    }

    @Override
    public IInventory createInventory(int size, String name) {
        name = name.replaceAll("_", " ");
        name = ChatColor.translateAlternateColorCodes('&', name);
        return new BukkitInventory(Bukkit.createInventory(null, size, name));
    }

    @Override
    public void setItemTitle(IItemStack iS, String title) {
        ItemStack IS = iS.get();
        ItemMeta IM = IS.getItemMeta();
        IM.setDisplayName(title);
        IS.setItemMeta(IM);
    }

    @Override
    public void addItemLore(IItemStack iS, String lore) {
        ItemStack IS = iS.get();

        ItemMeta IM = IS.getItemMeta();
        List<String> lores = IM.hasLore() ? IM.getLore() : new ArrayList<>();
        lores.add(lore);
        IM.setLore(lores);
        IS.setItemMeta(IM);
    }

    @Override
    public boolean setLore(IItemStack iS, int index, String lore) {
        ItemStack IS = iS.get();

        ItemMeta IM = IS.getItemMeta();
        List<String> lores = IM.hasLore() ? IM.getLore() : new ArrayList<>();
        if (lore == null || index < 0 || index > lores.size() - 1)
            return false;

        lores.set(index, lore);
        IM.setLore(lores);
        IS.setItemMeta(IM);

        return true;
    }

    @Override
    public boolean removeLore(IItemStack iS, int index) {
        ItemStack IS = iS.get();

        ItemMeta IM = IS.getItemMeta();
        List<String> lores = IM.getLore();
        if (lores == null || index < 0 || index > lores.size() - 1)
            return false;

        lores.remove(index);
        IM.setLore(lores);
        IS.setItemMeta(IM);

        return true;
    }
}
