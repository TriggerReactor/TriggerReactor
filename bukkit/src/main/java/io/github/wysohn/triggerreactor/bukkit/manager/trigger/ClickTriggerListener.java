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

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitLocation;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.LocationBasedTriggerManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class ClickTriggerListener
        extends LocationBasedTriggerListener<LocationBasedTriggerManager.ClickTrigger, LocationBasedTriggerManager<LocationBasedTriggerManager.ClickTrigger>> {


    public ClickTriggerListener(LocationBasedTriggerManager<LocationBasedTriggerManager.ClickTrigger> manager) {
        super(manager);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClickTrigger(PlayerInteractEvent e) {
        if (!BukkitUtil.isLeftHandClick(e))
            return;

        handleClick(e);
    }

    private void handleClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block clicked = e.getClickedBlock();

        if (clicked == null)
            return;

        Location loc = clicked.getLocation();
        BukkitLocation locationWrapped = new BukkitLocation(loc);
        LocationBasedTriggerManager.ClickTrigger trigger = manager.getTriggerForLocation(locationWrapped);
        if (trigger == null)
            return;

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", e.getPlayer());
        varMap.put("block", clicked);
        varMap.put("item", e.getItem());
        switch (e.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                varMap.put("click", "left");
                break;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                varMap.put("click", "right");
                break;
            default:
                varMap.put("click", "unknown");
        }


        trigger.activate(e, varMap);
        return;
    }
}
