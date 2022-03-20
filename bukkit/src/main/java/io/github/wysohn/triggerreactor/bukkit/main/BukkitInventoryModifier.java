package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.IInventoryModifier;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class BukkitInventoryModifier implements IInventoryModifier {
    @Inject
    Server server;
    @Inject
    IWrapper wrapper;

    @Inject
    public BukkitInventoryModifier() {
    }

    @Override
    public IInventory createInventory(int size, String name) {
        return createInventory(name, size, null);
    }

    @Override
    public IInventory createInventory(String name, IItemStack[] contents) {
        return createInventory(name, contents.length, contents);
    }

    private IInventory createInventory(String name, int size, IItemStack[] contents) {
        name = ChatColor.translateAlternateColorCodes('&', name);
        if(contents != null){
            ValidationUtil.assertTrue(contents.length, v -> v == size, "contents.length != size");
        }

        Inventory bukkitInventory = server.createInventory(null, size, name);
        if(contents != null){
            ItemStack[] itemStacks = new ItemStack[size];
            for(int i = 0; i < size; i++){
                itemStacks[i] = contents[i].get();
            }
            bukkitInventory.setContents(itemStacks);
        }

        return wrapper.wrap(bukkitInventory);
    }

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

    public void setItemTitle(IItemStack iS, String title) {
        ItemStack IS = iS.get();
        ItemMeta IM = IS.getItemMeta();
        IM.setDisplayName(title);
        IS.setItemMeta(IM);
    }

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
}
