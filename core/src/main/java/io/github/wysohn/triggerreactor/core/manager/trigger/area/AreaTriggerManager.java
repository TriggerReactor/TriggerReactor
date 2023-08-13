/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.IGameManagement;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTaggedTriggerManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
public class AreaTriggerManager extends AbstractTaggedTriggerManager<AreaTrigger> {
    @Inject
    private IAreaTriggerFactory factory;
    @Inject
    private TaskSupervisor task;
    @Inject
    private IGameManagement gameState;
    @Inject
    private IPluginManagement pluginManagement;

    private Map<SimpleChunkLocation, Map<Area, AreaTrigger>> areaTriggersByLocation = new ConcurrentHashMap<>();

    /**
     * The child class should update this map with its own way. Though, the entity which garbage-corrected will
     * be also deleted from this map automatically.
     * <b>Adding or removing from this map also has to be reflected in entityTrackMap as well</b>
     */
    private final Map<UUID, SimpleLocation> entityLocationMap = new ConcurrentHashMap<>();
    /**
     * The actual entity map.
     * <b>Adding or removing from this map also has to be reflected in entityLocationMap as well</b>
     */
    protected final Map<UUID, WeakReference<IEntity>> entityTrackMap = new ConcurrentHashMap<>();

    @Inject
    private AreaTriggerManager(@Named("DataFolder") File dataFolder,
                               @Named("AreaTriggerManagerFolder") String folderName) {
        super(new File(dataFolder, folderName));
    }

    protected synchronized void onEntityBlockMoveAsync(IEntity entity, SimpleLocation from, SimpleLocation current) {
        getAreaForLocation(from).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.removeEntity(entity.getUniqueId()));
        getAreaForLocation(current).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.addEntity(entity));
    }

    @Override
    public void initialize() {
        Thread entityTrackingThread = new Thread(new Runnable() {

            private Collection<WeakReference<IEntity>> getEntitiesSync(IWorld w) {
                Collection<WeakReference<IEntity>> entities = new LinkedList<>();
                try {
                    task.submitSync(() -> {
                        for (IEntity e : w.getEntities())
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
                while (pluginManagement.isEnabled() && !Thread.interrupted()) {
                    try {
                        //track entity locations
                        for (IWorld w : gameState.getWorlds()) {
                            Collection<WeakReference<IEntity>> entityCollection = getEntitiesSync(w);
                            for (WeakReference<IEntity> wr : entityCollection) {
                                IEntity e = wr.get();

                                //reference disposed so ignore
                                if (e == null)
                                    continue;

                                UUID uuid = e.getUniqueId();

                                if (!pluginManagement.isEnabled())
                                    break;

                                Future<Boolean> future = task.submitSync(() -> !e.isDead() && e.isValid());

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
                                SimpleLocation current = e.getLocation().toSimpleLocation();

                                //update location if equal
                                if (!previous.equals(current)) {
                                    entityLocationMap.put(uuid, current);
                                    onEntityBlockMoveAsync(e, previous, current);
                                }

                            }
                        }
                    } catch (Exception ex) {
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

        Thread referenceCleaningThread = new Thread() {

            @Override
            public void run() {
                while (pluginManagement.isEnabled() && !Thread.interrupted()) {
                    //clean up the reference map
                    Set<UUID> deletes = new HashSet<>();
                    for (Entry<UUID, WeakReference<IEntity>> entry : entityTrackMap.entrySet()) {
                        if (entry.getValue().get() == null)
                            deletes.add(entry.getKey());
                    }
                    for (UUID delete : deletes) {
                        entityTrackMap.remove(delete);
                        entityLocationMap.remove(delete);
                    }

                    getAllTriggers().forEach(AreaTrigger::getEntities);

                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException e) {

                    }
                }
            }

        };

        referenceCleaningThread.setName("AbstractAreaTriggerManager -- ReferenceCleaningThread");
        referenceCleaningThread.setDaemon(true);
        referenceCleaningThread.start();
    }

    @Override
    public void reload() {
        entityLocationMap.clear();
        entityTrackMap.clear();

        super.reload();

        areaTriggersByLocation.clear();

        for (AreaTrigger trigger : getAllTriggers()) {
            this.setupArea(trigger);
        }

        //re-register entities
        for (IWorld w : gameState.getWorlds()) {
            for (IEntity e : w.getEntities()) {
                UUID uuid = e.getUniqueId();

                if (e.isDead() || !e.isValid())
                    continue;

                SimpleLocation previous = null;
                SimpleLocation current = e.getLocation().toSimpleLocation();

                entityLocationMap.put(uuid, current);
                entityTrackMap.put(uuid, new WeakReference<>(e));
                onEntityBlockMoveAsync(e, previous, current);
            }
        }
    }

    /**
     * Start tracking the entity so the area triggers can be notified when the entity moves
     * into/out of an area.
     *
     * @param entity
     * @param current
     */
    public void startTrackingEntity(IEntity entity, SimpleLocation current) {
        entityTrackMap.put(entity.getUniqueId(), new WeakReference<>(entity));
        entityLocationMap.put(entity.getUniqueId(), current);
    }

    public void stopTrackingEntity(UUID entity) {
        entityTrackMap.remove(entity);
        entityLocationMap.remove(entity);
    }

    /**
     * Get list of all Area Triggers containing this sloc.
     *
     * @param sloc
     * @return list of Entries containing the given sloc. It may be empty but never null.
     */
    public List<Map.Entry<Area, AreaTrigger>> getAreaForLocation(SimpleLocation sloc) {
        if (sloc == null)
            return new ArrayList<>();

        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);
        if (!areaTriggersByLocation.containsKey(scloc))
            return new ArrayList<>();

        List<Map.Entry<Area, AreaTrigger>> list = areaTriggersByLocation.get(scloc).entrySet().stream()
                .filter(entry -> entry.getKey().isInThisArea(sloc))
                .collect(Collectors.toList());

        return list;
    }

    /**
     * get all the area that is conflicting with given area. This does not include the area itself.
     * It's quite a CPU intensive work; use it wisely
     *
     * @param area
     * @param filter decide what it means by 'conflict' between the given area and other areas
     * @return never be null; can be empty if no conflicts are found
     */
    public Set<Area> getConflictingAreas(Area area, Predicate<Area> filter) {
        Set<Area> conflicts = new HashSet<>();

        Set<SimpleChunkLocation> sclocs = Area.getAllChunkLocations(area);
        for (SimpleChunkLocation scloc : sclocs) {
            Map<Area, AreaTrigger> map = areaTriggersByLocation.get(scloc);
            if (map == null)
                continue;

            for (Entry<Area, AreaTrigger> mapentry : map.entrySet()) {
                Area areaOther = mapentry.getKey();

                if (filter.test(areaOther))
                    conflicts.add(areaOther);
            }
        }

        return conflicts;
    }

    /**
     * Create a new Area Trigger.
     *
     * @param name     name of the Area Trigger.
     * @param smallest smallest point (ex. 0,0,0)
     * @param largest  largest point(ex. 15,15,15)
     * @return true on success; false if exact same area (same smallest and largest) already exist.
     */
    public boolean createArea(String name, SimpleLocation smallest, SimpleLocation largest) {
        Area area = new Area(smallest, largest);

        // exact same area found
        if (!getConflictingAreas(area, area::equals).isEmpty())
            return false;

        File areaFolder = concatPath(folder, name);
        IConfigSource config = getConfigSource(folder, name);
        AreaTrigger trigger = factory.create(new AreaTriggerInfo(areaFolder, config, name), area, areaFolder);
        put(name, trigger);

        setupArea(trigger);

        return true;
    }

    /**
     * reset the area cache. Should be called on reload or on Area Trigger created.
     *
     * @param trigger
     */
    protected void setupArea(AreaTrigger trigger) {
        Area area = trigger.getArea();

        Set<SimpleChunkLocation> sclocs = Area.getAllChunkLocations(area);
        for (SimpleChunkLocation scloc : sclocs) {
            Map<Area, AreaTrigger> map = areaTriggersByLocation.get(scloc);
            if (map == null) {
                map = new ConcurrentHashMap<>();
                areaTriggersByLocation.put(scloc, map);
            }

            map.put(area, trigger);
        }
    }

    @Override
    public AreaTrigger remove(String name) {
        AreaTrigger remove = super.remove(name);

        Optional.ofNullable(remove).ifPresent(areaTrigger -> {
            for (SimpleChunkLocation scloc : Area.getAllChunkLocations(areaTrigger.getArea())) {
                Map<Area, AreaTrigger> map = areaTriggersByLocation.get(scloc);
                map.remove(areaTrigger.getArea());
            }
        });

        return remove;
    }

    /**
     * Try to get Area Triggers at given location
     *
     * @param sloc
     * @return list of areas that contains the sloc. It may be empty but never null.
     */
    public List<Map.Entry<Area, AreaTrigger>> getAreas(SimpleLocation sloc) {
        return getAreaForLocation(sloc);
    }

    /**
     * Try to remove Area Trigger at given location.
     *
     * @param sloc
     * @return false if no area found at location; true if deleted
     * @deprecated this is not valid anymore as there can be more than one Area Trigger.
     */
    @Deprecated
    public boolean deleteArea(SimpleLocation sloc) {
        /*Entry<Area, AreaTrigger> areaEntry = getAreaForLocation(sloc);
        if(areaEntry == null)
            return false;

        AreaTrigger trigger = areaEntry.getValue();

        for(SimpleChunkLocation scloc : Area.getAllChunkLocations(areaEntry.getKey())){
            Map<Area, AreaTrigger> map = areaTriggers.get(scloc);
            map.remove(areaEntry.getKey());
        }

        deleteInfo(nameMapper.remove(trigger.getTriggerName()));*/
        return false;
    }

    /**
     * @deprecated only to be called by listener or test code
     */
    public void onJoin(SimpleLocation sloc, IEntity player) {
        getAreaForLocation(sloc).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.addEntity(player));
    }

    /**
     * @deprecated only to be called by listener or test code
     */
    public void onLocationChange(Object eventInstance,
                                 SimpleLocation slocFrom,
                                 SimpleLocation slocTo,
                                 IPlayer player) {
        List<Map.Entry<Area, AreaTrigger>> from = getAreaForLocation(slocFrom);
        List<Map.Entry<Area, AreaTrigger>> to = getAreaForLocation(slocTo);
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", player.get());
        varMap.put("from", slocFrom);
        varMap.put("to", slocTo);

        from.stream()
                .filter((entry) -> !entry.getKey().isInThisArea(slocTo))//only for area leaving
                .map(Map.Entry::getValue)
                .forEach((trigger) -> {
                    trigger.removeEntity(player.getUniqueId());
                    trigger.activate(eventInstance, varMap, AreaTriggerManager.EventType.EXIT);
                });


        to.stream()
                .filter((entry) -> !entry.getKey().isInThisArea(slocFrom))//only for entering area
                .map(Map.Entry::getValue)
                .forEach((trigger) -> {
                    trigger.addEntity(player);
                    trigger.activate(eventInstance, varMap, AreaTriggerManager.EventType.ENTER);
                });
    }

    /**
     * @deprecated only to be called by listener or test code
     */
    public void onSpawn(IEntity entity, SimpleLocation sloc) {
        startTrackingEntity(entity, sloc);
        getAreaForLocation(sloc).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.addEntity(entity));
    }

    /**
     * @deprecated only to be called by listener or test code
     */
    public void onDeath(UUID entity, SimpleLocation sloc) {
        stopTrackingEntity(entity);
        getAreaForLocation(sloc).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.removeEntity(entity));
    }

    public static final String FOLDER_NAME = "AreaTrigger";

    public enum EventType {
        ENTER,
        EXIT
    }

}
