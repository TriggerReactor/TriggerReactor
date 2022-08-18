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
import io.github.wysohn.triggerreactor.bukkit.main.BukkitTriggerReactorCore;
import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AreaTriggerListener implements BukkitTriggerManager {
    private final AreaTriggerManager manager;

    public AreaTriggerListener(AreaTriggerManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        SimpleLocation currentSloc = LocationUtil.convertToSimpleLocation(e.getPlayer().getLocation());
        manager.getAreaForLocation(currentSloc).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.addEntity(new BukkitEntity(e.getPlayer())));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLocationChange(PlayerBlockLocationEvent e) {
        List<Map.Entry<Area, AreaTrigger>> from = manager.getAreaForLocation(e.getFrom());
        List<Map.Entry<Area, AreaTrigger>> to = manager.getAreaForLocation(e.getTo());
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", e.getPlayer());
        varMap.put("from", e.getFrom());
        varMap.put("to", e.getTo());

        from.stream()
                .filter((entry) -> !entry.getKey().isInThisArea(e.getTo()))//only for area leaving
                .map(Map.Entry::getValue)
                .forEach((trigger) -> {
                    trigger.removeEntity(e.getPlayer().getUniqueId());
                    trigger.activate(e, varMap, AreaTriggerManager.EventType.EXIT);
                });


        to.stream()
                .filter((entry) -> !entry.getKey().isInThisArea(e.getFrom()))//only for entering area
                .map(Map.Entry::getValue)
                .forEach((trigger) -> {
                    trigger.addEntity(new BukkitEntity(e.getPlayer()));
                    trigger.activate(e, varMap, AreaTriggerManager.EventType.ENTER);
                });
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getLocation());

        manager.startTrackingEntity(BukkitTriggerReactorCore.getWrapper().wrap(e.getEntity()), sloc);
        manager.getAreaForLocation(sloc).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.addEntity(new BukkitEntity(e.getEntity())));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        onDeath((EntityDeathEvent) e);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getEntity().getLocation());

        manager.stopTrackingEntity(e.getEntity().getUniqueId());
        manager.getAreaForLocation(sloc).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.removeEntity(e.getEntity().getUniqueId()));
    }
}
