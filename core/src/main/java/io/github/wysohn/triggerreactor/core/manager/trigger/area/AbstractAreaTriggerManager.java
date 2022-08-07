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

import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTaggedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractAreaTriggerManager extends AbstractTaggedTriggerManager<AreaTrigger> {
    protected static final String SMALLEST = "Smallest";
    protected static final String LARGEST = "Largest";
    protected static final String SYNC = "Sync";

    protected Map<SimpleChunkLocation, Map<Area, AreaTrigger>> areaTriggersByLocation = new ConcurrentHashMap<>();

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
    protected final Map<UUID, WeakReference<IEntity>> entityTrackMap = new ConcurrentHashMap<>();

    public AbstractAreaTriggerManager(TriggerReactorCore plugin, File folder) {
        super(plugin, folder, new ITriggerLoader<AreaTrigger>() {
            @Override
            public TriggerInfo[] listTriggers(File folder, ConfigSourceFactory fn) {
                return Optional.ofNullable(folder.listFiles())
                        .map(files -> Arrays.stream(files)
                                .filter(File::isDirectory)
                                .map(file -> {
                                    String name = file.getName();
                                    IConfigSource config = fn.create(folder, name);
                                    return toTriggerInfo(file, config);
                                })
                                .toArray(TriggerInfo[]::new))
                        .orElse(new TriggerInfo[0]);
            }

            @Override
            public TriggerInfo toTriggerInfo(File file, IConfigSource configSource) {
                return new AreaTriggerInfo(file, configSource, file.getName());
            }

            @Override
            public AreaTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
                SimpleLocation smallest = info.getConfig().get(SMALLEST, String.class)
                        .map(SimpleLocation::valueOf)
                        .orElseGet(() -> new SimpleLocation("unknown", 0, 0, 0));
                SimpleLocation largest = info.getConfig().get(LARGEST, String.class)
                        .map(SimpleLocation::valueOf)
                        .orElseGet(() -> new SimpleLocation("unknown", 0, 0, 0));
                boolean isSync = info.getConfig().get(SYNC, Boolean.class)
                        .orElse(false);

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

                Area area = new Area(smallest, largest);
                AreaTrigger trigger = new AreaTrigger(info, area, scriptFolder);

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
                Area area = trigger.getArea();
                trigger.getInfo().getConfig().put(SMALLEST, area.getSmallest().toString());
                trigger.getInfo().getConfig().put(LARGEST, area.getLargest().toString());

                File triggerFolder = new File(folder, trigger.getInfo().getTriggerName());
                if (!triggerFolder.exists()) {
                    triggerFolder.mkdirs();
                }

                if (trigger.getEnterTrigger() != null) {
                    try {
                        FileUtil.writeToFile(getTriggerFile(triggerFolder, "Enter", true), trigger.getEnterTrigger().getScript());
                    } catch (IOException e) {
                        e.printStackTrace();
                        plugin.getLogger().warning("Could not save Area Trigger [Enter] " + trigger.getInfo());
                    }
                }

                if (trigger.getExitTrigger() != null) {
                    try {
                        FileUtil.writeToFile(getTriggerFile(triggerFolder, "Exit", true), trigger.getExitTrigger().getScript());
                    } catch (IOException e) {
                        e.printStackTrace();
                        plugin.getLogger().warning("Could not save Area Trigger [Exit] " + trigger.getInfo());
                    }
                }
            }
        });

        Thread referenceCleaningThread = new Thread() {

            @Override
            public void run() {
                while (plugin.isEnabled() && !Thread.interrupted()) {
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

        File areaFolder = new File(folder, name);
        IConfigSource config = configSourceFactory.create(folder, name);
        AreaTrigger trigger = new AreaTrigger(new AreaTriggerInfo(areaFolder, config, name), area, areaFolder);
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
        Area area = trigger.area;

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
            for (SimpleChunkLocation scloc : Area.getAllChunkLocations(areaTrigger.area)) {
                Map<Area, AreaTrigger> map = areaTriggersByLocation.get(scloc);
                map.remove(areaTrigger.area);
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

    public enum EventType {
        ENTER, EXIT
    }
}