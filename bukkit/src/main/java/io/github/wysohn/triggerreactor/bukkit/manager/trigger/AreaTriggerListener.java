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

import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitEntity;
import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class AreaTriggerListener implements BukkitTriggerManager {
    private final AreaTriggerManager manager;

    public AreaTriggerListener(AreaTriggerManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        manager.onJoin(LocationUtil.convertToSimpleLocation(e.getPlayer().getLocation()),
                       new BukkitEntity(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLocationChange(PlayerBlockLocationEvent e) {
        manager.onLocationChange(e,
                                 e.getFrom(),
                                 e.getTo(),
                                 e.getPlayer(),
                                 new BukkitEntity(e.getPlayer()),
                                 e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getLocation());

        manager.onSpawn(new BukkitEntity(e.getEntity()), sloc);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        onDeath((EntityDeathEvent) e);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getEntity().getLocation());

        manager.onDeath(e.getEntity().getUniqueId(), sloc);
    }
}
