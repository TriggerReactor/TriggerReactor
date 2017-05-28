package io.github.wysohn.triggerreactor.manager.trigger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import io.github.wysohn.triggerreactor.core.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.TriggerManager;
import io.github.wysohn.triggerreactor.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class AreaTriggerManager extends TriggerManager {
    public final AreaSelectionHelper SELECTION_HELPER = new AreaSelectionHelper();

    private File folder;
    private Map<SimpleChunkLocation, Map<Area, AreaTrigger>> areaTriggers = new ConcurrentHashMap<>();
    private Map<String, AreaTrigger> nameMapper = new HashMap<>();

    public AreaTriggerManager(TriggerReactor plugin) {
        super(plugin);

        folder = new File(plugin.getDataFolder(), "AreaTrigger");
        if(!folder.exists()){
            folder.mkdirs();
        }

        Bukkit.getPluginManager().registerEvents(SELECTION_HELPER, plugin);

        reload();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void reload() {
        FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".yml");
            }
        };

        for(File file : folder.listFiles(filter)){
            Utf8YamlConfiguration yamlFile = new Utf8YamlConfiguration();
            try {
                yamlFile.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load Area Trigger "+file);
            }

            String name = file.getName().substring(0, file.getName().indexOf('.'));

            SimpleLocation smallest = getSmallestFromSection(yamlFile);
            SimpleLocation largest = getLargestFromSection(yamlFile);

            if(smallest == null || largest == null){
                plugin.getLogger().warning("Could not load Area Trigger"+file);
                plugin.getLogger().warning("Could not find Smallest: or Largest:");
                continue;
            }

            Area area = new Area(smallest, largest);
            AreaTrigger trigger = new AreaTrigger(area, name);
            nameMapper.put(name, trigger);

            File scriptFolder = new File(folder, name);
            if(!scriptFolder.exists()){
                scriptFolder.mkdirs();
            }

            String enterScript = null;
            try {
                enterScript = FileUtil.readFromFile(new File(scriptFolder, "Enter"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            String exitScript = null;
            try {
                exitScript = FileUtil.readFromFile(new File(scriptFolder, "Exit"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            Set<SimpleChunkLocation> set = getAllChunkLocations(area);
            for(SimpleChunkLocation scloc : set){
                Map<Area, AreaTrigger> triggerMap = areaTriggers.get(scloc);
                if(triggerMap == null){
                    triggerMap = new ConcurrentHashMap<>();
                    areaTriggers.put(scloc, triggerMap);
                }

                triggerMap.put(area, trigger);
            }

            try {
                if(enterScript != null){
                    trigger.setEnterTrigger(enterScript);
                }
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
            }

            try {
                if(exitScript != null){
                    trigger.setExitTrigger(exitScript);
                }
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
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
            Area area = trigger.area;

            File file = new File(folder, trigger.name+".yml");
            if(!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("Could not create "+file);
                }
            }

            Utf8YamlConfiguration yamlFile = new Utf8YamlConfiguration();
            setSmallestForSection(yamlFile, area.smallest);
            setLargestForSection(yamlFile, area.largest);
            try {
                yamlFile.save(file);
            } catch (IOException e1) {
                e1.printStackTrace();
                plugin.getLogger().warning("Could not save "+file);
            }

            File triggerFolder = new File(folder, trigger.name);
            if(!triggerFolder.exists()){
                triggerFolder.mkdirs();
            }

            if(trigger.enterTrigger != null){
                try {
                    FileUtil.writeToFile(new File(triggerFolder, "Enter"), trigger.enterTrigger.getScript());
                } catch (IOException e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("Could not save Area Trigger [Enter] "+trigger.name);
                }
            }

            if(trigger.exitTrigger != null){
                try {
                    FileUtil.writeToFile(new File(triggerFolder, "Exit"), trigger.exitTrigger.getScript());
                } catch (IOException e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("Could not save Area Trigger [Exit] "+trigger.name);
                }
            }
        }
    }

    private static final String SMALLEST = "Smallest";
    private static final String LARGEST = "Largest";
    private SimpleLocation getSmallestFromSection(ConfigurationSection section) {
        if(!section.contains(SMALLEST))
            return null;
        return SimpleLocation.valueOf(section.getString(SMALLEST, null));
    }

    private SimpleLocation getLargestFromSection(ConfigurationSection section) {
        if(!section.contains(LARGEST))
            return null;
        return SimpleLocation.valueOf(section.getString(LARGEST, null));
    }

    private void setSmallestForSection(ConfigurationSection section, SimpleLocation sloc) {
        section.set(SMALLEST, sloc.toString());
    }

    private void setLargestForSection(ConfigurationSection section, SimpleLocation sloc) {
        section.set(LARGEST, sloc.toString());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLocationChange(PlayerBlockLocationEvent e){
        Entry<Area, AreaTrigger> from = getAreaForLocation(e.getFrom());
        Entry<Area, AreaTrigger> to = getAreaForLocation(e.getTo());

        if(from == null && to == null)
            return;

        if(from != null && to != null && from.getKey().equals(to.getKey()))
            return;

        Map<String, Object> varMap = new HashMap<>();
        insertPlayerVariables(e.getPlayer(), varMap);
        varMap.put("from", e.getFrom());
        varMap.put("to", e.getTo());

        if(from != null){
            from.getValue().activate(e, varMap, EventType.EXIT);
        }

        if(to != null){
            to.getValue().activate(e, varMap, EventType.ENTER);
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

    protected Map.Entry<Area, AreaTrigger> getAreaForLocation(SimpleLocation sloc){
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
    public Set<Area> getConflictingAreas(Area area){
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
    public boolean createArea(String name, SimpleLocation smallest, SimpleLocation largest){
        Entry<Area, AreaTrigger> entry = getAreaForLocation(smallest);
        if(entry != null)
            return false;

        Area area = new Area(smallest, largest);
        AreaTrigger trigger = new AreaTrigger(area, name);
        nameMapper.put(name, trigger);

        Set<SimpleChunkLocation> sclocs = getAllChunkLocations(area);
        for(SimpleChunkLocation scloc : sclocs){
            Map<Area, AreaTrigger> map = areaTriggers.get(scloc);
            if(map == null){
                map = new ConcurrentHashMap<>();
                areaTriggers.put(scloc, map);
            }

            map.put(area, trigger);
        }

        return true;
    }

    /**
     * Try to get Area Trigger by given name
     * @param name
     * @return Area if found; null if nothing
     */
    public AreaTrigger getArea(String name){
        return nameMapper.get(name);
    }

    /**
     * Try to get Area Trigger at given location
     * @param location
     * @return Area if found; null if nothing
     */
    public AreaTrigger getArea(Location location){
        SimpleLocation sloc = new SimpleLocation(location);
        Entry<Area, AreaTrigger> areaEntry = getAreaForLocation(sloc);
        if(areaEntry == null)
            return null;

        return areaEntry.getValue();
    }

    /**
     * Try to remove Area Trigger by given name
     * @param name
     * @return false if can't find any Area Trigger with the name; true if deleted.
     */
    public boolean deleteArea(String name){
        AreaTrigger trigger = nameMapper.get(name);
        if(trigger == null)
            return false;

        for(SimpleChunkLocation scloc : getAllChunkLocations(trigger.area)){
            Map<Area, AreaTrigger> map = areaTriggers.get(scloc);
            map.remove(trigger.area);
        }

        File areafile = new File(folder, trigger.name+".yml");
        FileUtil.delete(areafile);
        File areafolder = new File(folder, trigger.name);
        FileUtil.delete(areafolder);

        nameMapper.remove(name);
        return true;
    }

    /**
     * Try to remove Area Trigger at given location.
     * @param location
     * @return false if no area found at location; true if deleted
     */
    public boolean deleteArea(Location location){
        SimpleLocation sloc = new SimpleLocation(location);
        Entry<Area, AreaTrigger> areaEntry = getAreaForLocation(sloc);
        if(areaEntry == null)
            return false;

        AreaTrigger trigger = areaEntry.getValue();

        for(SimpleChunkLocation scloc : getAllChunkLocations(areaEntry.getKey())){
            Map<Area, AreaTrigger> map = areaTriggers.get(scloc);
            map.remove(areaEntry.getKey());
        }

        File areafile = new File(folder, trigger.name+".yml");
        FileUtil.delete(areafile);
        File areafolder = new File(folder, trigger.name);
        FileUtil.delete(areafolder);

        nameMapper.remove(trigger.name);
        return true;
    }

    public class AreaSelectionHelper implements Listener{
        private final Set<UUID> selecting = new HashSet<>();
        private final Map<UUID, SimpleLocation> leftPosition = new HashMap<>();
        private final Map<UUID, SimpleLocation> rightPosition = new HashMap<>();

        @EventHandler
        public void onQuit(PlayerQuitEvent e){
            resetSelections(e.getPlayer());
        }

        @EventHandler
        public void onInteract(PlayerInteractEvent e){
            Player player = e.getPlayer();
            UUID uuid = player.getUniqueId();

            if(!selecting.contains(uuid))
                return;

            e.setCancelled(true);

            if(e.getHand() != EquipmentSlot.HAND)
                return;

            SimpleLocation sloc = new SimpleLocation(e.getClickedBlock().getLocation());

            if(e.getAction() == Action.LEFT_CLICK_BLOCK){
                leftPosition.put(uuid, sloc);
            }else if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
                rightPosition.put(uuid, sloc);
            }

            SimpleLocation left = leftPosition.get(uuid);
            SimpleLocation right = rightPosition.get(uuid);
            if(left != null && right != null){
                if(!left.getWorld().equals(right.getWorld())){
                    player.sendMessage(ChatColor.RED+"Positions have different world name.");
                    return;
                }

                SimpleLocation smallest = getSmallest(left, right);
                SimpleLocation largest = getLargest(left, right);

                player.sendMessage(ChatColor.LIGHT_PURPLE+"Smallest: "+smallest+" , Largest: "+largest);
            } else if (left != null){
                player.sendMessage(ChatColor.GREEN+"Left ready");
            } else if (right != null){
                player.sendMessage(ChatColor.GREEN+"Right ready");
            }
        }

        private SimpleLocation getSmallest(SimpleLocation left, SimpleLocation right) {
            return new SimpleLocation(left.getWorld(),
                    Math.min(left.getX(), right.getX()),
                    Math.min(left.getY(), right.getY()),
                    Math.min(left.getZ(), right.getZ()));
        }

        private SimpleLocation getLargest(SimpleLocation left, SimpleLocation right) {
            return new SimpleLocation(right.getWorld(),
                    Math.max(left.getX(), right.getX()),
                    Math.max(left.getY(), right.getY()),
                    Math.max(left.getZ(), right.getZ()));
        }

        /**
         *
         * @param player
         * @return true if on; false if off
         */
        public boolean toggleSelection(Player player){
            if(selecting.contains(player.getUniqueId())){
                selecting.remove(player.getUniqueId());
                resetSelections(player);
                return false;
            }else{
                selecting.add(player.getUniqueId());
                return true;
            }
        }

        public void resetSelections(Player player){
            selecting.remove(player.getUniqueId());
            leftPosition.remove(player.getUniqueId());
            rightPosition.remove(player.getUniqueId());
        }

        /**
         *
         * @param player
         * @return null if invalid selection; Area if done (this Area's name is always null)
         */
        public Area getSelection(Player player){
            UUID uuid = player.getUniqueId();

            SimpleLocation left = leftPosition.get(uuid);
            SimpleLocation right = rightPosition.get(uuid);

            if(left != null && right != null){
                if(!left.getWorld().equals(right.getWorld())){
                    player.sendMessage(ChatColor.RED+"Positions have different world name.");
                    return null;
                }

                SimpleLocation smallest = getSmallest(left, right);
                SimpleLocation largest = getLargest(left, right);

                return new Area(smallest, largest);
            } else {
                return null;
            }
        }
    }

    public class AreaTrigger extends Trigger{
        final Area area;
        final String name;

        EnterTrigger enterTrigger;
        ExitTrigger exitTrigger;

        public AreaTrigger(Area area, String name) {
            super(null);
            this.area = area;
            this.name = name;
        }

        //we don't need interpreter for area trigger but enter and exit trigger
        @Override
        protected Interpreter initInterpreter(Map<String, Object> scriptVars) {
            return null;
        }

        private EventType type = null;
        public void activate(PlayerBlockLocationEvent e, Map<String, Object> scriptVars, EventType type){
            this.type = type;

            super.activate(e, scriptVars);
        }

        //intercept and pass interpretation to appropriate trigger
        @Override
        protected void startInterpretation(Event e, Map<String, Object> scriptVars, Interpreter interpreter) {
            switch(type){
            case ENTER:
                if(enterTrigger != null)
                    enterTrigger.activate(e, scriptVars);
                break;
            case EXIT:
                if(exitTrigger != null)
                    exitTrigger.activate(e, scriptVars);
                break;
            default:
                throw new RuntimeException("Unknown area event type "+type);
            }
        }

        @Override
        public Trigger clone() {
            return null;
        }

        public void setEnterTrigger(String script) throws IOException, LexerException, ParserException{
            enterTrigger = new EnterTrigger(script);
        }

        public void setExitTrigger(String script) throws IOException, LexerException, ParserException{
            exitTrigger = new ExitTrigger(script);
        }

        private class EnterTrigger extends Trigger{

            public EnterTrigger(String script) throws IOException, LexerException, ParserException {
                super(script);

                init();
            }

            @Override
            public Trigger clone() {
                try {
                    return new EnterTrigger(script);
                } catch (IOException | LexerException | ParserException e) {
                    e.printStackTrace();
                }
                return null;
            }

        }

        private class ExitTrigger extends Trigger{

            public ExitTrigger(String script) throws IOException, LexerException, ParserException {
                super(script);

                init();
            }

            @Override
            public Trigger clone() {
                try {
                    return new ExitTrigger(script);
                } catch (IOException | LexerException | ParserException e) {
                    e.printStackTrace();
                }
                return null;
            }

        }
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
}
