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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitInventory;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitItemStack;
import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitPlayer;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InventoryTriggerListener
        implements BukkitTriggerManager {

    private final InventoryTriggerManager<?> manager;

    @Inject
    public InventoryTriggerListener(InventoryTriggerManager<?> manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        Inventory inventory = e.getInventory();

        IInventory wrappedInventory = new BukkitInventory(inventory);
        IPlayer wrappedPlayer = new BukkitPlayer((Player) e.getPlayer());

        manager.onOpen(e, wrappedInventory, wrappedPlayer);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        Inventory inventory = e.getInventory();

        if (!manager.hasInventoryOpen(new BukkitInventory(inventory)))
            return;
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;

        if (e.getRawSlot() < 0)
            return;

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null)
            clickedItem = new ItemStack(Material.AIR);
        IItemStack wrappedItem = new BukkitItemStack(clickedItem);

        Inventory inventory = e.getInventory();
        IInventory wrappedInventory = new BukkitInventory(inventory);

        manager.onClick(e,
                wrappedInventory,
                wrappedItem,
                e.getRawSlot(),
                e.getClick().name(),
                e.getHotbarButton(),
                e::setCancelled);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        IPlayer bukkitPlayer = new BukkitPlayer((Player) e.getPlayer());
        manager.onInventoryClose(e, bukkitPlayer, new BukkitInventory(e.getInventory()));
    }


}
