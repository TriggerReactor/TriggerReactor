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
package io.github.wysohn.triggerreactor.sponge.manager.trigger;

import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTrigger;
import io.github.wysohn.triggerreactor.sponge.bridge.entity.SpongeEntity;
import io.github.wysohn.triggerreactor.sponge.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.sponge.tools.LocationUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.World;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AreaTriggerManager extends AbstractAreaTriggerManager implements SpongeConfigurationFileIO {

    public AreaTriggerManager(TriggerReactorCore plugin) {
        super(plugin, new File(plugin.getDataFolder(), "AreaTrigger"));

        Thread entityTrackingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (plugin.isEnabled() && !Thread.interrupted()) {
                    //track entity locations
                    for (World w : Sponge.getServer().getWorlds()) {
                        for (Entity e : w.getEntities()) {
                            UUID uuid = e.getUniqueId();

                            Future<Boolean> future = plugin.callSyncMethod(new Callable<Boolean>() {

                                @Override
                                public Boolean call() throws Exception {
                                    return !e.isRemoved() && e.isLoaded();
                                }

                            });

                            boolean valid = false;
                            try {
                                valid = future.get();
                            } catch (InterruptedException e1) {
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
        for (World w : Sponge.getServer().getWorlds()) {
            for (Entity e : w.getEntities()) {
                UUID uuid = e.getUniqueId();

                if (e.isRemoved() || !e.isLoaded())
                    continue;

                SimpleLocation previous = null;
                SimpleLocation current = LocationUtil.convertToSimpleLocation(e.getLocation());

                entityLocationMap.put(uuid, current);
                entityTrackMap.put(uuid, new WeakReference<IEntity>(new SpongeEntity(e)));
                onEntityBlockMoveAsync(e, previous, current);
            }
        }
    }

    @Listener(order = Order.POST)
    public void onJoin(ClientConnectionEvent.Join e) {
        SimpleLocation currentSloc = LocationUtil.convertToSimpleLocation(e.getTargetEntity().getLocation());
        getAreaForLocation(currentSloc).stream()
                .map(Entry::getValue)
                .forEach((trigger) -> trigger.addEntity(new SpongeEntity(e.getTargetEntity())));
    }

    @Listener(order = Order.POST)
    public void onLocationChange(PlayerBlockLocationEvent e) {
        List<Entry<Area, AreaTrigger>> from = getAreaForLocation(e.getFrom());
        List<Entry<Area, AreaTrigger>> to = getAreaForLocation(e.getTo());

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", e.getTargetEntity());
        varMap.put("from", e.getFrom());
        varMap.put("to", e.getTo());

        from.stream()
                .filter((entry) -> !entry.getKey().isInThisArea(e.getTo()))//only for area leaving
                .map(Entry::getValue)
                .forEach((trigger) -> {
                    trigger.removeEntity(e.getTargetEntity().getUniqueId());
                    trigger.activate(e, varMap, EventType.EXIT);
                });


        to.stream()
                .filter((entry) -> !entry.getKey().isInThisArea(e.getFrom()))//only for entering area
                .map(Entry::getValue)
                .forEach((trigger) -> {
                    trigger.addEntity(new SpongeEntity(e.getTargetEntity()));
                    trigger.activate(e, varMap, EventType.ENTER);
                });
    }

    @Listener
    public void onSpawn(RespawnPlayerEvent e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getTargetEntity().getLocation());

        entityTrackMap.put(e.getTargetEntity().getUniqueId(), new WeakReference<IEntity>(new SpongeEntity(e.getTargetEntity())));
        entityLocationMap.put(e.getTargetEntity().getUniqueId(), sloc);

        getAreaForLocation(sloc).stream()
                .map(Entry::getValue)
                .forEach((trigger) -> trigger.addEntity(new SpongeEntity(e.getTargetEntity())));
    }

    protected synchronized void onEntityBlockMoveAsync(Entity entity, SimpleLocation from, SimpleLocation current) {
        getAreaForLocation(from).stream()
                .map(Entry::getValue)
                .forEach((trigger) -> trigger.removeEntity(entity.getUniqueId()));
        getAreaForLocation(current).stream()
                .map(Entry::getValue)
                .forEach((trigger) -> trigger.addEntity(new SpongeEntity(entity)));
    }

    @Listener
    public void onDeath(DestructEntityEvent.Death e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getTargetEntity().getLocation());

        entityTrackMap.remove(e.getTargetEntity().getUniqueId());
        entityLocationMap.remove(e.getTargetEntity().getUniqueId());

        getAreaForLocation(sloc).stream()
                .map(Entry::getValue)
                .forEach((trigger) -> trigger.removeEntity(e.getTargetEntity().getUniqueId()));
    }
}
