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
import io.github.wysohn.triggerreactor.core.main.IInventoryHandle;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.*;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryTriggerManager<ItemStack> extends AbstractTriggerManager<InventoryTrigger> {
    public static final String ITEMS = "Items";
    public static final String SIZE = "Size";
    public static final String TITLE = "Title";

    final static Map<IInventory, InventoryTrigger> inventoryMap = new ConcurrentHashMap<>();
    final Map<IInventory, Map<String, Object>> inventorySharedVars = new ConcurrentHashMap<>();

    IInventoryHandle<ItemStack> inventoryHandle;

    public InventoryTriggerManager(TriggerReactorCore plugin, IInventoryHandle<ItemStack> inventoryHandle) {
        super(plugin, new File(plugin.getDataFolder(), "InventoryTrigger"), new ITriggerLoader<InventoryTrigger>() {
            @Override
            public InventoryTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
                int size = info.get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_SIZE, Integer.class)
                        .filter(s -> s != 0 && s % 9 == 0)
                        .filter(s -> s <= InventoryTrigger.MAXSIZE)
                        .orElseThrow(() -> new InvalidTrgConfigurationException("Couldn't find or invalid Size", info));
                Map<Integer, IItemStack> items = new HashMap<>();

                if (info.has(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS)) {
                    if (!info.isSection(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS)) {
                        throw new InvalidTrgConfigurationException("Items should be an object", info);
                    }

                    for (int i = 0; i < size; i++) {
                        final int itemIndex = i;
                        info.get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS, i, inventoryHandle.getItemClass())
                                .ifPresent(item -> items.put(itemIndex, inventoryHandle.wrapItemStack(item)));
                    }
                }

                try {
                    String script = FileUtil.readFromFile(info.getSourceCodeFile());
                    IItemStack[] itemArray = new IItemStack[size];
                    for (int i = 0; i < size; i++)
                        itemArray[i] = items.getOrDefault(i, null);
                    return new InventoryTrigger(info, script, itemArray);
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

                    trigger.getInfo().put(TriggerConfigKey.KEY_TRIGGER_INVENTORY_SIZE, size);
                    trigger.getInfo()
                            .put(TriggerConfigKey.KEY_TRIGGER_INVENTORY_TITLE, trigger.getInfo().getTriggerName());
                    for (int i = 0; i < items.length; i++) {
                        IItemStack item = items[i];
                        if (item == null)
                            continue;

                        trigger.getInfo().put(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS, i, item.get());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        this.inventoryHandle = inventoryHandle;
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

        String title = trigger.getInfo().get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_TITLE, String.class).orElse(name);

        IInventory inventory = inventoryHandle.createInventory(trigger.getItems().length, title);
        inventoryMap.put(inventory, trigger);

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("inventory", inventory.get());
        inventorySharedVars.put(inventory, varMap);

        inventoryHandle.fillInventory(trigger, trigger.getItems().length, inventory);

        player.openInventory(inventory);

        return inventory;
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
        if (has(name))
            return false;

        File file = getTriggerFile(folder, name, true);
        IConfigSource config = configSourceFactory.create(folder, name);
        TriggerInfo info = TriggerInfo.defaultInfo(file, config);
        put(name, new InventoryTrigger(info, script, size, new HashMap<>()));

        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @param e
     * @param player
     * @param inventory
     * @deprecated Event handler. Do not call this method except from listener or tests.
     */
    public void onInventoryClose(Object e, IPlayer player, IInventory inventory) {
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