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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.wysohn.triggerreactor.bukkit.main.TriggerReactor;
import io.github.wysohn.triggerreactor.bukkit.manager.TriggerManager;
import io.github.wysohn.triggerreactor.core.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.interpreter.Interpreter.ProcessInterrupter;
import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.Node;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class InventoryTriggerManager extends TriggerManager {
    private final Map<String, InventoryTrigger> invenTriggers = new ConcurrentHashMap<>();

    private final Map<Inventory, InventoryTrigger> inventoryMap = new ConcurrentHashMap<>();
    private final Map<Inventory, Map<String, Object>> inventorySharedVars = new ConcurrentHashMap<>();

    private final File folder;

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

            Map<Integer, ItemStack> items = new HashMap<>();
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

            yaml.set("Size", trigger.items.length);

            if(!yaml.isSet("Items"))
                yaml.createSection("Items");
            writeItemList(yaml.getConfigurationSection("Items"), trigger.items);

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

    private void parseItemsList(ConfigurationSection itemSection, Map<Integer, ItemStack> items, int size) {
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

            items.put(i, IS);
        }
    }

    private void writeItemList(ConfigurationSection itemSection, ItemStack[] items) {
        for(int i = 0; i < items.length; i++){
            if(items[i] == null)
                continue;

            if(!itemSection.isSet(String.valueOf(i)))
                itemSection.createSection(String.valueOf(i));
            ConfigurationSection section = itemSection.getConfigurationSection(String.valueOf(i));

            section.set("Type", items[i].getType().name());
            section.set("Amount", items[i].getAmount());
            section.set("Data", items[i].getDurability());
            if(items[i].hasItemMeta() && items[i].getItemMeta().hasDisplayName())
                section.set("Title", items[i].getItemMeta().getDisplayName());
            if(items[i].hasItemMeta() && items[i].getItemMeta().hasLore())
                section.set("Lore", items[i].getItemMeta().getLore());
        }
    }

    /**
     *
     * @param name
     * @return null if not exists
     */
    public InventoryTrigger getTriggerForName(String name){
        return invenTriggers.get(name);
    }

    /**
     *
     * @param name this can contain color code &, but you should specify exact name for the title.
     * @return true on success; false if already exist
     * @throws ParserException See {@link Trigger#init()}
     * @throws LexerException See {@link Trigger#init()}
     * @throws IOException See {@link Trigger#init()}
     */
    public boolean createTrigger(int size, String name, String script) throws IOException, LexerException, ParserException{
        if(invenTriggers.containsKey(name))
            return false;

        invenTriggers.put(name, new InventoryTrigger(size, name, new HashMap<>(), script));

        return true;
    }

    /**
     *
     * @param name
     * @return true on success; false if not exists
     */
    public boolean deleteTrigger(String name){
        if(!invenTriggers.containsKey(name))
            return false;

        invenTriggers.remove(name);
        File yamlFile = new File(folder, name+".yml");
        FileUtil.delete(yamlFile);
        File triggerFile = new File(folder, name);
        FileUtil.delete(triggerFile);

        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * @param player
     * @param name
     * @return the opened Inventory's reference; null if no Inventory Trigger found
     */
    public Inventory openGUI(Player player, String name){
        InventoryTrigger trigger = invenTriggers.get(name);
        if(trigger == null)
            return null;

        Inventory inventory = Bukkit.createInventory(null, trigger.items.length,
                ChatColor.translateAlternateColorCodes('&', name));
        inventoryMap.put(inventory, trigger);

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("inventory", inventory);
        inventorySharedVars.put(inventory, varMap);

        fillInventory(trigger, trigger.items.length, inventory);

        player.openInventory(inventory);

        return inventory;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void onOpen(InventoryOpenEvent e){
        Inventory inventory = e.getInventory();

        if (!inventoryMap.containsKey(inventory))
            return;
        InventoryTrigger trigger = inventoryMap.get(inventory);

        Map<String, Object> varMap = inventorySharedVars.get(inventory);
        insertPlayerVariables((Player) e.getPlayer(), varMap);
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
        if(e.getRawSlot() < trigger.items.length){
            if(trigger.items[e.getRawSlot()] == null)
                varMap.put("item", new ItemStack(Material.AIR));
            else
                varMap.put("item", trigger.items[e.getRawSlot()].clone());
        }
        varMap.put("slot", e.getRawSlot());
        varMap.put("click", e.getClick().name());
        varMap.put("hotbar", e.getHotbarButton());
        varMap.put("trigger", "click");

        trigger.activate(e, varMap);
    }

    /**
     *
     * @param trigger
     * @param size mutiple of 9; must be less than or equalt to 54 (exclusive)
     * @param inventory
     */
    private void fillInventory(InventoryTrigger trigger, int size, Inventory inventory) {
        for(int i = 0; i < size; i++){
            ItemStack item = trigger.items[i];
            if(item != null){
                inventory.setItem(i, getColoredItem(item));
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
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        Inventory inventory = e.getInventory();

        if (!inventoryMap.containsKey(inventory))
            return;
        InventoryTrigger trigger = inventoryMap.get(inventory);

        Map<String, Object> varMap = inventorySharedVars.get(inventory);
        insertPlayerVariables((Player) e.getPlayer(), varMap);
        varMap.put("trigger", "close");

        trigger.activate(e, varMap);

        inventoryMap.remove(inventory);
        inventorySharedVars.remove(inventory);
    }

    public class InventoryTrigger extends Trigger{
        final static int MAXSIZE = 6*9;

        final ItemStack[] items;

        private InventoryTrigger(String name, String script, ItemStack[] items) throws IOException, LexerException, ParserException {
            super(name, script);
            this.items = items;

            init();
        }

        public InventoryTrigger(int size, String name, Map<Integer, ItemStack> items, String script) throws IOException, LexerException, ParserException{
            super(name, script);
            if(size < 9 || size % 9 != 0)
                throw new IllegalArgumentException("Inventory Trigger size should be multiple of 9!");

            if(size > MAXSIZE)
                throw new IllegalArgumentException("Inventory Size cannot be larger than "+MAXSIZE);

            this.items = new ItemStack[size];

            for(Map.Entry<Integer, ItemStack> entry : items.entrySet()){
                this.items[entry.getKey()] = entry.getValue();
            }

            init();
        }

        @Override
        protected void start(Event e, Map<String, Object> scriptVars, Interpreter interpreter, boolean sync) {
            try{
                interpreter.startWithContextAndInterrupter(e, new ProcessInterrupter(){
                    @Override
                    public boolean onNodeProcess(Node node) {
                        if (interpreter.isCooldown()) {
                            if(e instanceof InventoryInteractEvent){
                                HumanEntity he = ((InventoryInteractEvent) e).getWhoClicked();
                                if(he instanceof Player){
                                    Player player = (Player) he;
                                    UUID uuid = player.getUniqueId();
                                    cooldowns.put(uuid, interpreter.getCooldownEnd());
                                }
                            }
                            return false;
                        }

                        //safety feature to stop all trigger immediately if executing on 'open' or 'click'
                        //  is still running after the inventory is closed.
                        //TODO: documentation about loop in 'close' can fail really bad
                        if(e instanceof InventoryOpenEvent
                                || e instanceof InventoryClickEvent){
                            Inventory inv = ((InventoryEvent) e).getInventory();

                            //it's not GUI so stop execution
                            if(!inventoryMap.containsKey(inv))
                                return true;
                        }

                        return false;
                    }

                    @Override
                    public boolean onCommand(Object context, String command, Object[] args) {
                        if("CALL".equals(command)){
                            if(args.length < 1)
                                throw new RuntimeException("Need parameter [String]");

                            if(args[0] instanceof String){
                                Trigger trigger = plugin.getNamedTriggerManager().getTriggerForName((String) args[0]);
                                if(trigger == null)
                                    throw new RuntimeException("No trigger found for Named Trigger "+args[0]);

                                trigger.activate(e, interpreter.getVars());
                                return true;
                            } else {
                                throw new RuntimeException("Parameter type not match; it should be a String."
                                        + " Make sure to put double quotes, if you provided String literal.");
                            }
                        } else if("CANCELEVENT".equals(command)) {
                            if(!sync)
                                throw new RuntimeException("CANCELEVENT is illegal in async mode!");

                            if(context instanceof Cancellable){
                                ((Cancellable) context).setCancelled(true);
                                return true;
                            } else {
                                throw new RuntimeException(context+" is not a Cancellable event!");
                            }
                        }

                        return false;
                    }

                });
            }catch(Exception ex){
                ex.printStackTrace();
                if(e instanceof PlayerEvent){
                    Player player = ((PlayerEvent) e).getPlayer();
                    player.sendMessage(ChatColor.RED+"Could not execute this trigger.");
                    player.sendMessage(ChatColor.RED+ex.getMessage());
                    player.sendMessage(ChatColor.RED+"If you are administrator, see console for details.");
                }
            }
        }

        @Override
        public Trigger clone() {
            try {
                return new InventoryTrigger(triggerName, script, items);
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
            }
            return null;
        }

        public ItemStack[] getItems() {
            return items;
        }
    }
}
