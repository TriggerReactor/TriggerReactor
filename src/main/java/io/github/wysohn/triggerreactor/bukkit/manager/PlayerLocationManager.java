/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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

import org.bukkit.Bukkit;
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

import io.github.wysohn.triggerreactor.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.bukkit.bridge.player.BukkitPlayer;
import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.bukkit.util.LocationUtil;
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

        SimpleLocation backTo = onMove(new BukkitPlayer(player), from, to);
        if(backTo != null){
            e.setFrom(LocationUtil.convertToBukkitLocation(backTo));
            e.setTo(LocationUtil.convertToBukkitLocation(backTo));
        }
    }

    @Override
    protected SimpleLocation onMove(IPlayer player, SimpleLocation from, SimpleLocation to) {
        if(from.equals(to))
            return null;

        PlayerBlockLocationEvent pble = new PlayerBlockLocationEvent(player.get(), from, to);
        Bukkit.getPluginManager().callEvent(pble);
        if(pble.isCancelled()){
            Location bukkitFrom = LocationUtil.convertToBukkitLocation(from);
            Location result = bukkitFrom.clone();

            Player p = player.get();
            Location loc = p.getLocation();

            result.setPitch(loc.getPitch());
            result.setYaw(loc.getYaw());
            return new SimpleLocation(result.getWorld().getName(),
                    result.getBlockX(),
                    result.getBlockY(),
                    result.getBlockZ(),
                    result.getPitch(),
                    result.getYaw());
        } else {
            setCurrentBlockLocation(player.getUniqueId(), to);
            return null;
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
