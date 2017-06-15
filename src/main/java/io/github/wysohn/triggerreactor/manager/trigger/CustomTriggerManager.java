/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
package io.github.wysohn.triggerreactor.manager.trigger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.TriggerManager;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class CustomTriggerManager extends TriggerManager {
    static final Map<String, Class<? extends Event>> EVENTS = new TreeMap<String, Class<? extends Event>>(String.CASE_INSENSITIVE_ORDER);
    static final List<Class<? extends Event>> BASEEVENTS = new ArrayList<Class<? extends Event>>();

    private static final Map<String, Class<? extends Event>> ABBREVIATIONS = new HashMap<String, Class<? extends Event>>(){{
        put("onJoin", PlayerJoinEvent.class);
        put("onQuit", PlayerQuitEvent.class);
        put("onPlayerDeath", PlayerDeathEvent.class);
        put("onInteract", PlayerInteractEvent.class);
        put("onInteractEntity", PlayerInteractEntityEvent.class);
        put("onChat", AsyncPlayerChatEvent.class);

        put("onEntitySpawn", EntitySpawnEvent.class);
        put("onEntityDeath", EntityDeathEvent.class);

        put("onBlockPlace", BlockPlaceEvent.class);
        put("onBlockBreak", BlockBreakEvent.class);
    }};

    private final File folder;

    private final Map<Class<? extends Event>, Set<CustomTrigger>> triggerMap = new ConcurrentHashMap<>();
    private final Map<String, CustomTrigger> nameMap = new ConcurrentHashMap<>();

    public CustomTriggerManager(TriggerReactor plugin) {
        super(plugin);

        try {
            initEvents(plugin);
        } catch (IOException e) {
            e.printStackTrace();
        }

        folder = new File(plugin.getDataFolder(), "CustomTrigger");
        if(!folder.exists()){
            folder.mkdirs();
        }

        reload();
    }

    private static final String basePackageName = "org.bukkit.event";
    private static final Listener listener = new Listener(){};
    private void initEvents(Plugin plugin) throws IOException{
        //thanks google and spigot!
        ClassPath cp = ClassPath.from(Bukkit.class.getClassLoader());
        for (ClassInfo info : cp.getTopLevelClassesRecursive(basePackageName)) {
            Class<?> test = null;
            try {
                test = Class.forName(info.getName());
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }

            if(!Event.class.isAssignableFrom(test))
                continue;

            Class<? extends Event> clazz = (Class<? extends Event>) test;
            if(clazz.equals(Event.class))
                continue;

            EVENTS.put(info.getSimpleName(), clazz);
        }
    }

    @Override
    public void reload() {
        FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".yml");
            }
        };

        triggerMap.clear();
        HandlerList.unregisterAll(listener);

        for(File file : folder.listFiles(filter)){
            if(!file.isFile())
                continue;

            Utf8YamlConfiguration yamlFile = new Utf8YamlConfiguration();
            try {
                yamlFile.load(file);
            } catch (IOException | InvalidConfigurationException e1) {
                e1.printStackTrace();
                continue;
            }

            String eventName = yamlFile.getString("Event", null);
            if(eventName == null){
                plugin.getLogger().warning("Could not find Event: for "+file);
                continue;
            }

            Class<? extends Event> event = null;
            try {
                event = getEventFromName(eventName);
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
                plugin.getLogger().warning("Could not load "+file);
                continue;
            }

            boolean isSync = yamlFile.getBoolean("Sync", false);

            String fileName = file.getName().substring(0, file.getName().indexOf('.'));
            File codeFile = new File(folder, fileName);

            try {
                String read = FileUtil.readFromFile(codeFile);

                Set<CustomTrigger> triggers = getTriggerSetForEvent(event);

                try {
                    CustomTrigger trigger = new CustomTrigger(event, eventName, fileName, read);
                    trigger.setSync(isSync);

                    triggers.add(trigger);
                    nameMap.put(fileName, trigger);
                } catch (LexerException | ParserException e) {
                    e.printStackTrace();
                    continue;
                }
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    /**
     * Try to get set of Triggers associated with the event. It creates and puts new empty Set
     *  if couldn't find existing one already, and register it so can handle the event.
     * @param event any event that extends Event (and HandlerList of course)
     * @return Set of CustomTriggers associated with the event
     */
    private Set<CustomTrigger> getTriggerSetForEvent(Class<? extends Event> event) {
        Set<CustomTrigger> triggers = triggerMap.get(event);
        if(triggers == null){
            //this will allow TriggerReactor to hook events from other plugins as well.
            registerEvent(plugin, event);

            triggers = new HashSet<>();
            triggerMap.put(event, triggers);
        }
        return triggers;
    }

    private void registerEvent(Plugin plugin, Class<? extends Event> clazz) {
        try{
            plugin.getServer().getPluginManager().registerEvent(clazz, listener, EventPriority.HIGHEST, new EventExecutor(){
                @Override
                public void execute(Listener arg0, Event arg1) throws EventException {
                    handleEvent(arg1);
                }
            }, plugin);
        }catch(IllegalPluginAccessException e){
            //event with no handler list will throw this exception
            //which means it's a base event
            if(!BASEEVENTS.contains(BASEEVENTS))
                BASEEVENTS.add(clazz);
        }
    }

    /**
     * First it tries to return Event in ABBREVIATIONS if such name exists; if
     * not exists, then it will try to find event by event name; if it fails
     * too, will look for full class name.
     *  ex) 1. onJoin -> 2. PlayerJoinEvent -> 3. org.bukkit.event.player.PlayerJoinEvent
     *
     * @param name name of event to search
     * @return the event class
     * @throws ClassNotFoundException
     *             throws if search fails or the result event is
     *             a event that cannot receive events.
     */
    protected Class<? extends Event> getEventFromName(String name) throws ClassNotFoundException{
        Class<? extends Event> event;
        if(ABBREVIATIONS.containsKey(name)){
            event = ABBREVIATIONS.get(name);
        }else if(EVENTS.containsKey(name)){
            event = EVENTS.get(name);
        }else{
            event = (Class<? extends Event>) Class.forName(name);
        }

        try {
            event.getDeclaredMethod("getHandlerList");
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ClassNotFoundException(event+" is a base event so cannot receive events!");
        }

        return event;
    }

    @Override
    public void saveAll() {
        for(Entry<String, CustomTrigger> entry : nameMap.entrySet()){
            CustomTrigger trigger = entry.getValue();

            File file = new File(folder, trigger.name);

            Utf8YamlConfiguration yamlFile = new Utf8YamlConfiguration();
            try {
                File yfile = new File(folder, trigger.name+".yml");
                if(yfile.exists())
                    yamlFile.load(yfile);
                yamlFile.set("Sync", trigger.isSync());
                yamlFile.set("Event", trigger.eventName);
                yamlFile.save(yfile);
            } catch (IOException | InvalidConfigurationException e1) {
                e1.printStackTrace();
                continue;
            }

            try {
                FileUtil.writeToFile(file, trigger.getScript());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void handleEvent(Event e){
        Set<CustomTrigger> triggers = triggerMap.get(e.getClass());
        if(triggers == null)
            return;

        for(CustomTrigger trigger : triggers){
            Map<String, Object> vars = new HashMap<>();
            trigger.activate(e, vars);
        }
    }

    /**
     * Create a new CustomTrigger.
     *
     * @param eventName
     *            the class name of the Event that this Custom Trigger will
     *            handle.
     * @param name name of trigger (unique)
     * @param script the script
     * @return true if created; false if trigger with the 'name' already exists.
     * @throws ClassNotFoundException
     *             throws if className is not in abbreviation list, not a valid
     *             class name, or the specified event is not a valid event to handle.
     * @throws ParserException
     * @throws LexerException
     * @throws IOException
     */
    public boolean createCustomTrigger(String eventName, String name, String script) throws ClassNotFoundException,
    IOException, LexerException, ParserException {
        if(nameMap.containsKey(name))
            return false;

        Class<? extends Event> event = this.getEventFromName(eventName);

        Set<CustomTrigger> triggers = this.getTriggerSetForEvent(event);

        CustomTrigger trigger = new CustomTrigger(event, eventName, name, script);

        triggers.add(trigger);
        nameMap.put(name, trigger);

        return true;
    }

    /**
     * Find and return Custom Trigger with the 'name'
     * @param name
     * @return null if no such trigger with that name; CustomTrigger if found
     */
    public CustomTrigger getTriggerForName(String name){
        return nameMap.get(name);
    }

    /**
     * get set of triggers associated with 'className' Event
     * @param eventName the abbreviation, simple event name, or full event name. See {@link #getEventFromName(String)}
     * @return set of triggers; null if nothing is registered with the 'className'
     * @throws ClassNotFoundException See {@link #getEventFromName(String)}
     */
    public Set<CustomTrigger> getTriggersForEvent(String eventName) throws ClassNotFoundException{
        Class<? extends Event> clazz = this.getEventFromName(eventName);

        return triggerMap.get(clazz);
    }

    /**
     * Delete the Custom Trigger with 'name'
     * @param name
     * @return false if no such trigger exists with 'name'; true if deleted
     */
    public boolean removeTriggerForName(String name){
        if(!nameMap.containsKey(name))
            return false;

        CustomTrigger trigger = nameMap.remove(name);
        Set<CustomTrigger> triggers = triggerMap.get(trigger.event);
        if(triggers != null){
            triggers.remove(trigger);
        }

        FileUtil.delete(new File(folder, name));
        FileUtil.delete(new File(folder, name+".yml"));

        return true;
    }

    public class CustomTrigger extends Trigger{
        final Class<? extends Event> event;
        final String eventName;
        final String name;

        /**
         *
         * @param event
         * @param name
         * @param script
         * @throws IOException {@link Trigger#init()}
         * @throws LexerException {@link Trigger#init()}
         * @throws ParserException {@link Trigger#init()}
         */
        public CustomTrigger(Class<? extends Event> event, String eventName, String name, String script) throws IOException, LexerException, ParserException {
            super(script);
            this.event = event;
            this.eventName = eventName;
            this.name = name;

            init();
        }

        @Override
        public Trigger clone() {
            try {
                return new CustomTrigger(event, eventName, name, this.getScript());
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
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
            CustomTrigger other = (CustomTrigger) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }
    }
}
