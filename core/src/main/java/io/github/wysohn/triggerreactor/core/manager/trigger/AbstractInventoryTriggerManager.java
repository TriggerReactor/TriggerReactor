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
package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractInventoryTriggerManager extends AbstractTriggerManager {
    protected static final String ITEMS = "Items";
    protected static final String SIZE = "Size";

    private final static Map<IInventory, InventoryTrigger> inventoryMap = new ConcurrentHashMap<>();

    protected final Map<String, InventoryTrigger> invenTriggers = new ConcurrentHashMap<>();
    private final Map<IInventory, Map<String, Object>> inventorySharedVars = new ConcurrentHashMap<>();

    @Override
    public void reload() {
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".yml");
            }
        };

        for (File ymlfile : folder.listFiles(filter)) {
            String triggerName = extractName(ymlfile);

            File triggerFile = getTriggerFile(folder, triggerName, false);

            if (!triggerFile.exists()) {
                plugin.getLogger().warning(triggerFile + " does not exists!");
                plugin.getLogger().warning(triggerFile + " is skipped.");
                continue;
            }

            if (triggerFile.isDirectory()) {
                plugin.getLogger().warning(triggerFile + " should be a file not a directory!");
                plugin.getLogger().warning(triggerFile + " is skipped.");
                continue;
            }

            int size = 0;
            Map<Integer, IItemStack> items = null;
            try {
                size = getData(ymlfile, SIZE, 0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            if (size == 0 || size % 9 != 0) {
                plugin.getLogger().warning("Could not load inventory trigger " + triggerName);
                plugin.getLogger().warning("Size: does not exists or not multiple of 9!");
                continue;
            }
            if (size > InventoryTrigger.MAXSIZE) {
                plugin.getLogger().warning("Could not load inventory trigger " + triggerName);
                plugin.getLogger().warning("Size: cannot be larger than " + InventoryTrigger.MAXSIZE);
                continue;
            }

            try {
                items = this.getData(ymlfile, ITEMS);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            if (items == null) {
                plugin.getLogger().warning("Could not find Items: for inventory trigger " + triggerName);
                continue;
            }

            String script = null;
            try {
                script = FileUtil.readFromFile(triggerFile);
            } catch (IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load inventory trigger " + triggerName);
                continue;
            }

            InventoryTrigger trigger = null;
            try {
                trigger = new InventoryTrigger(size, triggerName, items, triggerFile, script);
                //trigger.setSync(isSync);
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load inventory trigger " + triggerName);
                continue;
            }

            invenTriggers.put(triggerName, trigger);
        }
    }

    @Override
    public void saveAll() {
        for (Entry<String, InventoryTrigger> entry : invenTriggers.entrySet()) {
            String triggerName = entry.getKey();
            InventoryTrigger trigger = entry.getValue();

            File yamlFile = new File(folder, triggerName + ".yml");
            File triggerFile = getTriggerFile(folder, triggerName, true);

            if (!yamlFile.exists()) {
                try {
                    yamlFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                this.setData(yamlFile, SIZE, trigger.getItems().length);
                this.setData(yamlFile, ITEMS, trigger.getItems());
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                FileUtil.writeToFile(triggerFile, trigger.getScript());
            } catch (IOException e1) {
                e1.printStackTrace();
                plugin.getLogger().warning("Could not save " + triggerName);
            }
        }
    }

    /**
     * @param player
     * @param name
     * @return the opened Inventory's reference; null if no Inventory Trigger found
     */
    public IInventory openGUI(IPlayer player, String name) {
        InventoryTrigger trigger = invenTriggers.get(name);
        if (trigger == null)
            return null;

        IInventory inventory = createInventory(trigger.getItems().length, name);
        inventoryMap.put(inventory, trigger);

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("inventory", inventory.get());
        inventorySharedVars.put(inventory, varMap);

        fillInventory(trigger, trigger.getItems().length, inventory);

        player.openInventory(inventory);

        return inventory;
    }

    /**
     * Create actual inventory.
     *
     * @param size size of inventory. Must be multiple of 9.
     * @param name name of the inventory. This is the raw name, so the
     *             implemented method has to translate color code and and
     *             underscore appropriately.
     * @return the inventory
     */
    protected abstract IInventory createInventory(int size, String name);

    /**
     * @param name
     * @return null if not exists
     */
    public InventoryTrigger getTriggerForName(String name) {
        return invenTriggers.get(name);
    }

    /**
     * @param name this can contain color code &, but you should specify exact
     *             name for the title.
     * @return true on success; false if already exist
     * @throws ParserException See {@link Trigger#init()}
     * @throws LexerException  See {@link Trigger#init()}
     * @throws IOException     See {@link Trigger#init()}
     */
    public boolean createTrigger(int size, String name, String script)
            throws TriggerInitFailedException {
        if (invenTriggers.containsKey(name))
            return false;

        File triggerFile = getTriggerFile(folder, name, true);
        invenTriggers.put(name, new InventoryTrigger(size, name, new HashMap<>(), triggerFile, script));

        return true;
    }

    /**
     * @param name
     * @return true on success; false if not exists
     */
    public boolean deleteTrigger(String name) {
        if (!invenTriggers.containsKey(name))
            return false;

        deleteInfo(invenTriggers.remove(name));

        return true;
    }

    @Override
    protected void deleteInfo(Trigger trigger) {
        FileUtil.delete(new File(trigger.file.getParent(), trigger.getTriggerName() + ".yml"));
        super.deleteInfo(trigger);
    }

    @Override
    protected Collection<? extends Trigger> getAllTriggers() {
        return invenTriggers.values();
    }

    /**
     * @param trigger
     * @param size      mutiple of 9; must be less than or equalt to 54 (exclusive)
     * @param inventory
     */
    protected abstract void fillInventory(InventoryTrigger trigger, int size, IInventory inventory);
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void onInventoryClose(Object e, IPlayer player, IInventory inventory) {
        if (!inventoryMap.containsKey(inventory))
            return;
        InventoryTrigger trigger = inventoryMap.get(inventory);

        Map<String, Object> varMap = inventorySharedVars.get(inventory);
        varMap.put("player", player.get());
        varMap.put("trigger", "close");

        trigger.setSync(true);
        trigger.activate(e, varMap);
        trigger.setSync(false);

        inventoryMap.remove(inventory);
        inventorySharedVars.remove(inventory);
    }

    public boolean hasInventoryOpen(IInventory inventory) {
        return inventoryMap.containsKey(inventory);
    }

    public InventoryTrigger getTriggerForOpenInventory(IInventory inventory) {
        return inventoryMap.get(inventory);
    }

    public Map<String, Object> getSharedVarsForInventory(IInventory inventory) {
        return inventorySharedVars.get(inventory);
    }

    public AbstractInventoryTriggerManager(TriggerReactor plugin, SelfReference ref, File tirggerFolder) {
        super(plugin, ref, tirggerFolder);
    }

    public static class InventoryTrigger extends Trigger {
        public static final int MAXSIZE = 6 * 9;

        final IItemStack[] items;

        private InventoryTrigger(String name, String script, File file, IItemStack[] items) throws TriggerInitFailedException {
            super(name, file, script);
            this.items = items;

            init();
        }

        public InventoryTrigger(int size, String name, Map<Integer, IItemStack> items, File file, String script) throws TriggerInitFailedException {
            super(name, file, script);
            if (size < 9 || size % 9 != 0)
                throw new IllegalArgumentException("Inventory Trigger size should be multiple of 9!");

            if (size > MAXSIZE)
                throw new IllegalArgumentException("Inventory Size cannot be larger than " + MAXSIZE);

            this.items = new IItemStack[size];

            for (Map.Entry<Integer, IItemStack> entry : items.entrySet()) {
                this.items[entry.getKey()] = entry.getValue();
            }

            init();
        }

        @Override
        protected void start(Object e, Map<String, Object> scriptVars, Interpreter interpreter, boolean sync) {
            try {
                interpreter.startWithContextAndInterrupter(e,
                        TriggerReactor.getInstance().createInterrupterForInv(e, interpreter, cooldowns, inventoryMap));
            } catch (Exception ex) {
                TriggerReactor.getInstance().handleException(e,
                        new Exception("Error occurred while processing Trigger [" + getTriggerName() + "]!", ex));
            }
        }

        @Override
        public Trigger clone() {
            try {
                return new InventoryTrigger(triggerName, script, file, items);
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
        }

        public IItemStack[] getItems() {
            return items;
        }
    }

}