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
package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactories;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTaggedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.scope.ManagerScope;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ManagerScope
public class AreaTriggerManager extends AbstractTaggedTriggerManager<AreaTrigger> {
    @Inject
    AreaTriggerFactory factory;
    @Inject
    IPluginLifecycleController pluginLifecycleController;
    @Inject
    IGameController gameController;
    @Inject
    TaskSupervisor taskSupervisor;
    @Inject
    Logger logger;
    @Inject
    ConfigSourceFactories configSourceFactories;

    /**
     * The child class should update this map with its own way. Though, the entity which garbage-corrected will
     * be also deleted from this map automatically.
     * <b>Adding or removing from this map also has to be reflected in entityTrackMap as well</b>
     */
    protected final Map<UUID, SimpleLocation> entityLocationMap = new ConcurrentHashMap<>();
    /**
     * The actual entity map.
     * <b>Adding or removing from this map also has to be reflected in entityLocationMap as well</b>
     */
    protected final Map<UUID, WeakReference<Object>> entityTrackMap = new ConcurrentHashMap<>();
    private final EntityTrackingThread entityTrackingThread = new EntityTrackingThread(this);
    protected Map<SimpleChunkLocation, Map<Area, AreaTrigger>> areaTriggersByLocation = new ConcurrentHashMap<>();

    @Inject
    AreaTriggerManager() {
        super("AreaTrigger");

        Thread referenceCleaningThread = new Thread(() -> {
            while (pluginLifecycleController.isEnabled() && !Thread.interrupted()) {
                //clean up the reference map
                Set<UUID> deletes = new HashSet<>();
                for (Entry<UUID, WeakReference<Object>> entry : entityTrackMap.entrySet()) {
                    if (entry.getValue().get() == null)
                        deletes.add(entry.getKey());
                }

                for (UUID delete : deletes) {
                    entityTrackMap.remove(delete);
                    entityLocationMap.remove(delete);
                }

                //clean up the area trigger tracked entities
                getAllTriggers().forEach(AreaTrigger::getEntities);

                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {

                }
            }
        });

        referenceCleaningThread.setName("AbstractAreaTriggerManager -- ReferenceCleaningThread");
        referenceCleaningThread.setDaemon(true);
        referenceCleaningThread.start();
    }

    @Override
    public AreaTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        File scriptFolder = new File(folder, info.getTriggerName());
        if (!scriptFolder.exists()) {
            scriptFolder.mkdirs();
        }

        String enterScript = null;
        File enterFile = null;
        try {
            enterFile = getTriggerFile(scriptFolder, "Enter", false);
            if (!enterFile.exists())
                enterFile.createNewFile();
            enterScript = FileUtil.readFromFile(enterFile);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        String exitScript = null;
        File exitFile = null;
        try {
            exitFile = getTriggerFile(scriptFolder, "Exit", false);
            if (!exitFile.exists())
                exitFile.createNewFile();
            exitScript = FileUtil.readFromFile(exitFile);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        AreaTrigger trigger = factory.create(info, scriptFolder);
        try {
            trigger.setEnterTrigger(enterScript);
            trigger.setExitTrigger(exitScript);
        } catch (TriggerInitFailedException e) {
            e.printStackTrace();
            return null;
        }

        return trigger;
    }

    @Override
    public void save(AreaTrigger trigger) {
        // area trigger doesn't have script itself but its children does
        //super.save(trigger);

        File triggerFolder = new File(folder, trigger.getTriggerName());
        if (!triggerFolder.exists()) {
            triggerFolder.mkdirs();
        }

        if (trigger.getEnterTrigger() != null) {
            try {
                FileUtil.writeToFile(getTriggerFile(triggerFolder, "Enter", true),
                        trigger.getEnterTrigger().getScript());
            } catch (IOException e) {
                e.printStackTrace();
                logger.warning("Could not save Area Trigger [Enter] " + trigger);
            }
        }

        if (trigger.getExitTrigger() != null) {
            try {
                FileUtil.writeToFile(getTriggerFile(triggerFolder, "Exit", true), trigger.getExitTrigger().getScript());
            } catch (IOException e) {
                e.printStackTrace();
                logger.warning("Could not save Area Trigger [Exit] " + trigger);
            }
        }
    }

    @Override
    public TriggerInfo[] listTriggers(File folder, ConfigSourceFactories fn) {
        return Optional.ofNullable(folder.listFiles())
                .map(files -> Arrays.stream(files).filter(File::isDirectory).map(file -> {
                    String name = file.getName();
                    IConfigSource config = fn.create(folder, name);
                    return toTriggerInfo(file, config);
                }).toArray(TriggerInfo[]::new))
                .orElse(new TriggerInfo[0]);
    }

    @Override
    public TriggerInfo toTriggerInfo(File file, IConfigSource configSource) {
        return new AreaTriggerInfo(file, configSource, file.getName());
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() throws Exception {
        super.onEnable();

        entityTrackingThread.setName("AreaTriggerManager -- EntityTrackingThread");
        entityTrackingThread.setDaemon(true);
        entityTrackingThread.start();
    }

    @Override
    public void onReload() {
        entityLocationMap.clear();
        entityTrackMap.clear();

        super.onReload();

        areaTriggersByLocation.clear();

        for (AreaTrigger trigger : getAllTriggers()) {
            this.setupArea(trigger);
        }

        //re-register entities
        for (IWorld w : gameController.getWorlds()) {
            for (IEntity e : w.getEntities()) {
                UUID uuid = e.getUniqueId();

                if (e.isDead() || !e.isValid())
                    continue;

                SimpleLocation previous = null;
                SimpleLocation current = e.getLocation();

                entityLocationMap.put(uuid, current);
                entityTrackMap.put(uuid, new WeakReference<>(e.get()));
                onEntityBlockMoveAsync(uuid, e.get(), previous, current);
            }
        }
    }

    @Override
    public AreaTrigger remove(String name) {
        AreaTrigger remove = super.remove(name);

        Optional.ofNullable(remove).ifPresent(areaTrigger -> {
            Area area = areaTrigger.getArea();
            for (SimpleChunkLocation scloc : Area.getAllChunkLocations(area)) {
                Map<Area, AreaTrigger> map = areaTriggersByLocation.get(scloc);
                map.remove(area);
            }
        });

        return remove;
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

    protected synchronized void onEntityBlockMoveAsync(UUID entityUuid,
                                                       Object entity,
                                                       SimpleLocation from,
                                                       SimpleLocation current) {
        getAreaForLocation(from).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.removeEntity(entityUuid));
        getAreaForLocation(current).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.addEntity(entityUuid, entity));
    }

    /**
     * Get list of all Area Triggers containing this sloc.
     *
     * @param sloc
     * @return list of Entries containing the given sloc. It may be empty but never null.
     */
    protected List<Map.Entry<Area, AreaTrigger>> getAreaForLocation(SimpleLocation sloc) {
        if (sloc == null)
            return new ArrayList<>();

        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);
        if (!areaTriggersByLocation.containsKey(scloc))
            return new ArrayList<>();

        List<Map.Entry<Area, AreaTrigger>> list = areaTriggersByLocation.get(scloc)
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().isInThisArea(sloc))
                .collect(Collectors.toList());

        return list;
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

        File areaFolder = new File(folder, name);
        IConfigSource config = configSourceFactories.create(folder, name);
        AreaTrigger trigger = factory.create(new AreaTriggerInfo(areaFolder, config, name), areaFolder);
        trigger.setArea(area);
        put(name, trigger);

        setupArea(trigger);

        return true;
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
     * Try to get Area Triggers at given location
     *
     * @param sloc
     * @return list of areas that contains the sloc. It may be empty but never null.
     */
    public List<Map.Entry<Area, AreaTrigger>> getAreas(SimpleLocation sloc) {
        return getAreaForLocation(sloc);
    }

    public void onEntityDeath(SimpleLocation diedAt, UUID entityUuid) {
        entityTrackMap.remove(entityUuid);
        entityLocationMap.remove(entityUuid);

        getAreaForLocation(diedAt).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.removeEntity(entityUuid));
    }

    public void onPlayerSpawn(SimpleLocation currentSloc, IPlayer entity) {
        getAreaForLocation(currentSloc).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.addEntity(entity.getUniqueId(), entity.get()));
    }

    /**
     * @param loc        current location as spawned
     * @param entityUuid uuid of the entity
     * @param entity     raw entity. Do not wrap it
     */
    public void onEntitySpawn(SimpleLocation loc, UUID entityUuid, Object entity) {
        if (entity instanceof IPlayer)
            throw new RuntimeException("Do not register player as entity. Use onPlayerSpawn instead.");

        entityTrackMap.put(entityUuid, new WeakReference<>(entity));
        entityLocationMap.put(entityUuid, loc);

        getAreaForLocation(loc).stream()
                .map(Map.Entry::getValue)
                .forEach((trigger) -> trigger.addEntity(entityUuid, entity));
    }

    public void onLocationChange(IPlayer player, SimpleLocation f, SimpleLocation t) {
        List<Map.Entry<Area, AreaTrigger>> from = getAreaForLocation(f);
        List<Map.Entry<Area, AreaTrigger>> to = getAreaForLocation(t);
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", player.get());
        varMap.put("from", f);
        varMap.put("to", t);

        from.stream().filter((entry) -> !entry.getKey().isInThisArea(t))//only for area leaving
                .map(Map.Entry::getValue).forEach((trigger) -> {
                    trigger.removeEntity(player.getUniqueId());
                    trigger.activate(varMap, AreaTriggerManager.EventType.EXIT);
                });


        to.stream().filter((entry) -> !entry.getKey().isInThisArea(f))//only for entering area
                .map(Map.Entry::getValue).forEach((trigger) -> {
                    trigger.addEntity(player.getUniqueId(), player.get());
                    trigger.activate(varMap, AreaTriggerManager.EventType.ENTER);
                });
    }

    public enum EventType {
        ENTER,
        EXIT
    }
}