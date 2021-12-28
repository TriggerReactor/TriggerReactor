package io.github.wysohn.triggerreactor.bukkit.listeners;

import io.github.wysohn.triggerreactor.bukkit.bridge.event.BukkitPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.PlayerLocationManager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import javax.inject.Inject;

public final class PlayerLocationListener extends AbstractBukkitListener {
    @Inject
    PlayerLocationManager playerLocationManager;
    @Inject
    IWrapper wrapper;

    @Inject
    PlayerLocationListener() {

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Location loc = player.getLocation();
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        playerLocationManager.setCurrentBlockLocation(player.getUniqueId(), sloc);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == e.getFrom())
            return;

        Player player = e.getPlayer();

        SimpleLocation from = playerLocationManager.getCurrentBlockLocation(player.getUniqueId());
        SimpleLocation to = LocationUtil.convertToSimpleLocation(e.getTo());

        PlayerBlockLocationEvent pble = new PlayerBlockLocationEvent(player, from, to);
        playerLocationManager.checkIfCancelled(new BukkitPlayerBlockLocationEvent(wrapper, pble));
        if (pble.isCancelled()) {
            Location loc = LocationUtil.convertToBukkitLocation(from);
            loc.setPitch(e.getPlayer().getLocation().getPitch());
            loc.setYaw(e.getPlayer().getLocation().getPitch());
            e.setFrom(loc);
            e.setTo(loc);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        playerLocationManager.removeCurrentBlockLocation(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRiding(VehicleMoveEvent e) {
        if (e.getFrom() == e.getTo())
            return;

        if (e.getVehicle().getPassengers().size() < 1)
            return;

        if (e.getVehicle().getPassengers().get(0).getType() != EntityType.PLAYER)
            return;

        Vehicle vehicle = e.getVehicle();
        Player player = (Player) vehicle.getPassengers().get(0);

        SimpleLocation from = playerLocationManager.getCurrentBlockLocation(player.getUniqueId());
        SimpleLocation to = LocationUtil.convertToSimpleLocation(e.getTo());

        PlayerBlockLocationEvent pble = new PlayerBlockLocationEvent(player, from, to);
        playerLocationManager.checkIfCancelled(new BukkitPlayerBlockLocationEvent(wrapper, pble));
        if (pble.isCancelled()) {
            Location loc = LocationUtil.convertToBukkitLocation(from);
            vehicle.setVelocity(new Vector());
            vehicle.teleport(loc);
        }
    }

    @EventHandler
    public void onSpawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        Location loc = player.getLocation();
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        playerLocationManager.setCurrentBlockLocation(player.getUniqueId(), sloc);
    }

    @EventHandler
    public void onTeleport(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        Location loc = player.getLocation();
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        playerLocationManager.setCurrentBlockLocation(player.getUniqueId(), sloc);
    }
}
