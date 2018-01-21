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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;
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

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractCustomTriggerManager;
import io.github.wysohn.triggerreactor.tools.FileUtil;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

public class CustomTriggerManager extends AbstractCustomTriggerManager implements BukkitTriggerManager{
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

    public CustomTriggerManager(TriggerReactor plugin) {
        super(plugin, new CommonFunctions(plugin), APISupport.getSharedVars(), new File(plugin.getDataFolder(), "CustomTrigger"));

        try {
            initEvents();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reload() {
        HandlerList.unregisterAll(listener);
        super.reload();
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
    public boolean createCustomTrigger(String eventName, String name, String script)
            throws ClassNotFoundException, TriggerInitFailedException {
        if (nameMap.containsKey(name))
            return false;

        Class<?> event = this.getEventFromName(eventName);

        Set<CustomTrigger> triggers = this.getTriggerSetForEvent(event);

        CustomTrigger trigger = new CustomTrigger(event, eventName, name, script);

        triggers.add(trigger);
        nameMap.put(name, trigger);

        return true;
    }

    @Override
    public Set<CustomTrigger> getTriggersForEvent(String eventName) throws ClassNotFoundException {
        Class<?> clazz = this.getEventFromName(eventName);

        return triggerMap.get(clazz);
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
