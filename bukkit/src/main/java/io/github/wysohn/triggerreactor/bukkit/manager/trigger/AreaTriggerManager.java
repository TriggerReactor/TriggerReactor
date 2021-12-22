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
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTrigger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Singleton
public class AreaTriggerManager extends AbstractAreaTriggerManager implements BukkitTriggerManager {
    @Inject
    @Named("PluginInstance")
    Object pluginInstance;

    @Inject
    public AreaTriggerManager() {
        super("AreaTrigger");
    }

    @Override
    public void onEnable() throws Exception {
        super.onEnable();

        Thread entityTrackingThread = new Thread(new Runnable() {

            private Collection<WeakReference<Entity>> getEntitiesSync(World w) {
                Collection<WeakReference<Entity>> entities = new LinkedList<>();
                try {
                    Bukkit.getScheduler().callSyncMethod((Plugin) pluginInstance, (Callable<Void>) () -> {
                        for (Entity e : w.getEntities())
                            entities.add(new WeakReference<>(e));
                        return null;
                    }).get();
                } catch (InterruptedException | CancellationException e1) {
                } catch (ExecutionException e1) {
                    e1.printStackTrace();
                }
                return entities;
            }

            @Override
            public void run() {
                while (main.isEnabled() && !Thread.interrupted()) {
                    try{
                        //track entity locations
                        for (World w : Bukkit.getWorlds()) {
                            Collection<WeakReference<Entity>> entityCollection = getEntitiesSync(w);
                            for (WeakReference<Entity> wr : entityCollection) {
                                Entity e = wr.get();

                                //reference disposed so ignore
                                if (e == null)
                                    continue;

                                UUID uuid = e.getUniqueId();

                                if (!main.isEnabled())
                                    break;

                                Future<Boolean> future = main.callSyncMethod(() -> !e.isDead() && e.isValid());

                                boolean valid = false;
                                try {
                                    if (future != null)
                                        valid = future.get();
                                } catch (InterruptedException | CancellationException e1) {
                                } catch (ExecutionException e1) {
                                    e1.printStackTrace();
                                }

                                if (!valid)
                                    continue;

                                if (!entityLocationMap.containsKey(uuid))
                                    continue;

                                SimpleLocation previous = entityLocationMap.get(uuid);
                                SimpleLocation current = LocationUtil.convertToSimpleLocation(e.getLocation());

                                //update location if equal
                                if (!previous.equals(current)) {
                                    entityLocationMap.put(uuid, current);
                                    onEntityBlockMoveAsync(e, previous, current);
                                }

                            }
                        }
                    } catch (IllegalPluginAccessException ex){
                        logger.info("Entity tracking has stopped. Plugin is disabling...");
                        return;
                    } catch (Exception ex){
                        ex.printStackTrace();
                        // some other unknown issues.
                        return;
                    }

                    try {
                        Thread.sleep(50L);//same as one tick
                    } catch (InterruptedException e) {
                    }
                }
            }

        });
        entityTrackingThread.setName("AreaTriggerManager -- EntityTrackingThread");
        entityTrackingThread.setDaemon(true);
        entityTrackingThread.start();
    }

    @Override
    public void onReload() {
        super.onReload();

        //re-register entities
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                UUID uuid = e.getUniqueId();

                if (e.isDead() || !e.isValid())
                    continue;

                SimpleLocation previous = null;
                SimpleLocation current = LocationUtil.convertToSimpleLocation(e.getLocation());

                entityLocationMap.put(uuid, current);
                entityTrackMap.put(uuid, new WeakReference<IEntity>(new BukkitEntity(e)));
                onEntityBlockMoveAsync(e, previous, current);
            }
        }
    }

    @Override
    public void onDisable() {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        SimpleLocation currentSloc = LocationUtil.convertToSimpleLocation(e.getPlayer().getLocation());
        getAreaForLocation(currentSloc).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.addEntity(new BukkitEntity(e.getPlayer())));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLocationChange(PlayerBlockLocationEvent e) {
        List<Map.Entry<Area, AreaTrigger>> from = getAreaForLocation(e.getFrom());
        List<Map.Entry<Area, AreaTrigger>> to = getAreaForLocation(e.getTo());
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", e.getPlayer());
        varMap.put("from", e.getFrom());
        varMap.put("to", e.getTo());

        from.stream()
                .filter((entry) -> !entry.getKey().isInThisArea(e.getTo()))//only for area leaving
                .map(Map.Entry::getValue)
                .forEach((trigger) -> {
                    trigger.removeEntity(e.getPlayer().getUniqueId());
                    trigger.activate(e, varMap, EventType.EXIT);
                });


        to.stream()
                .filter((entry) -> !entry.getKey().isInThisArea(e.getFrom()))//only for entering area
                .map(Map.Entry::getValue)
                .forEach((trigger) -> {
                    trigger.addEntity(new BukkitEntity(e.getPlayer()));
                    trigger.activate(e, varMap, EventType.ENTER);
                });
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getLocation());

        entityTrackMap.put(e.getEntity().getUniqueId(), new WeakReference<IEntity>(new BukkitEntity(e.getEntity())));
        entityLocationMap.put(e.getEntity().getUniqueId(), sloc);

        getAreaForLocation(sloc).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.addEntity(new BukkitEntity(e.getEntity())));
    }

    protected synchronized void onEntityBlockMoveAsync(Entity entity, SimpleLocation from, SimpleLocation current) {
        getAreaForLocation(from).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.removeEntity(entity.getUniqueId()));
        getAreaForLocation(current).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.addEntity(new BukkitEntity(entity)));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        onDeath((EntityDeathEvent) e);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getEntity().getLocation());

        entityTrackMap.remove(e.getEntity().getUniqueId());
        entityLocationMap.remove(e.getEntity().getUniqueId());

        getAreaForLocation(sloc).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.removeEntity(e.getEntity().getUniqueId()));
    }
}
