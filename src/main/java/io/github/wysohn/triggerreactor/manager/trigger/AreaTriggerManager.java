package io.github.wysohn.triggerreactor.manager.trigger;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import io.github.wysohn.triggerreactor.core.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.TriggerManager;
import io.github.wysohn.triggerreactor.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;

public class AreaTriggerManager extends TriggerManager {
    private File folder;
    private Map<SimpleChunkLocation, Map<Area, AreaTrigger>> areaTriggers = new ConcurrentHashMap<>();

    public AreaTriggerManager(TriggerReactor plugin) {
        super(plugin);

        folder = new File(plugin.getDataFolder(), "AreaTrigger");
        if(!folder.exists()){
            folder.mkdirs();
        }

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
                plugin.getLogger().warning("Could not load Area Trigger"+file);
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

            File scriptFolder = new File(folder, name);
            if(!scriptFolder.exists()){
                scriptFolder.mkdirs();
            }

            String enterScript = readFile(new File(scriptFolder, "Enter"));
            String exitScript = readFile(new File(scriptFolder, "Exit"));

            Set<SimpleChunkLocation> set = getAllChunkLocations(area);
            for(SimpleChunkLocation scloc : set){
                Map<Area, AreaTrigger> triggerMap = areaTriggers.get(scloc);
                if(triggerMap == null){
                    triggerMap = new ConcurrentHashMap<>();
                    areaTriggers.put(scloc, triggerMap);
                }

                AreaTrigger trigger = new AreaTrigger();

                try {
                    trigger.enterTrigger = enterScript == null ?
                            null : trigger.createEnterTrigger(enterScript);
                } catch (IOException | LexerException | ParserException e) {
                    e.printStackTrace();
                }

                try {
                    trigger.exitTrigger = exitScript == null ?
                            null : trigger.createExitTrigger(exitScript);
                } catch (IOException | LexerException | ParserException e) {
                    e.printStackTrace();
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

    private void setSmallestForSection(ConfigurationSection section, SimpleLocation sloc){
       section.set(SMALLEST, sloc.toString());
    }

    private void setLargestForSection(ConfigurationSection section, SimpleLocation sloc){
        section.set(LARGEST, sloc.toString());
    }

    private String readFile(File file) {
        if(!file.exists())
            return null;

        StringBuilder builder = new StringBuilder();
        try(FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8")){
            int read = -1;
            while((read = isr.read()) != -1){
                builder.append((char) read);
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

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

    public class AreaTrigger extends Trigger{
        EnterTrigger enterTrigger;
        ExitTrigger exitTrigger;

        public AreaTrigger() {
            super(null);
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

        public EnterTrigger createEnterTrigger(String script) throws IOException, LexerException, ParserException{
            return new EnterTrigger(script);
        }

        public ExitTrigger createExitTrigger(String script) throws IOException, LexerException, ParserException{
            return new ExitTrigger(script);
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

    private static class Area{
        final SimpleLocation smallest;
        final SimpleLocation largest;
        public Area(SimpleLocation smallest, SimpleLocation largest) {
            this.smallest = smallest;
            this.largest = largest;
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
    }
}
