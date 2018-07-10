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
package io.github.wysohn.triggerreactor.sponge.manager.trigger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractCustomTriggerManager;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

public class CustomTriggerManager extends AbstractCustomTriggerManager implements SpongeConfigurationFileIO{
    static final Map<String, Class<? extends Event>> EVENTS = new TreeMap<String, Class<? extends Event>>(String.CASE_INSENSITIVE_ORDER);
    static final List<Class<? extends Event>> BASEEVENTS = new ArrayList<Class<? extends Event>>();

    private static final Map<String, Class<? extends Event>> ABBREVIATIONS = new HashMap<String, Class<? extends Event>>(){{
        put("onJoin", ClientConnectionEvent.Join.class);
        put("onQuit", ClientConnectionEvent.Disconnect.class);
        //put("onPlayerDeath", DestructEntityEvent.Death.class); same as entity death event
        put("onInteract", InteractBlockEvent.class);
        put("onInteractEntity", InteractEntityEvent.class);
        put("onChat", MessageEvent.class);

        //put("onEntitySpawn", EntitySpawnEvent.class);
        put("onEntityDeath", DestructEntityEvent.Death.class);

        put("onBlockPlace", ChangeBlockEvent.Place.class);
        put("onBlockBreak", ChangeBlockEvent.Break.class);
    }};

    public CustomTriggerManager(TriggerReactor plugin) {
        super(plugin, new CommonFunctions(plugin), new File(plugin.getDataFolder(), "CustomTrigger"));

        try {
            initEvents();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reload() {
        Sponge.getEventManager().unregisterPluginListeners(plugin);
        super.reload();
    }

    private static final String basePackageName = "org.spongepowered.api.event";
    protected void initEvents() throws IOException{
        for(String clazzName : ReflectionUtil.getAllClasses(Sponge.class.getClassLoader(), basePackageName)){
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

        File triggerFile = getTriggerFile(folder, name+".trg");
        CustomTrigger trigger = new CustomTrigger(event, eventName, name, triggerFile, script);

        triggers.add(trigger);
        nameMap.put(name, trigger);

        return true;
    }

    @Override
    public Set<CustomTrigger> getTriggersForEvent(String eventName) throws ClassNotFoundException {
        Class<?> clazz = this.getEventFromName(eventName);

        return triggerMap.get(clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void registerEvent(TriggerReactor plugin, Class<?> clazz) {
        Sponge.getEventManager().registerListener(plugin, (Class<? extends Event>) clazz, new EventListener() {

            @Override
            public void handle(Event event) throws Exception {
                handleEvent(event);
            }

        });
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
}
