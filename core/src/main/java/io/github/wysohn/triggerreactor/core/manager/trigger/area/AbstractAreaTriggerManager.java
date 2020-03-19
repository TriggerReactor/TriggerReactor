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
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTaggedTriggerManager;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;
import java.io.FileFilter;
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

    public AbstractAreaTriggerManager(TriggerReactorCore plugin, File tirggerFolder) {
        super(plugin, tirggerFolder);

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

                    Set<String> keys = triggers.keySet();
                    for (String key : keys) {
                        AreaTrigger area = triggers.get(key);
                        area.getEntities();
                    }

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

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".yml");
            }
        };

        areaTriggersByLocation.clear();

        for (File ymlfile : folder.listFiles(filter)) {
            String[] extracted = extractPrefix(extractName(ymlfile));
            String triggerName = extracted[1];

            SimpleLocation smallest = null;
            SimpleLocation largest = null;
            boolean isSync = false;
            try {
                smallest = SimpleLocation.valueOf(getData(ymlfile, SMALLEST));
                largest = SimpleLocation.valueOf(getData(ymlfile, LARGEST));
                isSync = getData(ymlfile, SYNC, false);
            } catch (IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load Area Trigger " + ymlfile);
                continue;
            }

            if (smallest == null || largest == null) {
                plugin.getLogger().warning("Could not load Area Trigger" + ymlfile);
                plugin.getLogger().warning("Could not find Smallest: or Largest:");
                continue;
            }

            File scriptFolder = new File(folder, triggerName);
            if (!scriptFolder.exists()) {
                scriptFolder.mkdirs();
            }

            String enterScript = null;
            File enterFile = null;
            try {
                enterFile = getTriggerFile(scriptFolder, "Enter", false);
                enterScript = FileUtil.readFromFile(enterFile);
            } catch (IOException e1) {
                e1.printStackTrace();
                continue;
            }

            String exitScript = null;
            File exitFile = null;
            try {
                exitFile = getTriggerFile(scriptFolder, "Exit", false);
                exitScript = FileUtil.readFromFile(exitFile);
            } catch (IOException e1) {
                e1.printStackTrace();
                continue;
            }

            Area area = new Area(smallest, largest);
            AreaTrigger trigger = new AreaTrigger(area, scriptFolder, triggerName);
            trigger.setSync(isSync);

            triggers.put(triggerName, trigger);

            this.setupArea(trigger);

            try {
                if (enterScript != null) {
                    trigger.setEnterTrigger(enterScript, enterFile);
                }
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
                continue;
            }

            try {
                if (exitScript != null) {
                    trigger.setExitTrigger(exitScript, exitFile);
                }
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    @Override
    public void saveAll() {
        Set<AreaTrigger> saveReady = new HashSet<>();

        for (Entry<SimpleChunkLocation, Map<Area, AreaTrigger>> oentry : areaTriggersByLocation.entrySet()) {

            for (Entry<Area, AreaTrigger> entry : oentry.getValue().entrySet()) {
                AreaTrigger trigger = entry.getValue();

                saveReady.add(trigger);
            }
        }

        for (AreaTrigger trigger : saveReady) {
            Area area = trigger.getArea();

            File ymlfile = new File(folder, trigger.getTriggerName() + ".yml");
            if (!ymlfile.exists()) {
                try {
                    ymlfile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("Could not create " + ymlfile);
                    continue;
                }
            }

            try {
                setData(ymlfile, SMALLEST, area.getSmallest().toString());
                setData(ymlfile, LARGEST, area.getLargest().toString());
                setData(ymlfile, SYNC, trigger.isSync());
            } catch (IOException e1) {
                e1.printStackTrace();
                continue;
            }

            File triggerFolder = new File(folder, trigger.getTriggerName());
            if (!triggerFolder.exists()) {
                triggerFolder.mkdirs();
            }

            if (trigger.getEnterTrigger() != null) {
                try {
                    FileUtil.writeToFile(getTriggerFile(triggerFolder, "Enter", true), trigger.getEnterTrigger().getScript());
                } catch (IOException e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("Could not save Area Trigger [Enter] " + trigger.getTriggerName());
                }
            }

            if (trigger.getExitTrigger() != null) {
                try {
                    FileUtil.writeToFile(getTriggerFile(triggerFolder, "Exit", true), trigger.getExitTrigger().getScript());
                } catch (IOException e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("Could not save Area Trigger [Exit] " + trigger.getTriggerName());
                }
            }
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
     * This method does not check if world of smallest and largest are same.
     * Also <b>check confliction with {@link #getConflictingAreas(Area, Predicate)} before</b> using this method.
     *
     * @param name
     * @param smallest
     * @param largest
     * @return true on success; false if exact same area already exist.
     */
    public boolean createArea(String name, SimpleLocation smallest, SimpleLocation largest) {
        Area area = new Area(smallest, largest);

        // exact same area found
        if (!getConflictingAreas(area, area::equals).isEmpty())
            return false;

        File areaFolder = new File(folder, name);
        AreaTrigger trigger = new AreaTrigger(area, areaFolder, name);
        triggers.put(name, trigger);

        setupArea(trigger);

        return true;
    }

    /**
     * reset the area cache. Should be called for reloading.
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

    /**
     * Try to get Area Trigger by given name
     *
     * @param name
     * @return Area if found; null if nothing
     */
    public AreaTrigger getArea(String name) {
        return triggers.get(name);
    }

    /**
     * Try to remove Area Trigger by given name
     *
     * @param name
     * @return false if can't find any Area Trigger with the name; true if deleted.
     */
    public boolean deleteArea(String name) {
        AreaTrigger trigger = triggers.get(name);
        if (trigger == null)
            return false;

        for (SimpleChunkLocation scloc : Area.getAllChunkLocations(trigger.area)) {
            Map<Area, AreaTrigger> map = areaTriggersByLocation.get(scloc);
            map.remove(trigger.area);
        }

        deleteInfo(trigger);

        triggers.remove(name);
        return true;
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

    @Override
    protected void deleteInfo(AreaTrigger trigger) {
        FileUtil.delete(new File(folder, trigger.getTriggerName()));
    }

}