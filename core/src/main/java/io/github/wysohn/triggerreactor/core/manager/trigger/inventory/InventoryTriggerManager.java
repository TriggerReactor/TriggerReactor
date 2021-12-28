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
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactories;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryTriggerManager extends AbstractTriggerManager<InventoryTrigger> {
    final Map<IInventory, Map<String, Object>> inventorySharedVars = new ConcurrentHashMap<>();
    @Inject
    InventoryTriggerFactory factory;
    @Inject
    ConfigSourceFactories configSourceFactories;
    @Inject
    IWrapper wrapper;
    @Inject
    IGameController gameController;
    @Inject
    @Named("ItemStack")
    Class<?> itemClass;

    @Inject
    InventoryTriggerManager() {
        super("InventoryTrigger");
    }

    @Override
    public InventoryTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        int size = info.getConfig()
                .get(SIZE, Integer.class)
                .filter(s -> s != 0 && s % 9 == 0)
                .filter(s -> s <= InventoryTrigger.MAXSIZE)
                .orElseThrow(
                        () -> new InvalidTrgConfigurationException("Couldn't find or invalid Size", info.getConfig()));
        Map<Integer, IItemStack> items = new HashMap<>();

        if (info.getConfig().has(ITEMS)) {
            if (!info.getConfig().isSection(ITEMS)) {
                throw new InvalidTrgConfigurationException("Items should be an object", info.getConfig());
            }

            for (int i = 0; i < size; i++) {
                final int itemIndex = i;
                info.getConfig()
                        .get(ITEMS + "." + i, itemClass)
                        .ifPresent(item -> items.put(itemIndex, wrapper.wrap(item)));
            }
        }

        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            IItemStack[] itemArray = new IItemStack[size];
            for (int i = 0; i < size; i++)
                itemArray[i] = items.getOrDefault(i, null);
            return factory.create(info, script, itemArray);
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

    @Override
    public void onDisable() {

    }

    /**
     * @param name this can contain color code &, but you should specify exact
     *             name for the title.
     * @return true on success; false if already exist
     */
    public boolean createTrigger(int size, String name, String script) throws TriggerInitFailedException {
        if (has(name))
            return false;

        File file = getTriggerFile(folder, name, true);
        IConfigSource config = configSourceFactories.create(folder, name);
        TriggerInfo info = TriggerInfo.defaultInfo(file, config);
        put(name, factory.create(info, script, new IItemStack[size]));

        return true;
    }

    public boolean hasInventoryOpen(IInventory inventory) {
        return inventoryMap.containsKey(inventory);
    }

    public void onClose(Object event, IPlayer player, IInventory inventory) {
        InventoryTrigger trigger = Objects.requireNonNull(inventoryMap.get(inventory));

        Map<String, Object> varMap = inventorySharedVars.get(inventory);
        varMap.put("event", event);
        varMap.put("player", player.get());
        varMap.put("trigger", "close");

        trigger.activate(varMap, true);

        inventoryMap.remove(inventory);
        inventorySharedVars.remove(inventory);
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

        inventory.setContents(trigger.getItems(), true);
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
    protected IInventory createInventory(int size, String name) {
        name = name.replaceAll("_", " ");
        return wrapper.wrap(gameController.createInventory(size, name));
    }

    //helper method to replace all the items in an inventory trigger
    public void replaceItems(InventoryTrigger trigger, IItemStack[] items) {
        IItemStack[] triggerItems = trigger.getItems();
        for (int i = 0; i < triggerItems.length; i++) {
            triggerItems[i] = items[i];
        }

        updateItemConfig(trigger, items);
    }

    public void onClick(Object event,
                        IInventory inventory,
                        Object currentItem,
                        int slot,
                        String clickType,
                        int hotbar) {
        if (slot < 0)
            return;

        InventoryTrigger trigger = Objects.requireNonNull(getTriggerForOpenInventory(inventory));

        Map<String, Object> varMap = getSharedVarsForInventory(inventory);
        varMap.put(Trigger.VAR_NAME_EVENT, event);
        varMap.put("item", currentItem);
        varMap.put("slot", slot);
        varMap.put("click", clickType);
        varMap.put("hotbar", hotbar);
        varMap.put("trigger", "click");

        trigger.activate(varMap);
    }

    public InventoryTrigger getTriggerForOpenInventory(IInventory inventory) {
        return inventoryMap.get(inventory);
    }

    public Map<String, Object> getSharedVarsForInventory(IInventory inventory) {
        return inventorySharedVars.get(inventory);
    }

    public boolean isTrackedInventory(IInventory inventory) {
        return inventoryMap.containsKey(inventory);
    }

    public void onOpen(Object event, Object player, IInventory inventory) {
        InventoryTrigger trigger = Objects.requireNonNull(getTriggerForOpenInventory(inventory));

        Map<String, Object> varMap = getSharedVarsForInventory(inventory);
        varMap.put(Trigger.VAR_NAME_EVENT, event);
        varMap.put("player", player);
        varMap.put("trigger", "open");

        trigger.activate(varMap);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    final static Map<IInventory, InventoryTrigger> inventoryMap = new ConcurrentHashMap<>();
    public static final String ITEMS = "Items";
    public static final String SIZE = "Size";
    public static final String TITLE = "Title";

}