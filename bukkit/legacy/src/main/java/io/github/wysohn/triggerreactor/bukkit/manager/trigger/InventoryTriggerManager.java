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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitInventory;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitItemStack;
import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitPlayer;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.bukkit.tools.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager;
import io.github.wysohn.triggerreactor.tools.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryTriggerManager extends AbstractInventoryTriggerManager implements BukkitTriggerManager {
    public InventoryTriggerManager(TriggerReactor plugin) {
        super(plugin, new CommonFunctions(plugin), new File(plugin.getDataFolder(), "InventoryTrigger"));
    }

    @Override
    public <T> T getData(File file, String key, T def) throws IOException {
        if (key.equals(ITEMS)) {
            int size = BukkitTriggerManager.super.getData(file, SIZE, 0);
            Utf8YamlConfiguration conf = new Utf8YamlConfiguration();
            try {
                conf.load(file);
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }

            Map<Integer, IItemStack> items = new HashMap<>();

            if (conf.contains(ITEMS))
                parseItemsList(conf.getConfigurationSection(ITEMS), items, size);

            return (T) items;
        } else {
            return BukkitTriggerManager.super.getData(file, key, def);
        }
    }

    @Override
    public void setData(File file, String key, Object value) throws IOException {
        if (key.equals(ITEMS)) {
            Utf8YamlConfiguration conf = new Utf8YamlConfiguration();
            try {
                conf.load(file);
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }

            IItemStack[] items = (IItemStack[]) value;

            ConfigurationSection itemsSection;
            if (conf.contains(ITEMS))
                itemsSection = conf.getConfigurationSection(ITEMS);
            else
                itemsSection = conf.createSection(ITEMS);

            writeItemList(itemsSection, items);

            conf.save(file);
        } else {
            BukkitTriggerManager.super.setData(file, key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private void parseItemsList(ConfigurationSection itemSection, Map<Integer, IItemStack> items, int size) {
        for (int i = 0; i < size; i++) {
            if (itemSection.isConfigurationSection(String.valueOf(i))) { // 1.12.2 or below
                ConfigurationSection section = itemSection.getConfigurationSection(String.valueOf(i));

                Material type = Material.valueOf((String) section.get("Type", Material.DIRT.name()));
                int amount = section.getInt("Amount", 1);
                short data = (short) section.getInt("Data", 0);
                ItemMeta IM = (ItemMeta) section.get("Meta");

                ItemStack IS = new ItemStack(type, amount, data);
                if (IM == null)
                    IM = IS.getItemMeta();

                if (IM != null) {
                    //leave these for backward compatibility
                    String title = section.getString("Title", null);
                    Object lore = section.get("Lore", null);

                    if (title != null)
                        IM.setDisplayName(title);
                    if (lore != null && lore instanceof List)
                        IM.setLore((List<String>) lore);

                    IS.setItemMeta(IM);
                }

                items.put(i, new BukkitItemStack(IS));
            } else { // just leave it to bukkit
                ItemStack IS = itemSection.getItemStack(String.valueOf(i));
                if (IS != null) {
                    items.put(i, new BukkitItemStack(IS));
                }
            }
        }
    }

    private void writeItemList(ConfigurationSection itemSection, IItemStack[] items) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null)
                continue;

            ItemStack item = items[i].get();

            //leave it to bukkit
            itemSection.set(String.valueOf(i), item);
        }
    }

    /**
     * @param player
     * @param name
     * @return the opened Inventory's reference; null if no Inventory Trigger found
     */
    public IInventory openGUI(Player player, String name) {
        return openGUI(new BukkitPlayer(player), name);
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        Inventory inventory = e.getInventory();

        if (!this.hasInventoryOpen(new BukkitInventory(inventory)))
            return;
        InventoryTrigger trigger = getTriggerForOpenInventory(new BukkitInventory(inventory));

        Map<String, Object> varMap = getSharedVarsForInventory(new BukkitInventory(inventory));
        varMap.put("player", e.getPlayer());
        varMap.put("trigger", "open");

        trigger.activate(e, varMap);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        Inventory inventory = e.getInventory();

        if (!this.hasInventoryOpen(new BukkitInventory(inventory)))
            return;
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        Inventory inventory = e.getInventory();

        if (!this.hasInventoryOpen(new BukkitInventory(inventory)))
            return;
        InventoryTrigger trigger = getTriggerForOpenInventory(new BukkitInventory(inventory));

        // just always cancel if it's GUI
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player))
            return;

        if (e.getRawSlot() < 0)
            return;

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null)
            clickedItem = new ItemStack(Material.AIR);

        Map<String, Object> varMap = getSharedVarsForInventory(new BukkitInventory(inventory));
        varMap.put("item", clickedItem.clone());
        varMap.put("slot", e.getRawSlot());
        varMap.put("click", e.getClick().name());
        varMap.put("hotbar", e.getHotbarButton());
        varMap.put("trigger", "click");

        trigger.activate(e, varMap);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        onInventoryClose(e, new BukkitPlayer((Player) e.getPlayer()), new BukkitInventory(e.getInventory()));
    }

    @Override
    protected void fillInventory(InventoryTrigger trigger, int size, IInventory inventory) {
        Inventory inv = inventory.get();
        for (int i = 0; i < size; i++) {
            IItemStack item = trigger.getItems()[i];
            if (item != null) {
                inv.setItem(i, getColoredItem(item.get()));
            }
        }
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

    @Override
    protected void deleteInfo(Trigger trigger) {
        File yamlFile = new File(folder, trigger.getTriggerName() + ".yml");
        FileUtil.delete(yamlFile);
        File triggerFile = new File(folder, trigger.getTriggerName());
        FileUtil.delete(triggerFile);
    }

    @Override
    protected IInventory createInventory(int size, String name) {
        name = name.replaceAll("_", " ");
        name = ChatColor.translateAlternateColorCodes('&', name);
        return new BukkitInventory(Bukkit.createInventory(null, size, name));
    }
}
