/*
 *     Copyright (C) 2021 wysohn and contributors
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * The controller responsible for any interaction with the game aspect.
 * Do not put plugin specific methods here. Put them in the TriggerReactorMain instead.
 */
public interface IGameController {
    void addItemLore(IItemStack iS, String lore);

    /**
     * Call event so that it can be heard by listeners
     *
     * @param event
     */
    void callEvent(IEvent event);

    /**
     * Run Callable on the server thread.
     *
     * @param call the callable
     * @return the future object.
     */
    <T> Future<T> callSyncMethod(Callable<T> call);

    Object createEmptyPlayerEvent(ICommandSender sender);

    /**
     * Create ProcessInterrupter that will be used for the most of the Triggers. It is responsible for this
     * interrupter to handle
     * cooldowns, CALL executor, etc, that has to be processed during the iterpretation.
     *
     * @param cooldowns list of current cooldowns.
     * @return the interrupter created.
     */
    ProcessInterrupter createInterrupter(Map<UUID, Long> cooldowns);

    /**
     * Create ProcessInterrupter that will be used for the most of the Triggers. It is responsible for this
     * interrupter to handle
     * cooldowns, CALL executor, etc, that has to be processed during the interpretation.
     * This method exists specifically for Inventory Trigger. As Inventory Trigger should stop at some point when
     * the Inventory was closed, it is the iterrupter's responsibility to do that.
     *
     * @param cooldowns    list of current cooldowns.
     * @param inventoryMap the inventory map that contains all the information about open inventories. As child class
     *                     that implements
     *                     IIventory should override hashCode() and equals() methods, you can assume that each
     *                     IInventory instance represents one trigger
     *                     that is running with the InventoryTrigger mapped. So it is ideal to get inventory object
     *                     from the 'e' context and see if the Inventory
     *                     object exists in the 'inventoryMap.' For the properly working InventoryTriggerManager,
     *                     closing the inventory should delete the IInventory
     *                     from the 'inventoryMap,' so you can safely assume that closed inventory will not exists in
     *                     the 'inventoryMap.'
     * @return
     */
    ProcessInterrupter createInterrupterForInv(Map<UUID, Long> cooldowns,
                                               Map<IInventory, InventoryTrigger> inventoryMap);

    Object createPlayerCommandEvent(ICommandSender sender, String label, String[] args);

    /**
     * try to extract player from context 'e'.
     *
     * @param e Event for Bukkit API
     * @return
     */
    IPlayer extractPlayerFromContext(Object e);

    /**
     * get sender instance of the console
     *
     * @return
     */
    ICommandSender getConsoleSender();

    /**
     * extract useful custom variables manually from 'event'
     *
     * @param event
     * @return
     */
    Map<String, Object> getCustomVarsForTrigger(Object event);

    IPlayer getPlayer(String string);

    boolean removeLore(IItemStack iS, int index);

    void setItemTitle(IItemStack iS, String title);

    boolean setLore(IItemStack iS, int index, String lore);

    /**
     * Show glowstones to indicate the walk/click triggers in the chunk. This should send block change packet
     * instead of changing the real block.
     *
     * @param sender sender to show the glow stones
     * @param set    the set contains location of block and its associated trigger.
     */
    void showGlowStones(ICommandSender sender, Set<Map.Entry<SimpleLocation, Trigger>> set);

    Iterable<? extends IPlayer> getOnlinePlayers();

    Iterable<? extends IWorld> getWorlds();

    IInventory createInventory(int size, String name);
}
