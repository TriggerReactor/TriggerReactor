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
package io.github.wysohn.triggerreactor.core.manager.trigger.inventory;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.ITriggerReactorAPI;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractInventoryTriggerManager<ItemStack> extends AbstractTriggerManager<InventoryTrigger> {
    @Inject
    ITriggerReactorAPI api;

    public static final String ITEMS = "Items";
    public static final String SIZE = "Size";
    public static final String TITLE = "Title";

    private final Class<ItemStack> itemClass;

    final static Map<IInventory, InventoryTrigger> inventoryMap = new ConcurrentHashMap<>();
    final Map<IInventory, Map<String, Object>> inventorySharedVars = new ConcurrentHashMap<>();

    public AbstractInventoryTriggerManager(String folderName,
                                           Class<ItemStack> itemClass) {
        super(folderName);
        this.itemClass = itemClass;
    }

    @Override
    public InventoryTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        int size = info.getConfig().get(SIZE, Integer.class)
                .filter(s -> s != 0 && s % 9 == 0)
                .filter(s -> s <= InventoryTrigger.MAXSIZE)
                .orElseThrow(() -> new InvalidTrgConfigurationException("Couldn't find or invalid Size", info.getConfig()));
        Map<Integer, IItemStack> items = new HashMap<>();

        if (info.getConfig().has(ITEMS)) {
            if (!info.getConfig().isSection(ITEMS)) {
                throw new InvalidTrgConfigurationException("Items should be an object", info.getConfig());
            }

            for (int i = 0; i < size; i++) {
                final int itemIndex = i;
                info.getConfig().get(ITEMS + "." + i, itemClass).ifPresent(item ->
                        items.put(itemIndex, main.getWrapper().wrap(item)));
            }
        }

        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            IItemStack[] itemArray = new IItemStack[size];
            for (int i = 0; i < size; i++)
                itemArray[i] = items.getOrDefault(i, null);
            return new InventoryTrigger(api, info, script, itemArray);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(InventoryTrigger trigger) {
        try {
            FileUtil.writeToFile(trigger.getInfo().getSourceCodeFile(), trigger.getScript());

            IItemStack[] items = trigger.items;
            int size = trigger.items.length;

            trigger.getInfo().getConfig().put(SIZE, size);
            trigger.getInfo().getConfig().put(TITLE, trigger.getInfo().getTriggerName());
            updateItemConfig(trigger, items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateItemConfig(InventoryTrigger trigger, IItemStack[] items) {
        for (int i = 0; i < items.length; i++) {
            IItemStack item = items[i];
            if (item == null)
                continue;

            trigger.getInfo().getConfig().put(ITEMS + "." + i, item.get());
        }
    }

    /**
     * @param player
     * @param name
     * @return the opened Inventory's reference; null if no Inventory Trigger found
     */
    public IInventory openGUI(IPlayer player, String name) {
        InventoryTrigger trigger = get(name);
        if (trigger == null)
            return null;

        String title = trigger.getInfo().getConfig().get(TITLE, String.class).orElse(name);

        IInventory inventory = createInventory(trigger.getItems().length, title);
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
     * @param name this can contain color code &, but you should specify exact
     *             name for the title.
     * @return true on success; false if already exist
     * @throws ParserException See {@link Trigger#init()}
     * @throws LexerException  See {@link Trigger#init()}
     * @throws IOException     See {@link Trigger#init()}
     */
    public boolean createTrigger(int size, String name, String script)
            throws TriggerInitFailedException {
        if (has(name))
            return false;

        File file = getTriggerFile(folder, name, true);
        IConfigSource config = configSourceFactories.create(folder, name);
        TriggerInfo info = TriggerInfo.defaultInfo(file, config);
        put(name, new InventoryTrigger(api, info, script, size, new HashMap<>()));

        return true;
    }

    /**
     * @param trigger
     * @param size      mutiple of 9; must be less than or equalt to 54 (exclusive)
     * @param inventory
     */
    protected abstract void fillInventory(InventoryTrigger trigger, int size, IInventory inventory);

    //helper method to replace all the items in an inventory trigger
    public void replaceItems(InventoryTrigger trigger, IItemStack[] items) {
        IItemStack[] triggerItems = trigger.getItems();
        for (int i = 0; i < triggerItems.length; i++) {
            triggerItems[i] = items[i];
        }

        updateItemConfig(trigger, items);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void onInventoryClose(Object e, IPlayer player, IInventory inventory) {
        if (!inventoryMap.containsKey(inventory))
            return;
        InventoryTrigger trigger = inventoryMap.get(inventory);

        Map<String, Object> varMap = inventorySharedVars.get(inventory);
        varMap.put("player", player.get());
        varMap.put("trigger", "close");

        trigger.activate(e, varMap, true);

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

}