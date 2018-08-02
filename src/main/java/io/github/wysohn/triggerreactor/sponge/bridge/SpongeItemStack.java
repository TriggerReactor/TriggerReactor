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
package io.github.wysohn.triggerreactor.sponge.bridge;

import org.spongepowered.api.item.inventory.ItemStack;

import io.github.wysohn.triggerreactor.core.bridge.IItemStack;

public class SpongeItemStack implements IItemStack {
    private final ItemStack itemStack;

    public SpongeItemStack(ItemStack itemStack) {
        super();
        this.itemStack = itemStack;
    }

    @Override
    public <T> T get() {
        return (T) itemStack;
    }

    /**
     * Returns the full name of item as String.
     */
    @Override
    public <T> T getType() {
        return (T) itemStack.getItem().getType().getId();
    }

    @Override
    public IItemStack clone() {
        return new SpongeItemStack(itemStack.copy());
    }

}
