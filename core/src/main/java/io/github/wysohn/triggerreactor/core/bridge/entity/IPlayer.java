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
package io.github.wysohn.triggerreactor.core.bridge.entity;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;

import java.util.UUID;

public interface IPlayer extends IEntity, ICommandSender {

    UUID getUniqueId();

    SimpleChunkLocation getChunk();

    IInventory getInventory();

    /**
     * Get item in this player's main hand.
     *
     * @return IItemStack in the main hand; null if holding anything.
     */
    IItemStack getItemInMainHand();

    void setItemInMainHand(IItemStack iS);

    ILocation getLocation();

    void openInventory(IInventory inventory);
}
