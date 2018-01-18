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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractCustomTriggerManager;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.tools.FileUtil;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

public class CustomTriggerManager extends AbstractCustomTriggerManager {
    static final Map<String, Class<? extends Event>> EVENTS = new TreeMap<String, Class<? extends Event>>(String.CASE_INSENSITIVE_ORDER);
    static final List<Class<? extends Event>> BASEEVENTS = new ArrayList<Class<? extends Event>>();

    private static final Map<String, Class<? extends Event>> ABBREVIATIONS = new HashMap<String, Class<? extends Event>>(){{
        put("onJoin", PlayerJoinEvent.class);
        put("onQuit", PlayerQuitEvent.class);
        put("onPlayerDeath", PlayerDeathEvent.class);
        put("onInteract", PlayerInteractEvent.class);
        put("onInteractEntity", PlayerInteractEntityEvent.class);
        put("onChat", AsyncPlayerChatEvent.class);

        //put("onEntitySpawn", EntitySpawnEvent.class);
        put("onEntityDeath", EntityDeathEvent.class);

        put("onBlockPlace", BlockPlaceEvent.class);
        put("onBlockBreak", BlockBreakEvent.class);
    }};

    final File folder;

    public CustomTriggerManager(TriggerReactor plugin) {
        super(plugin);

        try {
            initEvents();
        } catch (IOException e) {
            e.printStackTrace();
        }

        folder = new File(plugin.getDataFolder(), "CustomTrigger");
        if(!folder.exists()){
            folder.mkdirs();
        }

        reload();
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
                event = (Class<? extends Event>) getEventFromName(eventName);
            } catch (ClassNotFoundException e1) {
                plugin.getLogger().warning("Could not load "+file);
                plugin.getLogger().warning(e1.getMessage() + " does not exist.");
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
                } catch (TriggerInitFailedException e) {
                    e.printStackTrace();
                    continue;
                }
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    private static final String basePackageName = "org.bukkit.event";
    static final Listener listener = new Listener(){};
    protected void initEvents() throws IOException{
        for(String clazzName : ReflectionUtil.getAllClasses(Bukkit.class.getClassLoader(), basePackageName)){
            Class<?> test = null;
            try {
                test = Class.forName(clazzName);
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }

            if(!Event.class.isAssignableFrom(test))
                continue;

            Class<? extends Event> clazz = (Class<? extends Event>) test;
            if(clazz.equals(Event.class))
                continue;

            EVENTS.put(clazz.getSimpleName(), clazz);
        }
    }

    @Override
    protected void registerEvent(TriggerReactor plugin, Class<?> clazz) {
        try{
            Bukkit.getPluginManager().registerEvent((Class<? extends Event>) clazz, listener, EventPriority.HIGHEST, new EventExecutor(){
                @Override
                public void execute(Listener arg0, Event arg1) throws EventException {
                    handleEvent(arg1);
                }
            }, plugin.getMain());
        }catch(IllegalPluginAccessException e){
            //event with no handler list will throw this exception
            //which means it's a base event
            if(!BASEEVENTS.contains(BASEEVENTS))
                BASEEVENTS.add((Class<? extends Event>) clazz);
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
    @Override
    protected Class<?> getEventFromName(String name) throws ClassNotFoundException{
        Class<? extends Event> event;
        if(ABBREVIATIONS.containsKey(name)){
            event = ABBREVIATIONS.get(name);
        }else if(EVENTS.containsKey(name)){
            event = EVENTS.get(name);
        }else{
            event = (Class<? extends Event>) Class.forName(name);
        }

        return event;
    }

    @Override
    public void saveAll() {
        for(Entry<String, CustomTrigger> entry : nameMap.entrySet()){
            CustomTrigger trigger = entry.getValue();

            File file = new File(folder, trigger.getTriggerName());

            Utf8YamlConfiguration yamlFile = new Utf8YamlConfiguration();
            try {
                File yfile = new File(folder, trigger.getTriggerName()+".yml");
                if(yfile.exists())
                    yamlFile.load(yfile);
                yamlFile.set("Sync", trigger.isSync());
                yamlFile.set("Event", trigger.getEventName());
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

    @Override
    protected void handleEvent(Object e){
        Set<CustomTrigger> triggers = triggerMap.get(e.getClass());
        if(triggers == null)
            return;

        for(CustomTrigger trigger : triggers){
            Map<String, Object> vars = new HashMap<>();
            trigger.activate(e, vars);
        }
    }

    @Override
    protected void deleteInfo(Trigger trigger) {
        FileUtil.delete(new File(folder, trigger.getTriggerName()));
        FileUtil.delete(new File(folder, trigger.getTriggerName()+".yml"));
    }
}
