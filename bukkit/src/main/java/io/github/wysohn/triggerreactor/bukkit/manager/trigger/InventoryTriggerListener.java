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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import io.github.wysohn.triggerreactor.bukkit.main.BukkitTriggerReactorCore;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
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

import java.util.Map;

public class InventoryTriggerListener
        implements BukkitTriggerManager {

    private final InventoryTriggerManager<?> manager;

    public InventoryTriggerListener(InventoryTriggerManager<?> manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        Inventory inventory = e.getInventory();

        if (!manager.hasInventoryOpen(BukkitTriggerReactorCore.getWrapper().wrap(inventory)))
            return;
        InventoryTrigger trigger = manager.getTriggerForOpenInventory(BukkitTriggerReactorCore.getWrapper()
                                                                              .wrap(inventory));

        Map<String, Object> varMap = manager.getSharedVarsForInventory(BukkitTriggerReactorCore.getWrapper()
                                                                               .wrap(inventory));
        varMap.put("player", e.getPlayer());
        varMap.put("trigger", "open");

        trigger.activate(e, varMap);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        Inventory inventory = e.getInventory();

        if (!manager.hasInventoryOpen(BukkitTriggerReactorCore.getWrapper().wrap(inventory)))
            return;
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        Inventory inventory = e.getInventory();

        if (!manager.hasInventoryOpen(BukkitTriggerReactorCore.getWrapper().wrap(inventory)))
            return;
        InventoryTrigger trigger = manager.getTriggerForOpenInventory(BukkitTriggerReactorCore.getWrapper()
                                                                              .wrap(inventory));

        // just always cancel if it's GUI
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player))
            return;

        if (e.getRawSlot() < 0)
            return;

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null)
            clickedItem = new ItemStack(Material.AIR);

        Map<String, Object> varMap = manager.getSharedVarsForInventory(BukkitTriggerReactorCore.getWrapper()
                                                                               .wrap(inventory));
        varMap.put("item", clickedItem.clone());
        varMap.put("slot", e.getRawSlot());
        varMap.put("click", e.getClick().name());
        varMap.put("hotbar", e.getHotbarButton());
        varMap.put("trigger", "click");

        trigger.activate(e, varMap);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        IPlayer bukkitPlayer = BukkitTriggerReactorCore.getWrapper().wrap((Player) e.getPlayer());
        manager.onInventoryClose(e, bukkitPlayer, BukkitTriggerReactorCore.getWrapper().wrap(e.getInventory()));
    }


}
