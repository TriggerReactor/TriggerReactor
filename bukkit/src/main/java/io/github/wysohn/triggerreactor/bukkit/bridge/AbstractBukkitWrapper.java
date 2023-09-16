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

package io.github.wysohn.triggerreactor.bukkit.bridge;

import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitEntity;
import io.github.wysohn.triggerreactor.bukkit.bridge.event.BukkitPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.core.bridge.*;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IPlayerBlockLocationEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * For future reference: The instance can be found using {@link BukkitTriggerReactorCore#getWrapper()}
 */
public abstract class AbstractBukkitWrapper {
    public IEntity wrap(Entity entity) {
        return new BukkitEntity(entity);
    }

    public abstract IPlayer wrap(Player player);

    public IPlayerBlockLocationEvent wrap(PlayerBlockLocationEvent pble) {
        return new BukkitPlayerBlockLocationEvent(pble);
    }

    public ICommandSender wrap(CommandSender commandSender) {
        return new BukkitCommandSender(commandSender);
    }

    public IInventory wrap(Inventory inventory) {
        return new BukkitInventory(inventory);
    }

    public IItemStack wrap(ItemStack itemStack) {
        return new BukkitItemStack(itemStack);
    }

    public ILocation wrap(Location location) {
        return new BukkitLocation(location);
    }

    public IWorld wrap(World world) {
        return new BukkitWorld(world);
    }
}
