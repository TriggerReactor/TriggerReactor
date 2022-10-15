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

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitBlock;
import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitPlayer;
import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.LocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.WalkTrigger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class WalkTriggerListener
        extends LocationBasedTriggerListener<WalkTrigger,
        LocationBasedTriggerManager<WalkTrigger>> {


    public WalkTriggerListener(LocationBasedTriggerManager<WalkTrigger> manager) {
        super(manager);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerBlockLocationEvent e) {
        SimpleLocation bottomLoc = e.getTo().add(0, -1, 0);

        manager.handleWalk(e,
                           new BukkitPlayer(e.getPlayer()),
                           e.getFrom(),
                           e.getTo(),
                           new BukkitBlock(LocationUtil.convertToBukkitLocation(bottomLoc).getBlock()));
    }
}
