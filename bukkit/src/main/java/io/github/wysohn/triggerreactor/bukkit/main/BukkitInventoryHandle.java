package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitItemStack;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.main.IInventoryHandle;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BukkitInventoryHandle implements IInventoryHandle<ItemStack> {
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
    public IItemStack wrapItemStack(ItemStack item) {
        return new BukkitItemStack(item);
    }

    @Override
    public IInventory createInventory(int size, String name) {
        name = name.replaceAll("_", " ");
        name = ChatColor.translateAlternateColorCodes('&', name);
        return BukkitTriggerReactorCore.getWrapper().wrap(Bukkit.createInventory(null, size, name));
    }
}
