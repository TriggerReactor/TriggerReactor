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

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class AreaTriggerManager extends AbstractAreaTriggerManager implements BukkitTriggerManager{

    public AreaTriggerManager(TriggerReactor plugin) {
        super(plugin, new CommonFunctions(plugin), new File(plugin.getDataFolder(), "AreaTrigger"));

        Thread entityTrackingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                //track entity locations
                for(World w : Bukkit.getWorlds()) {
                    for(Entity e : w.getEntities()) {
                        UUID uuid = e.getUniqueId();

                        Future<Boolean> future = plugin.callSyncMethod(new Callable<Boolean>() {

                            @Override
                            public Boolean call() throws Exception {
                                return !e.isDead() && e.isValid();
                            }

                        });

                        boolean valid = false;
                        try {
                            valid = future.get();
                        } catch (InterruptedException e1) {
                        } catch (ExecutionException e1) {
                            e1.printStackTrace();
                        }

                        if(!valid)
                            continue;

                        if(!entityLocationMap.containsKey(uuid))
                            continue;

                        SimpleLocation previous = entityLocationMap.get(uuid);
                        SimpleLocation current = LocationUtil.convertToSimpleLocation(e.getLocation());

                        //update location if equal
                        if(!previous.equals(current)) {
                            entityLocationMap.put(uuid, current);
                            onEntityBlockMoveAsync(e, previous, current);
                        }

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

        entityLocationMap.clear();

        //re-register entities
        for(World w : Bukkit.getWorlds()) {
            for(Entity e : w.getEntities()) {
                UUID uuid = e.getUniqueId();

                if(e.isDead() || !e.isValid())
                    continue;

                SimpleLocation previous = null;
                SimpleLocation current = LocationUtil.convertToSimpleLocation(e.getLocation());

                entityLocationMap.put(uuid, current);
                onEntityBlockMoveAsync(e, previous, current);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLocationChange(PlayerBlockLocationEvent e){
        Entry<Area, AreaTrigger> from = getAreaForLocation(e.getFrom());
        Entry<Area, AreaTrigger> to = getAreaForLocation(e.getTo());

        if(from == null && to == null)
            return;

        if(from != null && to != null && from.getKey().equals(to.getKey()))
            return;

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", e.getPlayer());
        varMap.put("from", e.getFrom());
        varMap.put("to", e.getTo());

        if(from != null){
            from.getValue().removeEntity(e.getPlayer().getUniqueId());

            varMap.put("entities", from.getValue().getEntities());
            from.getValue().activate(e, varMap, EventType.EXIT);
        }

        if(to != null){
            to.getValue().addEntity(e.getPlayer());

            varMap.put("entities", to.getValue().getEntities());
            to.getValue().activate(e, varMap, EventType.ENTER);
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getLocation());

        entityTrackMap.put(e.getEntity().getUniqueId(), new WeakReference<Entity>(e.getEntity()));
        entityLocationMap.put(e.getEntity().getUniqueId(), sloc);

        Entry<Area, AreaTrigger> entry = getAreaForLocation(sloc);
        if(entry != null) {
            entry.getValue().addEntity(e.getEntity());
        }
    }

    protected synchronized void onEntityBlockMoveAsync(Entity entity, SimpleLocation from, SimpleLocation current) {
        Entry<Area, AreaTrigger> fromArea = getAreaForLocation(from);
        Entry<Area, AreaTrigger> toArea = getAreaForLocation(current);

        if(fromArea != null) {
            fromArea.getValue().removeEntity(entity.getUniqueId());
        }

        if(toArea != null) {
            toArea.getValue().addEntity(entity);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getEntity().getLocation());

        entityTrackMap.remove(e.getEntity().getUniqueId());
        entityLocationMap.remove(e.getEntity().getUniqueId());

        Entry<Area, AreaTrigger> area = getAreaForLocation(sloc);
        if(area != null) {
            area.getValue().removeEntity(e.getEntity().getUniqueId());
        }
    }

    @Override
    protected void deleteInfo(Trigger trigger) {
        File areafile = new File(folder, trigger.getTriggerName()+".yml");
        FileUtil.delete(areafile);
        File areafolder = new File(folder, trigger.getTriggerName());
        FileUtil.delete(areafolder);
    }
}
