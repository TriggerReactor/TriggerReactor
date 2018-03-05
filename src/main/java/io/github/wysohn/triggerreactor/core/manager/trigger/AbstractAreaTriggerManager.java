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
package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public abstract class AbstractAreaTriggerManager extends AbstractTriggerManager {
    protected static final String SMALLEST = "Smallest";
    protected static final String LARGEST = "Largest";
    protected static final String SYNC = "Sync";

    protected Map<SimpleChunkLocation, Map<Area, AreaTrigger>> areaTriggers = new ConcurrentHashMap<>();
    protected Map<String, AreaTrigger> nameMapper = new HashMap<>();

    @Override
    public void reload() {
        FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".yml");
            }
        };

        areaTriggers.clear();

        for(File ymlfile : folder.listFiles(filter)){
            String triggerName = extractName(ymlfile);

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

            if(smallest == null || largest == null){
                plugin.getLogger().warning("Could not load Area Trigger"+ymlfile);
                plugin.getLogger().warning("Could not find Smallest: or Largest:");
                continue;
            }

            File scriptFolder = new File(folder, triggerName);
            if(!scriptFolder.exists()){
                scriptFolder.mkdirs();
            }

            String enterScript = null;
            File enterFile = null;
            try {
                enterFile = getTriggerFile(scriptFolder, "Enter.trg");
                enterScript = FileUtil.readFromFile(enterFile);
            } catch (IOException e1) {
                e1.printStackTrace();
                continue;
            }

            String exitScript = null;
            File exitFile = null;
            try {
                exitFile = getTriggerFile(scriptFolder, "Exit.trg");
                exitScript = FileUtil.readFromFile(exitFile);
            } catch (IOException e1) {
                e1.printStackTrace();
                continue;
            }

            Area area = new Area(smallest, largest);
            AreaTrigger trigger = new AreaTrigger(area, scriptFolder, triggerName);
            trigger.setSync(isSync);

            nameMapper.put(triggerName, trigger);

            this.setupArea(trigger);

            try {
                if(enterScript != null){
                    trigger.setEnterTrigger(enterScript);
                }
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
                continue;
            }

            try {
                if(exitScript != null){
                    trigger.setExitTrigger(exitScript);
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

        for(Entry<SimpleChunkLocation, Map<Area, AreaTrigger>> oentry : areaTriggers.entrySet()){
            SimpleChunkLocation scloc = oentry.getKey();

            for(Entry<Area, AreaTrigger> entry : oentry.getValue().entrySet()){
                Area area = entry.getKey();
                AreaTrigger trigger = entry.getValue();

                saveReady.add(trigger);
            }
        }

        for(AreaTrigger trigger : saveReady){
            Area area = trigger.getArea();

            File ymlfile = new File(folder, trigger.getTriggerName()+".yml");
            if(!ymlfile.exists()){
                try {
                    ymlfile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("Could not create "+ymlfile);
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
            if(!triggerFolder.exists()){
                triggerFolder.mkdirs();
            }

            if(trigger.getEnterTrigger() != null){
                try {
                    FileUtil.writeToFile(new File(triggerFolder, "Enter.trg"), trigger.getEnterTrigger().getScript());
                } catch (IOException e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("Could not save Area Trigger [Enter] "+trigger.getTriggerName());
                }
            }

            if(trigger.getExitTrigger() != null){
                try {
                    FileUtil.writeToFile(new File(triggerFolder, "Exit.trg"), trigger.getExitTrigger().getScript());
                } catch (IOException e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("Could not save Area Trigger [Exit] "+trigger.getTriggerName());
                }
            }
        }
    }

    protected Map.Entry<Area, AreaTrigger> getAreaForLocation(SimpleLocation sloc) {
        if(sloc == null)
            return null;

        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);
        if(!areaTriggers.containsKey(scloc))
            return null;

        for(Entry<Area, AreaTrigger> entry : areaTriggers.get(scloc).entrySet()){
            if(entry.getKey().isInThisArea(sloc))
                return entry;
        }

        return null;
    }

    /**
     * get all the area that is conflicting with given area. This does not include the area itself.
     * It's quite a CPU intensive work; use it wisely
     * @param area
     * @return never be null; can be empty if no conflicts are found
     */
    public Set<Area> getConflictingAreas(Area area) {
        Set<Area> conflicts = new HashSet<>();

        Set<SimpleChunkLocation> sclocs = Area.getAllChunkLocations(area);
        for(SimpleChunkLocation scloc : sclocs){
            Map<Area, AreaTrigger> map = areaTriggers.get(scloc);
            if(map == null)
                continue;

            for(Entry<Area, AreaTrigger> mapentry : map.entrySet()){
                Area areaOther = mapentry.getKey();

                if(area.equals(areaOther))
                    continue;

                if(Area.isConflicting(area, areaOther)){
                    conflicts.add(areaOther);
                }
            }
        }

        return conflicts;
    }

    /**
     * This method does not check if world of smallest and largest are same.
     * Also <b>check confliction with {@link #getConflictingAreas(SimpleLocation, SimpleLocation)} before</b> using this method.
     * @param name
     * @param smallest
     * @param largest
     * @return true on success; false if name already exist.
     */
    public boolean createArea(String name, SimpleLocation smallest, SimpleLocation largest) {
        Entry<Area, AreaTrigger> entry = getAreaForLocation(smallest);
        if(entry != null)
            return false;

        Area area = new Area(smallest, largest);
        File areaFolder = new File(folder, name);
        AreaTrigger trigger = new AreaTrigger(area, areaFolder, name);
        nameMapper.put(name, trigger);

        setupArea(trigger);

        return true;
    }

    /**
     * reset the area cache. Should be called for reloading.
     * @param trigger
     */
    protected void setupArea(AreaTrigger trigger) {
        Area area = trigger.area;

        Set<SimpleChunkLocation> sclocs = Area.getAllChunkLocations(area);
        for(SimpleChunkLocation scloc : sclocs){
            Map<Area, AreaTrigger> map = areaTriggers.get(scloc);
            if(map == null){
                map = new ConcurrentHashMap<>();
                areaTriggers.put(scloc, map);
            }

            map.put(area, trigger);
        }
    }

    /**
     * Try to get Area Trigger by given name
     * @param name
     * @return Area if found; null if nothing
     */
    public AreaTrigger getArea(String name) {
        return nameMapper.get(name);
    }

    /**
     * Try to remove Area Trigger by given name
     * @param name
     * @return false if can't find any Area Trigger with the name; true if deleted.
     */
    public boolean deleteArea(String name) {
        AreaTrigger trigger = nameMapper.get(name);
        if(trigger == null)
            return false;

        for(SimpleChunkLocation scloc : Area.getAllChunkLocations(trigger.area)){
            Map<Area, AreaTrigger> map = areaTriggers.get(scloc);
            map.remove(trigger.area);
        }

        deleteInfo(trigger);

        nameMapper.remove(name);
        return true;
    }

    /**
     * Try to get Area Trigger at given location
     * @param location
     * @return Area if found; null if nothing
     */
    public AreaTrigger getArea(SimpleLocation sloc) {
        Entry<Area, AreaTrigger> areaEntry = getAreaForLocation(sloc);
        if(areaEntry == null)
            return null;

        return areaEntry.getValue();
    }

    /**
     * Try to remove Area Trigger at given location.
     * @param location
     * @return false if no area found at location; true if deleted
     */
    public boolean deleteArea( SimpleLocation sloc) {
        Entry<Area, AreaTrigger> areaEntry = getAreaForLocation(sloc);
        if(areaEntry == null)
            return false;

        AreaTrigger trigger = areaEntry.getValue();

        for(SimpleChunkLocation scloc : Area.getAllChunkLocations(areaEntry.getKey())){
            Map<Area, AreaTrigger> map = areaTriggers.get(scloc);
            map.remove(areaEntry.getKey());
        }

        deleteInfo(nameMapper.remove(trigger.getTriggerName()));
        return true;
    }

    public enum EventType{
        ENTER, EXIT;
    }

    @Override
    protected void deleteInfo(Trigger trigger) {
        FileUtil.delete(new File(folder, trigger.getTriggerName()));
    }

    public AbstractAreaTriggerManager(TriggerReactor plugin, SelfReference ref, File tirggerFolder) {
        super(plugin, ref, tirggerFolder);
    }

    public static class AreaTrigger extends Trigger{
        final Area area;
        final File folder;

        private EnterTrigger enterTrigger;
        private ExitTrigger exitTrigger;

        public AreaTrigger(Area area, File folder, String name) {
            super(name, null, null);
            this.area = area;
            this.folder = folder;
        }

        public Area getArea() {
            return area;
        }

        //we don't need interpreter for area trigger but enter and exit trigger
        @Override
        protected Interpreter initInterpreter(Map<String, Object> scriptVars) {
            return null;
        }

        private EventType type = null;
        public void activate(Object e, Map<String, Object> scriptVars, EventType type){
            this.type = type;

            super.activate(e, scriptVars);
        }

        //intercept and pass interpretation to appropriate trigger
        @Override
        protected void startInterpretation(Object e, Map<String, Object> scriptVars, Interpreter interpreter, boolean sync) {
            switch(type){
            case ENTER:
                if(getEnterTrigger() != null)
                    getEnterTrigger().activate(e, scriptVars);
                break;
            case EXIT:
                if(getExitTrigger() != null)
                    getExitTrigger().activate(e, scriptVars);
                break;
            default:
                throw new RuntimeException("Unknown area event type "+type);
            }
        }

        @Override
        public Trigger clone() {
            return null;
        }

        public void setEnterTrigger(String script) throws TriggerInitFailedException{
            File triggerFile = getTriggerFile(folder, "Enter.trg");
            enterTrigger = new EnterTrigger(this, triggerFile, script);
        }

        public void setExitTrigger(String script) throws TriggerInitFailedException{
            File triggerFile = getTriggerFile(folder, "Exit.trg");
            exitTrigger = new ExitTrigger(this, triggerFile, script);
        }

        public EnterTrigger getEnterTrigger() {
            return enterTrigger;
        }

        public void setEnterTrigger(EnterTrigger enterTrigger) {
            this.enterTrigger = enterTrigger;
        }

        public ExitTrigger getExitTrigger() {
            return exitTrigger;
        }

        public void setExitTrigger(ExitTrigger exitTrigger) {
            this.exitTrigger = exitTrigger;
        }

        public static class EnterTrigger extends Trigger{
            private final AreaTrigger areaTrigger;

            public EnterTrigger(AreaTrigger areaTrigger, File file, String script) throws TriggerInitFailedException {
                super(areaTrigger.triggerName, file, script);
                this.areaTrigger = areaTrigger;

                init();
            }

            @Override
            public boolean isSync() {
                return areaTrigger.isSync();
            }

            @Override
            public void setSync(boolean sync) {
                areaTrigger.setSync(sync);
            }

            @Override
            public Trigger clone() {
                try {
                    return new EnterTrigger(areaTrigger, file, script);
                } catch (TriggerInitFailedException e) {
                    e.printStackTrace();
                }
                return null;
            }

        }

        public static class ExitTrigger extends Trigger{
            private final AreaTrigger areaTrigger;

            public ExitTrigger(AreaTrigger areaTrigger, File file, String script) throws TriggerInitFailedException {
                super(areaTrigger.getTriggerName(), file, script);
                this.areaTrigger = areaTrigger;

                init();
            }

            @Override
            public boolean isSync() {
                return areaTrigger.isSync();
            }

            @Override
            public void setSync(boolean sync) {
                areaTrigger.setSync(sync);
            }

            @Override
            public Trigger clone() {
                try {
                    return new ExitTrigger(areaTrigger, file, script);
                } catch (TriggerInitFailedException e) {
                    e.printStackTrace();
                }
                return null;
            }

        }
    }
}