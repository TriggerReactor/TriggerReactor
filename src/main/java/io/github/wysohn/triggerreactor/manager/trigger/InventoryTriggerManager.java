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
package io.github.wysohn.triggerreactor.manager.trigger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import io.github.wysohn.triggerreactor.core.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.TriggerManager;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.tools.CustomSkullType;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class InventoryTriggerManager extends TriggerManager {
    private final Map<String, InventoryTrigger> invenTriggers = new ConcurrentHashMap<>();
    private final Map<Inventory, InventoryTrigger> inventoryMap = new ConcurrentHashMap<>();

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

            File triggerFolder = new File(folder, triggerName);
            if(!triggerFolder.isDirectory()){
                plugin.getLogger().warning(triggerFolder+" is not a directory!");
                continue;
            }else if(!triggerFolder.exists()){
                triggerFolder.mkdirs();
            }

            Utf8YamlConfiguration yaml = new Utf8YamlConfiguration();
            try {
                yaml.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load inventory trigger "+triggerName);
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

            Map<Integer, ItemStack> items = new HashMap<>();
            ConfigurationSection itemSection = yaml.getConfigurationSection("Items");
            if(itemSection == null){
                plugin.getLogger().warning("Could not find Items: for inventory trigger "+triggerName);
                continue;
            }

            parseItemsList(itemSection, items, size);

            Map<Integer, String> scriptMap = new HashMap<>();
            for(int i = 0; i < size; i++){
                File slotTrigger = new File(triggerFolder, String.valueOf(i));
                if(!slotTrigger.exists())
                    continue;

                if(!slotTrigger.isFile())
                    continue;

                String script = null;
                try {
                    script = FileUtil.readFromFile(slotTrigger);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                scriptMap.put(i, script);
            }

            try {
                invenTriggers.put(triggerName, new InventoryTrigger(size, items, scriptMap));
            } catch (InvalidSlotException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load inventory trigger "+triggerName);
            }
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

            yaml.set("Size", trigger.slots.length);

            if(!yaml.isSet("Items"))
                yaml.createSection("Items");
            writeItemList(yaml.getConfigurationSection("Items"), trigger.items);

            File slotFolder = new File(folder, triggerName);
            if(!slotFolder.exists()){
                slotFolder.mkdirs();
            }

            for(int i = 0; i < trigger.slots.length; i++){
                File slotFile = new File(slotFolder, String.valueOf(i));
                InventoryTrigger.InventorySlot slot = trigger.slots[i];

                if(slot == null){
                    slotFile.delete();
                } else{
                    try{
                        FileUtil.writeToFile(slotFile, slot.getScript());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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
            String title = section.getString("Title", null);
            List<String> lore = section.getStringList("Lore");

            ItemStack IS = new ItemStack(type, amount, data);
            ItemMeta IM = IS.getItemMeta();
            if(title != null)
                IM.setDisplayName(title);
            if(lore != null)
                IM.setLore(lore);
            IS.setItemMeta(IM);

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
     * @param name
     * @return true on success; false if already exist
     */
    public boolean createTrigger(int size, String name){
        if(invenTriggers.containsKey(name))
            return false;

        try {
            invenTriggers.put(name, new InventoryTrigger(size, new HashMap<>(), new HashMap<>()));
        } catch (InvalidSlotException e) {
            e.printStackTrace();
        }

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
        File slotFolder = new File(folder, name);
        FileUtil.delete(slotFolder);

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

        Inventory inventory = Bukkit.createInventory(null, 6*9, getTitleWithPage(1, name));
        inventoryMap.put(inventory, trigger);

        fillInventory(trigger, 1, inventory);

        player.openInventory(inventory);

        return inventory;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////

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

        String name = extractNameFromTitle(inventory.getTitle());
        int page = extractPageFromTitle(inventory.getTitle());

        if(e.getRawSlot() < 0)
            return;

        // check if navigation button
        if (handleNavigation((Player) e.getWhoClicked(), name, trigger, e.getRawSlot(), page))
            return;

        Map<String, Object> varMap = new HashMap<>();
        insertPlayerVariables((Player) e.getWhoClicked(), varMap);
        varMap.put("inventory", e.getInventory());
        varMap.put("item", e.getCurrentItem());
        varMap.put("slot", e.getRawSlot());

        trigger.activate(e, varMap);
    }

    private boolean handleNavigation(Player player, String name, InventoryTrigger trigger, int rawSlot, int currentPage) {
        int size = 6*9;

        Inventory inventory;
        switch(rawSlot){
        case 45://first
            inventory = Bukkit.createInventory(null, size, getTitleWithPage(1, name));
            inventoryMap.put(inventory, trigger);

            fillInventory(trigger, 1, inventory);

            player.openInventory(inventory);
            return true;
        case 46://previous
            int previous = Math.max(1, currentPage - 1);
            inventory = Bukkit.createInventory(null, size, getTitleWithPage(previous, name));
            inventoryMap.put(inventory, trigger);

            fillInventory(trigger, previous, inventory);

            player.openInventory(inventory);
            return true;
        case 52://next
            int next = Math.min(trigger.pageSize - 1, currentPage + 1);
            inventory = Bukkit.createInventory(null, size, getTitleWithPage(next, name));
            inventoryMap.put(inventory, trigger);

            fillInventory(trigger, next, inventory);

            player.openInventory(inventory);
            return true;
        case 53://last
            inventory = Bukkit.createInventory(null, size, getTitleWithPage(trigger.pageSize - 1, name));
            inventoryMap.put(inventory, trigger);

            fillInventory(trigger, trigger.pageSize - 1, inventory);

            player.openInventory(inventory);
            return true;
        default:
            return false;
        }
    }

    /**
     *
     * @param trigger
     * @param page 1~
     * @param inventory
     */
    private void fillInventory(InventoryTrigger trigger, int page, Inventory inventory) {
        int firstSlot = (page - 1)*InventoryTrigger.SLOTSPERPAGE;

        for(int i = firstSlot; i < firstSlot + 45; i++){
            ItemStack item = trigger.items[i];
            if(item == null){
                if(trigger.slots[i] == null){
                    item = new ItemStack(Material.AIR);
                }else {
                    item = new ItemStack(Material.STONE);
                    ItemMeta IM = item.getItemMeta();
                    IM.setDisplayName(i+". Item Not Set");
                    item.setItemMeta(IM);
                }
            }

            inventory.setItem(i % InventoryTrigger.SLOTSPERPAGE, getColoredItem(item));
        }

        putNavigationButtons(inventory);
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

    private static final String NAVIGATION_BUTTON_FIRST = ChatColor.GOLD+"<<";
    private static final String NAVIGATION_BUTTON_PREVIOUS = ChatColor.GOLD+"<";
    private static final String NAVIGATION_BUTTON_NEXT = ChatColor.GOLD+">";
    private static final String NAVIGATION_BUTTON_LAST = ChatColor.GOLD+">>";
    private void putNavigationButtons(Inventory inv){
        ItemStack skullFirst = getSkull(CustomSkullType.ARROW_UP, NAVIGATION_BUTTON_FIRST,
                ChatColor.LIGHT_PURPLE+"FIRST");
        ItemStack skullPrevious = getSkull(CustomSkullType.ARROW_LEFT, NAVIGATION_BUTTON_PREVIOUS,
                ChatColor.LIGHT_PURPLE+"PREVIOUS");
        ItemStack skullNext = getSkull(CustomSkullType.ARROW_RIGHT, NAVIGATION_BUTTON_NEXT,
                ChatColor.LIGHT_PURPLE+"NEXT");
        ItemStack skullLast = getSkull(CustomSkullType.ARROW_DOWN, NAVIGATION_BUTTON_LAST,
                ChatColor.LIGHT_PURPLE+"LAST");

        int firstSlotOfSixthLine = 5*9;
        inv.setItem(firstSlotOfSixthLine + 0, skullFirst);
        inv.setItem(firstSlotOfSixthLine + 1, skullPrevious);
        inv.setItem(firstSlotOfSixthLine + 7, skullNext);
        inv.setItem(firstSlotOfSixthLine + 8, skullLast);
    }

    private ItemStack getSkull(CustomSkullType type, String name, String... lore){
        ItemStack IS = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta IM = IS.getItemMeta();
        SkullMeta SM = (SkullMeta) IM;

        SM.setOwner(type.getOwner());
        SM.setDisplayName(name);
        List<String> lores = new ArrayList<String>();
        for(String str : lore)
            lores.add(str);
        SM.setLore(lores);

        IS.setItemMeta(SM);

        return IS;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        Inventory inventory = e.getInventory();
        inventoryMap.remove(inventory);
    }

    public class InventoryTrigger extends Trigger{
        final static int SLOTSPERPAGE = 5*9;

        final ItemStack[] items;
        /**
         * it's save to assume that slot length is always multiple of 9
         */
        final InventorySlot[] slots;
        final int pageSize;

        public InventoryTrigger(int size, Map<Integer, ItemStack> items, Map<Integer, String> scriptMap) throws InvalidSlotException{
            super(null);

            if(size < 9 || size % 9 != 0)
                throw new IllegalArgumentException("Inventory Trigger size should be multiple of 9!");

            this.items = new ItemStack[size];
            this.slots = new InventorySlot[size];

            for(Map.Entry<Integer, ItemStack> entry : items.entrySet()){
                this.items[entry.getKey()] = entry.getValue();
            }

            for(Map.Entry<Integer, String> entry : scriptMap.entrySet()){
                try {
                    this.slots[entry.getKey()] = new InventorySlot(entry.getValue());
                } catch (IOException | LexerException | ParserException e) {
                    throw new InvalidSlotException(entry.getKey(), e);
                }
            }

            this.pageSize = (size / SLOTSPERPAGE) + 1;
        }

        //we don't need interpreter for inventory trigger but its slots
        @Override
        protected Interpreter initInterpreter(Map<String, Object> scriptVars) {
            return null;
        }

        //intercept and pass interpretation to slots
        @Override
        protected void startInterpretation(Event e, Map<String, Object> scriptVars, Interpreter interpreter) {
            InventoryClickEvent ice = (InventoryClickEvent) e;

            int rawSlot = ice.getRawSlot();
            if(rawSlot >= 0 && rawSlot < SLOTSPERPAGE){
                Inventory inventory = ice.getInventory();
                int page = extractPageFromTitle(inventory.getTitle());

                InventoryTrigger.InventorySlot slot = slots[ice.getRawSlot()
                        + InventoryTrigger.SLOTSPERPAGE * (page - 1)];
                if (slot == null)
                    return;

                slot.activate(ice, scriptVars);
            }

        }

        @Override
        public Trigger clone() {
            // TODO Auto-generated method stub
            return null;
        }

        public InventorySlot[] getSlots() {
            return slots;
        }

        public ItemStack[] getItems() {
            return items;
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////

        public class InventorySlot extends Trigger{

            public InventorySlot(String script) throws IOException, LexerException, ParserException {
                super(script);

                init();
            }

            @Override
            public Trigger clone() {
                return null;
            }

        }
    }

    @SuppressWarnings("serial")
    static class InvalidSlotException extends Exception{
        public InvalidSlotException(int slot, Throwable cause) {
            super("Slot "+slot+" could not be initialized!", cause);
        }
    }

    private static final String SEPARATOR = ":";
    /**
     *
     * @param page starting from 1
     * @param title
     * @return combined title
     */
    public static String getTitleWithPage(int page, String title){
        return page+SEPARATOR+title;
    }
    /**
     *
     * @param title
     * @return starting from 1
     */
    public static int extractPageFromTitle(String title){
        return Integer.parseInt(title.split(SEPARATOR, 2)[0]);
    }
    public static String extractNameFromTitle(String title){
        return title.split(SEPARATOR, 2)[1];
    }
}
