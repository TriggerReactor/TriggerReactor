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
package io.github.wysohn.triggerreactor.core.bridge;

/**
 * For the sake of InventoryTrigger support, all child classes must override hashCode() and equals()
 * method out of the actual Inventory class.
 *
 * @author wysohn
 */
public interface IInventory extends IMinecraftObject {

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);

    IItemStack[] getStorageContents();

    default void setContents(IItemStack[] items) {
        setContents(items, false);
    }

    /**
     * Set contents of inventory and also replace the color codes
     * of all items.
     *
     * @param items
     * @param colorItems set contents and also replace the color code of items
     */
    void setContents(IItemStack[] items, boolean colorItems);
}
