/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import io.github.wysohn.triggerreactor.bridge.IInventory;
import io.github.wysohn.triggerreactor.bridge.IItemStack;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitInventory;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitItemStack;
import io.github.wysohn.triggerreactor.bukkit.bridge.player.BukkitPlayer;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class InventoryTriggerManager extends AbstractInventoryTriggerManager {
    final File folder;

    public InventoryTriggerManager(TriggerReactor plugin) {
        super(plugin);

        folder = new File(plugin.getDataFolder(), "InventoryTrigger");
        if(!folder.exists())
            folder.mkdirs();

        reload();
    }

    @Override
    public void reload() {
        FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".yml");
            }
        };

        for(File file : folder.listFiles(filter)){
            String fileName = file.getName();
            String triggerName = fileName.substring(0, fileName.indexOf('.'));

            File triggerFile = new File(folder, triggerName);
            if(!triggerFile.exists()){
                plugin.getLogger().warning(triggerFile+" does not exists!");
                plugin.getLogger().warning(triggerFile+" is skipped.");
                continue;
            }

            if(triggerFile.isDirectory()){
                plugin.getLogger().warning(triggerFile+" should be a file not a directory!");
                plugin.getLogger().warning(triggerFile+" is skipped.");
                continue;
            }

            Utf8YamlConfiguration yaml = new Utf8YamlConfiguration();
            try {
                yaml.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load inventory trigger "+triggerName);
                continue;
            }

            if(!yaml.contains("Size")){
                plugin.getLogger().warning("Could not find Size: for inventory trigger "+triggerName);
                continue;
            }
            int size = yaml.getInt("Size");
            if(size % 9 != 0){
                plugin.getLogger().warning("Could not load inventory trigger "+triggerName);
                plugin.getLogger().warning("Size: must be multiple of 9!");
                continue;
            }
            if(size > InventoryTrigger.MAXSIZE){
                plugin.getLogger().warning("Could not load inventory trigger "+triggerName);
                plugin.getLogger().warning("Size: cannot be larger than "+InventoryTrigger.MAXSIZE);
                continue;
            }

            Map<Integer, IItemStack> items = new HashMap<>();
            ConfigurationSection itemSection = yaml.getConfigurationSection("Items");
            if(itemSection == null){
                plugin.getLogger().warning("Could not find Items: for inventory trigger "+triggerName);
                continue;
            }

            parseItemsList(itemSection, items, size);

            //boolean isSync = yaml.getBoolean("Sync", false);

            String script = null;
            try {
                script = FileUtil.readFromFile(triggerFile);
            } catch (IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load inventory trigger "+triggerName);
                continue;
            }

            InventoryTrigger trigger = null;
            try {
                trigger = new InventoryTrigger(size, triggerName, items, script);
                //trigger.setSync(isSync);
            } catch (IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load inventory trigger "+triggerName);
                continue;
            } catch (LexerException | ParserException e){
                e.printStackTrace();
                plugin.getLogger().warning("Failed to interpret the script for "+triggerName);
                continue;
            }

            invenTriggers.put(triggerName, trigger);
        }
    }

    @Override
    public void saveAll() {
        for(Entry<String, InventoryTrigger> entry : invenTriggers.entrySet()){
            String triggerName = entry.getKey();
            InventoryTrigger trigger = entry.getValue();

            File yamlFile = new File(folder, triggerName+".yml");
            if(!yamlFile.exists()){
                try {
                    yamlFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Utf8YamlConfiguration yaml = new Utf8YamlConfiguration();
            try {
                yaml.load(yamlFile);
            } catch (IOException | InvalidConfigurationException e1) {
                e1.printStackTrace();
            }

            yaml.set("Size", trigger.getItems().length);

            if(!yaml.isSet("Items"))
                yaml.createSection("Items");
            writeItemList(yaml.getConfigurationSection("Items"), trigger.getItems());

            //yaml.set("Sync", trigger.isSync());

            try {
                FileUtil.writeToFile(new File(folder, triggerName), trigger.getScript());
            } catch (IOException e1) {
                e1.printStackTrace();
                plugin.getLogger().warning("Could not save "+triggerName);
            }

            try {
                yaml.save(yamlFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseItemsList(ConfigurationSection itemSection, Map<Integer, IItemStack> items, int size) {
        for(int i = 0; i < size; i++){
            ConfigurationSection section = itemSection.getConfigurationSection(String.valueOf(i));
            if(section == null)
                continue;

            Material type = Material.valueOf((String) section.get("Type", Material.DIRT.name()));
            int amount = section.getInt("Amount", 1);
            short data = (short) section.getInt("Data", 0);

            ItemStack IS = new ItemStack(type, amount, data);
            ItemMeta IM = IS.getItemMeta();
            if(IM != null){
                String title = section.getString("Title", null);
                List<String> lore = section.getStringList("Lore");

                if(title != null)
                    IM.setDisplayName(title);
                if(lore != null)
                    IM.setLore(lore);
                IS.setItemMeta(IM);
            }

            items.put(i, new BukkitItemStack(IS));
        }
    }

    private void writeItemList(ConfigurationSection itemSection, IItemStack[] items) {
        for(int i = 0; i < items.length; i++){
            if(items[i] == null)
                continue;

            ItemStack item = items[i].get();

            if(!itemSection.isSet(String.valueOf(i)))
                itemSection.createSection(String.valueOf(i));
            ConfigurationSection section = itemSection.getConfigurationSection(String.valueOf(i));

            section.set("Type", item.getType().name());
            section.set("Amount", item.getAmount());
            section.set("Data", item.getDurability());
            if(item.hasItemMeta() && item.getItemMeta().hasDisplayName())
                section.set("Title", item.getItemMeta().getDisplayName());
            if(item.hasItemMeta() && item.getItemMeta().hasLore())
                section.set("Lore", item.getItemMeta().getLore());
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e){
        Inventory inventory = e.getInventory();

        if (!inventoryMap.containsKey(inventory))
            return;
        InventoryTrigger trigger = inventoryMap.get(inventory);

        Map<String, Object> varMap = inventorySharedVars.get(inventory);
        varMap.put("player", e.getPlayer());
        varMap.put("trigger", "open");

        trigger.activate(e, varMap);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        if (!inventoryMap.containsKey(e.getInventory()))
            return;
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        Inventory inventory = e.getInventory();

        if (!inventoryMap.containsKey(inventory))
            return;
        InventoryTrigger trigger = inventoryMap.get(inventory);

        // just always cancel if it's GUI
        e.setCancelled(true);

        if(!(e.getWhoClicked() instanceof Player))
            return;

        if(e.getRawSlot() < 0)
            return;

        Map<String, Object> varMap = inventorySharedVars.get(inventory);
        if(e.getRawSlot() < trigger.getItems().length){
            if(trigger.getItems()[e.getRawSlot()] == null)
                varMap.put("item", new ItemStack(Material.AIR));
            else{
                ItemStack item = trigger.getItems()[e.getRawSlot()].get();
                varMap.put("item", item.clone());
            }
        }
        varMap.put("slot", e.getRawSlot());
        varMap.put("click", e.getClick().name());
        varMap.put("hotbar", e.getHotbarButton());
        varMap.put("trigger", "click");

        trigger.activate(e, varMap);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        onInventoryClose(e, new BukkitPlayer((Player) e.getPlayer()), new BukkitInventory(e.getInventory()));
    }

    @Override
    protected void fillInventory(InventoryTrigger trigger, int size, IInventory inventory) {
        Inventory inv = inventory.get();
        for(int i = 0; i < size; i++){
            IItemStack item = trigger.getItems()[i];
            if(item != null){
                inv.setItem(i, getColoredItem(item.get()));
            }
        }
    }

    /**
    *
    * @param item
    * @return copy of colored item
    */
   private ItemStack getColoredItem(ItemStack item) {
       item = item.clone();
       ItemMeta IM = item.getItemMeta();

       if(IM != null){
           if(IM.hasDisplayName()){
               IM.setDisplayName(ChatColor.translateAlternateColorCodes('&', IM.getDisplayName()));
           }

           if(IM.hasLore()){
               List<String> lore = new ArrayList<>(IM.getLore());
               for(int i = 0; i < IM.getLore().size(); i++){
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
        File yamlFile = new File(folder, trigger.getTriggerName()+".yml");
        FileUtil.delete(yamlFile);
        File triggerFile = new File(folder, trigger.getTriggerName());
        FileUtil.delete(triggerFile);
    }
}
