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

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;

public interface IInventoryHandle<ItemStack> {
    /**
     * Create actual inventory.
     *
     * @param size size of inventory. Must be multiple of 9.
     * @param name name of the inventory. This is the raw name, so the
     *             implemented method has to translate color code and and
     *             underscore appropriately.
     * @return the inventory
     */
    IInventory createInventory(int size, String name);

    /**
     * @param trigger
     * @param size      mutiple of 9; must be less than or equalt to 54 (exclusive)
     * @param inventory
     */
    void fillInventory(InventoryTrigger trigger, int size, IInventory inventory);

    Class<ItemStack> getItemClass();

    IItemStack wrapItemStack(ItemStack item);

    /**
     * Set the contents of the inventory.
     *
     * @param inv   the inventory
     * @param items the items to be set
     */
    void setContents(IInventory inv, IItemStack[] items);

    /**
     * Get the contents of the inventory.
     *
     * @param inv the inventory
     * @return the contents of the inventory
     */
    IItemStack[] getContents(IInventory inv);
}
