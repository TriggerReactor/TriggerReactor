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

package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IInventoryHandle;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public final class InventoryEditManager<ItemStack> extends Manager {
    @Inject
    private InventoryTriggerManager<ItemStack> invManager;
    @Inject
    private IInventoryHandle handle;
    @Inject
    private IPluginManagement pluginManagement;
    @Inject
    private ITriggerLoader<InventoryTrigger> inventoryTriggerLoader;

    //map of player uuids to inventories representing the inventories currently being edited
    private final Map<UUID, InventoryTrigger> sessions = new HashMap<>();
    //inventories currently awaiting a save/discard/continue command
    private final Map<UUID, IInventory> suspended = new HashMap<>();

    @Inject
    private InventoryEditManager() {
        super();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void reload() {
    }

    @Override
    public void shutdown() {

    }

    public void startEdit(IPlayer player, String invTriggerName) {
        UUID u = player.getUniqueId();
        InventoryTrigger trigger = invManager.get(invTriggerName);

        if (trigger == null) {
            player.sendMessage("InventoryTrigger " + invTriggerName + " does not exist.");
            return;
        }

        if (isEditing(player)) {
            player.sendMessage("You are already editing an inventory.");
            return;
        }

        sessions.put(u, trigger);

        IItemStack[] items = trigger.getItems();
        IInventory inv = handle.createInventory(items.length, trigger.getInfo().getTriggerName());
        handle.setContents(inv, items);
        inv.open(player);
//        ItemStack[] items = new ItemStack[iitems.length];
//        for (int i = 0; i < items.length; i++) {
//            items[i] = Optional.ofNullable(iitems[i])
//                    .map(IScriptObject::get)
//                    .filter(ItemStack.class::isInstance)
//                    .map(ItemStack.class::cast)
//                    .orElse(null);
//        }
//
//        Inventory inv = Bukkit.createInventory(null, items.length, trigger.getInfo().getTriggerName());
//        inv.setContents(items);
//        player.openInventory(BukkitTriggerReactorCore.getWrapper().wrap(inv));
    }

    public boolean isEditing(IPlayer player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public void continueEdit(IPlayer player) {
        UUID u = player.getUniqueId();
        if (!suspended.containsKey(u)) {
            return;
        }
        IInventory inv = suspended.remove(u);
        player.openInventory(inv);
    }

    public void discardEdit(IPlayer player) {
        if (!sessions.containsKey(player.getUniqueId())) {
            return;
        }
        stopEdit(player);
        player.sendMessage("Discarded edits");
    }

    //helper method to remove the player from both maps
    public void stopEdit(IPlayer player) {
        UUID u = player.getUniqueId();
        sessions.remove(u);
        suspended.remove(u);
    }

    public void saveEdit(IPlayer player) {
        UUID u = player.getUniqueId();
        if (!sessions.containsKey(u)) {
            return;
        }
        IInventory inv = suspended.get(u);
        InventoryTrigger trigger = sessions.get(u);
        IItemStack[] items = handle.getContents(inv);

        replaceItems(trigger, items);
        stopEdit(player);
        player.sendMessage("Saved edits");
    }

    //helper method to replace all the items in an inventory trigger
    private void replaceItems(InventoryTrigger trigger, IItemStack[] items) {
        IItemStack[] triggerItems = trigger.getItems();
        for (int i = 0; i < triggerItems.length; i++) {
            triggerItems[i] = items[i];
        }

        inventoryTriggerLoader.save(trigger);
    }

    /**
     * @param player
     * @param inventory
     * @deprecated event handler. Must be called by either listener or tests.
     */
    @Deprecated
    public void onInventoryClose(IPlayer player, IInventory inventory) {
        UUID u = player.getUniqueId();
        if (!sessions.containsKey(u)) {
            return;
        }
        //filter out already suspended
        if (suspended.containsKey(u)) {
            return;
        }

        suspended.put(u, inventory);
        sendMessage(player);
    }

    private void sendMessage(IPlayer player) {
        pluginManagement.runCommandAsConsole(MESSAGE.replace("@p", player.getName()));
    }

    private static final char X = '\u2718';
    private static final char CHECK = '\u2713';
    private static final char PENCIL = '\u270E';
    private static final String MESSAGE = "tellraw @p [\"\",{\"text\":\"" + CHECK
            + " Save\",\"bold\":true,\"underlined\":false,\"color\":\"green\","
            + "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trg links inveditsave\"}},{\"text\":\"\\n\"},"
            + "{\"text\":\""
            + PENCIL
            + " Continue Editing\",\"bold\":true,\"underlined\":false,\"color\":\"yellow\","
            + "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trg links inveditcontinue\"}},"
            + "{\"text\":\"\\n\"},{\"text\":\""
            + X
            + " Cancel\",\"bold\":true,\"underlined\":false,\"color\":\"red\","
            + "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trg links inveditdiscard\"}}]";
}
