package io.github.wysohn.triggerreactor.bukkit.listeners.trigger;

import io.github.wysohn.triggerreactor.bukkit.listeners.AbstractBukkitListener;
import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.inject.Inject;

public final class AreaTriggerListener extends AbstractBukkitListener {
    @Inject
    AreaTriggerManager areaTriggerManager;
    @Inject
    IWrapper wrapper;

    @Inject
    AreaTriggerListener() {

    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        onDeath((EntityDeathEvent) e);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getEntity().getLocation());

        areaTriggerManager.onEntityDeath(sloc, e.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        SimpleLocation currentSloc = LocationUtil.convertToSimpleLocation(e.getPlayer().getLocation());
        IPlayer entity = wrapper.wrap(e.getPlayer());

        areaTriggerManager.onPlayerSpawn(currentSloc, entity);
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getLocation());

        areaTriggerManager.onEntitySpawn(sloc, e.getEntity().getUniqueId(), e.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLocationChange(PlayerBlockLocationEvent e) {
        areaTriggerManager.onLocationChange(wrapper.wrap(e.getPlayer()), e.getFrom(), e.getTo());
    }
}
