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

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;

public class SpongeInventory implements IInventory {
    private final Inventory inventory;
    private final Carrier carrier;

    public SpongeInventory(Inventory inventory, Carrier carrier) {
        super();
        this.inventory = inventory;
        this.carrier = carrier;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SpongeInventory)) return false;

        return carrier.equals(((SpongeInventory) obj).carrier);
    }

    @Override
    public <T> T get() {
        return (T) inventory;
    }

    @Override
    public int hashCode() {
        return carrier.hashCode();
    }

}
