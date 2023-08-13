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

package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;

import java.util.Map;
import java.util.UUID;

/**
 * This interface provides methods that can be used to control
 * any plugin specific behaviors. For example, spawning an entity
 * is <b>not</b> intended to be here since it is more of the 'game specific'
 * behavior.
 * <p>
 * However, it would make sense to have a method that can be used to
 * disable a plugin, executing a command, etc. is more of the 'plugin specific'
 * behavior, so those methods should be here.
 */
public interface IPluginManagement {
    /**
     * get Plugin's description.
     *
     * @return returns the full name of the plugin and its version.
     */
    String getPluginDescription();

    /**
     * get Plugin's version as String
     *
     * @return version of the plugin as String.
     */
    String getVersion();

    /**
     * get Author of plugin
     *
     * @return author name of the plugin as String.
     */
    String getAuthor();

    /**
     * get sender instance of the console
     *
     * @return
     */
    ICommandSender getConsoleSender();

    /**
     * Run a command as a console.
     *
     * @param command the command to be executed (without the slash)
     */
    void runCommandAsConsole(String command);

    boolean isEnabled();

    boolean isDebugging();

    void setDebugging(boolean bool);

    /**
     * Disable this plugin.
     */
    void disablePlugin();

    /**
     * Get the main class instance. JavaPlugin for Bukkit API for example.
     *
     * @return
     */
    <T> T getMain();

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

    /**
     * try to extract player from context 'e'.
     *
     * @param e Event for Bukkit API
     * @return
     */
    IPlayer extractPlayerFromContext(Object e);

    /**
     * extract useful custom variables manually from 'context'
     *
     * @param context
     * @return
     */
    Map<String, Object> getCustomVarsForTrigger(Object context);
}
