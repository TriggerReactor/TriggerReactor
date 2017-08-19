package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.TriggerManager;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;

public abstract class AbstractAreaTriggerManager extends TriggerManager {

    protected Map<SimpleChunkLocation, Map<Area, AreaTrigger>> areaTriggers = new ConcurrentHashMap<>();
    protected Map<String, AreaTrigger> nameMapper = new HashMap<>();

    public static class AreaTrigger extends Trigger{
        final Area area;

        private EnterTrigger enterTrigger;
        private ExitTrigger exitTrigger;

        public AreaTrigger(Area area, String name) {
            super(name, null);
            this.area = area;
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
            enterTrigger = new EnterTrigger(this, script);
        }

        public void setExitTrigger(String script) throws TriggerInitFailedException{
            exitTrigger = new ExitTrigger(this, script);
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

            public EnterTrigger(AreaTrigger areaTrigger, String script) throws TriggerInitFailedException {
                super(areaTrigger.triggerName, script);
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
                    return new EnterTrigger(areaTrigger, script);
                } catch (TriggerInitFailedException e) {
                    e.printStackTrace();
                }
                return null;
            }

        }

        public static class ExitTrigger extends Trigger{
            private final AreaTrigger areaTrigger;

            public ExitTrigger(AreaTrigger areaTrigger, String script) throws TriggerInitFailedException {
                super(areaTrigger.getTriggerName(), script);
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
                    return new ExitTrigger(areaTrigger, script);
                } catch (TriggerInitFailedException e) {
                    e.printStackTrace();
                }
                return null;
            }

        }
    }

    protected Set<SimpleChunkLocation> getAllChunkLocations(Area area) {
        SimpleLocation smallest = area.smallest;
        SimpleLocation largest = area.largest;

        Set<SimpleChunkLocation> set = new HashSet<>();

        for(int i = smallest.getX(); i <= largest.getX(); i += 16){
            for(int j = smallest.getZ(); j <= largest.getZ(); j += 16){
                int chunkX = i >> 4;
                int chunkZ = j >> 4;

                set.add(new SimpleChunkLocation(smallest.getWorld(), chunkX, chunkZ));
            }
        }

        return set;
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

        Set<SimpleChunkLocation> sclocs = getAllChunkLocations(area);
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
        AreaTrigger trigger = new AreaTrigger(area, name);
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

        Set<SimpleChunkLocation> sclocs = getAllChunkLocations(area);
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

        for(SimpleChunkLocation scloc : getAllChunkLocations(trigger.area)){
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

        for(SimpleChunkLocation scloc : getAllChunkLocations(areaEntry.getKey())){
            Map<Area, AreaTrigger> map = areaTriggers.get(scloc);
            map.remove(areaEntry.getKey());
        }

        deleteInfo(nameMapper.remove(trigger.getTriggerName()));
        return true;
    }

    public enum EventType{
        ENTER, EXIT;
    }

    public static class Area{
        final SimpleLocation smallest;
        final SimpleLocation largest;

        public Area(SimpleLocation smallest, SimpleLocation largest) {
            this.smallest = smallest;
            this.largest = largest;
        }

        public SimpleLocation getSmallest() {
            return smallest;
        }

        public SimpleLocation getLargest() {
            return largest;
        }

        public boolean isInThisArea(SimpleLocation sloc){
            if(smallest.getX() <= sloc.getX() && sloc.getX() <= largest.getX()
                    && smallest.getY() <= sloc.getY() && sloc.getY() <= largest.getY()
                    && smallest.getZ() <= sloc.getZ() && sloc.getZ() <= largest.getZ())
                return true;
            return false;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((largest == null) ? 0 : largest.hashCode());
            result = prime * result + ((smallest == null) ? 0 : smallest.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Area other = (Area) obj;
            if (largest == null) {
                if (other.largest != null)
                    return false;
            } else if (!largest.equals(other.largest))
                return false;
            if (smallest == null) {
                if (other.smallest != null)
                    return false;
            } else if (!smallest.equals(other.smallest))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "[smallest=" + smallest + ", largest=" + largest + "]";
        }
        public static boolean isConflicting(Area area1, Area area2){
            if(!area1.smallest.getWorld().equals(area2.smallest.getWorld()))
                return false;

            int xs1 = area1.smallest.getX(), xs2 = area2.smallest.getX();
            int ys1 = area1.smallest.getY(), ys2 = area2.smallest.getY();
            int zs1 = area1.smallest.getZ(), zs2 = area2.smallest.getZ();

            int xl1 = area1.largest.getX(), xl2 = area2.largest.getX();
            int yl1 = area1.largest.getY(), yl2 = area2.largest.getY();
            int zl1 = area1.largest.getZ(), zl2 = area2.largest.getZ();

            boolean xConflict = false;
            boolean zConflict = false;
            //compare x
            if(Math.abs(xl1 - xs1) > Math.abs(xl2 - xs2)){//sec1 is longer so check if one of the points in sec2 within the range
                if((xs1 <= xs2 && xs2 <= xl1) || (xs1 <= xl2 && xl2 <= xl1)){
                    xConflict = true;
                }
            }else{//sec2 is longer so check if one of the points in sec1 within the range
                if((xs2 <= xs1 && xs1 <= xl2) || (xs2 <= xl1 && xl1 <= xl2)){
                    xConflict = true;
                }
            }

            //compare z
            if(Math.abs(zl1 - zs1) > Math.abs(zl2 - zs2)){//sec1 is longer so check if one of the points in sec2 within the range
                if((zs1 <= zs2 && zs2 <= zl1) || (zs1 <= zl2 && zl2 <= zl1)){
                    zConflict = true;
                }
            }else{//sec2 is longer so check if one of the points in sec1 within the range
                if((zs2 <= zs1 && zs1 <= zl2) || (zs2 <= zl1 && zl1 <= zl2)){
                    zConflict = true;
                }
            }

            //compare y
            if(xConflict && zConflict){
                if(ys1 > ys2){//sec1 on sec2
                    int yFloor = ys1;
                    int yCeiling = yl2;

                    if(yFloor - yCeiling <= 0)
                        return true;
                }else if(yl1 < yl2){//sec2 on sec1
                    int yFloor = ys2;
                    int yCeiling = yl1;

                    if(yFloor - yCeiling <= 0)
                        return true;
                }else{//sec2 bot == sec1 bot
                    return true;
                }
            }

            return false;
        }
    }

    public AbstractAreaTriggerManager(TriggerReactor plugin) {
        super(plugin);
    }

}