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

package io.github.wysohn.triggerreactor.bukkit.manager;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitInventory;
import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitPlayer;
import io.github.wysohn.triggerreactor.core.manager.InventoryEditManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryEditListener implements Listener {

    private final InventoryEditManager<ItemStack> manager;


    public InventoryEditListener(InventoryEditManager<ItemStack> manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player))
            throw new RuntimeException(e.getPlayer() + " is not a Player.");

        manager.onInventoryClose(new BukkitPlayer((Player) e.getPlayer()),
                                 new BukkitInventory(e.getInventory()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.stopEdit(new BukkitPlayer(event.getPlayer()));
    }
}
