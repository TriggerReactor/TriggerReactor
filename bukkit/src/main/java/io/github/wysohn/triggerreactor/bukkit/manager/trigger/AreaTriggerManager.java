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
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTrigger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.awt.image.renderable.ContextualRenderedImageFactory;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AreaTriggerManager extends AbstractAreaTriggerManager implements BukkitTriggerManager {

    public AreaTriggerManager(TriggerReactorCore plugin) {
        super(plugin, new File(plugin.getDataFolder(), "AreaTrigger"));

        Thread entityTrackingThread = new Thread(new Runnable() {

            private Collection<WeakReference<Entity>> getEntitiesSync(World w) {
                Collection<WeakReference<Entity>> entities = new LinkedList<>();
                try {
                    Bukkit.getScheduler().callSyncMethod(plugin.getMain(), new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            for (Entity e : w.getEntities())
                                entities.add(new WeakReference<>(e));
                            return null;
                        }
                    }).get();
                } catch (InterruptedException | CancellationException e1) {
                } catch (ExecutionException e1) {
                    e1.printStackTrace();
                }
                return entities;
            }

            @Override
            public void run() {
                while (plugin.isEnabled() && !Thread.interrupted()) {
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

                                if (!plugin.isEnabled())
                                    break;

                                Future<Boolean> future = plugin.callSyncMethod(new Callable<Boolean>() {

                                    @Override
                                    public Boolean call() throws Exception {
                                        return !e.isDead() && e.isValid();
                                    }

                                });

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
                        plugin.getLogger().info("Entity tracking has stopped. Plugin is disabling...");
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
    public void reload() {
        super.reload();

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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRiding(VehicleMoveEvent e) {
        if(e.getVehicle().getPassengers().size() >= 1) {
                if (e.getVehicle().getPassengers().get(0).getType() == EntityType.PLAYER) {
                    List<Map.Entry<Area, AreaTrigger>> from = getAreaForLocation(new SimpleLocation(e.getFrom().getWorld().getName(), (int) e.getFrom().getX(), (int) e.getFrom().getY(), (int) e.getFrom().getZ()));
                    List<Map.Entry<Area, AreaTrigger>> to = getAreaForLocation(new SimpleLocation(e.getTo().getWorld().getName(), (int) e.getTo().getX(), (int) e.getTo().getY(), (int) e.getTo().getZ()));
                    Map<String, Object> varMap = new HashMap<>();
                    varMap.put("player", e.getVehicle().getPassengers().get(0));
                    varMap.put("from", e.getFrom());
                    varMap.put("to", e.getTo());
                    from.stream()
                            .filter((entry) -> !entry.getKey().isInThisArea(new SimpleLocation(e.getTo().getWorld().getName(), (int) e.getTo().getX(), (int) e.getTo().getY(), (int) e.getTo().getZ())))//only for area leaving
                            .map(Map.Entry::getValue)
                            .forEach((trigger) -> {
                                trigger.removeEntity(e.getVehicle().getPassengers().get(0).getUniqueId());
                                trigger.activate(e, varMap, EventType.EXIT);
                            });


                    to.stream()
                            .filter((entry) -> !entry.getKey().isInThisArea(new SimpleLocation(e.getFrom().getWorld().getName(), (int) e.getFrom().getX(), (int) e.getFrom().getY(), (int) e.getFrom().getZ())))//only for entering area
                            .map(Map.Entry::getValue)
                            .forEach((trigger) -> {
                                trigger.addEntity(new BukkitEntity(e.getVehicle().getPassengers().get(0)));
                                trigger.activate(e, varMap, EventType.ENTER);
                            });


            }
        }
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
