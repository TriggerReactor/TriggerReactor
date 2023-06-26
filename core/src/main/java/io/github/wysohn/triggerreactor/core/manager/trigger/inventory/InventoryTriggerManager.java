/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.wysohn.triggerreactor.core.manager.trigger.inventory;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.IGameManagement;
import io.github.wysohn.triggerreactor.core.main.IInventoryHandle;
import io.github.wysohn.triggerreactor.core.manager.trigger.*;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Singleton
public class InventoryTriggerManager<ItemStack> extends AbstractTriggerManager<InventoryTrigger> {
    public static final String ITEMS = "Items";
    public static final String SIZE = "Size";
    public static final String TITLE = "Title";

    final static Map<IInventory, InventoryTrigger> inventoryMap = new ConcurrentHashMap<>();
    final Map<IInventory, Map<String, Object>> inventorySharedVars = new ConcurrentHashMap<>();

    @Inject
    private IGameManagement gameManagement;
    @Inject
    private IInventoryHandle inventoryHandle;
    @Inject
    private ITriggerLoader<InventoryTrigger> loader;

    @Inject
    private InventoryTriggerManager(@Named("DataFolder") File folder,
                                    @Named("InventoryTriggerManagerFolder") String folderName) {
        super(new File(folder, folderName));
    }

    @Override
    public void initialize() {

    }

    /**
     * Open the GUI (of the existing InventoryTrigger) for the target player
     *
     * @param playerName    name of the player
     * @param inventoryName name of the InventoryTrigger
     * @return the opened Inventory's reference; null if no Inventory Trigger found
     * @throws IllegalArgumentException if the player is not online or not found
     */
    public IInventory openGUI(String playerName, String inventoryName) {
        IPlayer player = gameManagement.getPlayer(playerName);
        if (player == null)
            throw new IllegalArgumentException("Player " + playerName + " not found!");

        return openGUI(player, inventoryName);
    }

    /**
     * Open the GUI (of the existing InventoryTrigger) for the target player
     *
     * @param player        target player
     * @param inventoryName name of the InventoryTrigger
     * @return the opened Inventory's reference; null if no Inventory Trigger found
     */
    public IInventory openGUI(IPlayer player, String inventoryName) {
        InventoryTrigger trigger = get(inventoryName);
        if (trigger == null)
            return null;

        String title = trigger.getInfo()
                .get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_TITLE, String.class)
                .orElse(inventoryName);

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
        InventoryTrigger trigger = put(name, new InventoryTrigger(info, script, size, new HashMap<>()));

        loader.save(trigger);

        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @param eventInstance
     * @param inventory
     * @param player
     * @deprecated Event handler. Do not call this method except from listener or tests.
     */
    public void onOpen(Object eventInstance, IInventory inventory, IPlayer player) {
        if (!hasInventoryOpen(inventory))
            return;
        InventoryTrigger trigger = getTriggerForOpenInventory(inventory);

        Map<String, Object> varMap = getSharedVarsForInventory(inventory);
        varMap.put("player", player.get());
        varMap.put("trigger", "open");

        trigger.activate(eventInstance, varMap);
    }

    /**
     * @param inventory
     * @deprecated Event handler. Do not call this method except from listener or tests.
     */
    public void onClick(Object eventInstance,
                        IInventory inventory,
                        IItemStack clickedItem,
                        int rawSlot,
                        String clickName,
                        int hotbar,
                        Consumer<Boolean> eventCancelled) {
        if (!hasInventoryOpen(inventory))
            return;
        InventoryTrigger trigger = getTriggerForOpenInventory(inventory);

        // just always cancel if it's GUI
        eventCancelled.accept(true);

        Map<String, Object> varMap = getSharedVarsForInventory(inventory);
        varMap.put("item", clickedItem.clone().get());
        varMap.put("slot", rawSlot);
        varMap.put("click", clickName);
        varMap.put("hotbar", hotbar);
        varMap.put("trigger", "click");

        trigger.activate(eventInstance, varMap);
    }

    /**
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

    private InventoryTrigger getTriggerForOpenInventory(IInventory inventory) {
        return inventoryMap.get(inventory);
    }

    private Map<String, Object> getSharedVarsForInventory(IInventory inventory) {
        return inventorySharedVars.get(inventory);
    }

}