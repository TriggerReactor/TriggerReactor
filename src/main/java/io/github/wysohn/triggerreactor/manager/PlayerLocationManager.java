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
package io.github.wysohn.triggerreactor.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.manager.location.SimpleLocation;

public class PlayerLocationManager extends Manager implements Listener{
    private transient Map<UUID, SimpleLocation> locations = new ConcurrentHashMap<>();

    public PlayerLocationManager(TriggerReactor plugin) {
        super(plugin);
        // TODO Auto-generated constructor stub
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        Location loc = player.getLocation();
        SimpleLocation sloc = new SimpleLocation(loc);
        locations.put(player.getUniqueId(), sloc);
    }

    @EventHandler
    public void onSpawn(PlayerRespawnEvent e){
        Player player = e.getPlayer();
        Location loc = player.getLocation();
        SimpleLocation sloc = new SimpleLocation(loc);
        locations.put(player.getUniqueId(), sloc);
    }

    @EventHandler
    public void onTeleport(PlayerChangedWorldEvent e){
        Player player = e.getPlayer();
        Location loc = player.getLocation();
        SimpleLocation sloc = new SimpleLocation(loc);
        locations.put(player.getUniqueId(), sloc);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        locations.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent e){
        if(e.getTo() == e.getFrom())
            return;

        Player player = e.getPlayer();

        SimpleLocation from = locations.get(player.getUniqueId());
        SimpleLocation to = new SimpleLocation(e.getTo());

        if(from.equals(to))
            return;

        PlayerBlockLocationEvent pble = new PlayerBlockLocationEvent(player, from, to);
        Bukkit.getPluginManager().callEvent(pble);
        if(pble.isCancelled()){
            Location bukkitFrom = convertToBukkitLocation(from);
            Location result = bukkitFrom.clone();
            result.setPitch(player.getLocation().getPitch());
            result.setYaw(player.getLocation().getYaw());
            e.setFrom(result);
            e.setTo(result);
        } else {
            locations.put(player.getUniqueId(), to);
        }
    }

    private Location convertToBukkitLocation(SimpleLocation from) {
        World world = Bukkit.getWorld(from.getWorld());
        int x = from.getX();
        int y = from.getY();
        int z= from.getZ();
        return new Location(world, x + 0.5, y, z + 0.5);
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
