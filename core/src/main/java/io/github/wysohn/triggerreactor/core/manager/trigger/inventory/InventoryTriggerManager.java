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
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.IInventoryModifier;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.inject.Inject;
import java.io.File;
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
    IInventoryModifier inventoryModifier;
    @Inject
    IWrapper wrapper;

    @Inject
    InventoryTriggerManager() {
        super("InventoryTrigger");
    }

    @Override
    public InventoryTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            return factory.create(info, script);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
        InventoryTrigger trigger = factory.create(info, script);
        put(name, trigger);
        trigger.setItems(new IItemStack[size]);

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

        String title = trigger.getInventoryTitle();
        if (title == null)
            title = name;

        IInventory inventory = createInventory(trigger.size(), title);
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
        return wrapper.wrap(inventoryModifier.createInventory(size, name));
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
}