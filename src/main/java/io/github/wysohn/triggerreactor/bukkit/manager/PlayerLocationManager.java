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
package io.github.wysohn.triggerreactor.bukkit.manager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import io.github.wysohn.triggerreactor.bukkit.bridge.event.BukkitPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractPlayerLocationManager;

public class PlayerLocationManager extends AbstractPlayerLocationManager implements Listener{


    public PlayerLocationManager(TriggerReactor plugin) {
        super(plugin);
        // TODO Auto-generated constructor stub
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        Location loc = player.getLocation();
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        setCurrentBlockLocation(player.getUniqueId(), sloc);
    }

    @EventHandler
    public void onSpawn(PlayerRespawnEvent e){
        Player player = e.getPlayer();
        Location loc = player.getLocation();
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        setCurrentBlockLocation(player.getUniqueId(), sloc);
    }

    @EventHandler
    public void onTeleport(PlayerChangedWorldEvent e){
        Player player = e.getPlayer();
        Location loc = player.getLocation();
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        setCurrentBlockLocation(player.getUniqueId(), sloc);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        removeCurrentBlockLocation(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent e){
        if(e.getTo() == e.getFrom())
            return;

        Player player = e.getPlayer();

        SimpleLocation from = getCurrentBlockLocation(player.getUniqueId());
        SimpleLocation to = LocationUtil.convertToSimpleLocation(e.getTo());

        PlayerBlockLocationEvent pble = new PlayerBlockLocationEvent(player, from, to);
        onMove(new BukkitPlayerBlockLocationEvent(pble));
        if(pble.isCancelled()){
            Location loc = LocationUtil.convertToBukkitLocation(from);
            loc.setPitch(e.getPlayer().getLocation().getPitch());
            loc.setYaw(e.getPlayer().getLocation().getPitch());
            e.setFrom(loc);
            e.setTo(loc);
        }
    }

    @Override
    public void reload() {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }

}
