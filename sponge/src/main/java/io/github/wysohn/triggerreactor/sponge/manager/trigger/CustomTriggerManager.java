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

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.AbstractCustomTriggerManager;
import io.github.wysohn.triggerreactor.sponge.manager.event.TriggerReactorStartEvent;
import io.github.wysohn.triggerreactor.sponge.manager.event.TriggerReactorStopEvent;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class CustomTriggerManager extends AbstractCustomTriggerManager {
    private Map<EventHook, EventListener> registeredListeners = new HashMap<>();

    public CustomTriggerManager(TriggerReactorCore plugin) {
        super(plugin, new File(plugin.getDataFolder(), "CustomTrigger"), new EventRegistry() {
            @Override
            public boolean eventExist(String eventStr) {
                try {
                    return getEvent(eventStr) != null;
                } catch (ClassNotFoundException e) {
                    return false;
                }
            }

            @Override
            public Class<?> getEvent(String eventStr) throws ClassNotFoundException {
                Class<? extends Event> event;
                if (ABBREVIATIONS.containsKey(eventStr)) {
                    event = ABBREVIATIONS.get(eventStr);
                } else if (EVENTS.containsKey(eventStr)) {
                    event = EVENTS.get(eventStr);
                } else {
                    event = (Class<? extends Event>) Class.forName(eventStr);
                }

                return event;
            }
        });

        try {
            initEvents();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void initEvents() throws IOException {
        for (String clazzName : ReflectionUtil.getAllClasses(Sponge.class.getClassLoader(), basePackageName)) {
            Class<?> test = null;
            try {
                test = Class.forName(clazzName);
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }

            if (!Event.class.isAssignableFrom(test))
                continue;

            Class<? extends Event> clazz = (Class<? extends Event>) test;
            if (clazz.equals(Event.class))
                continue;

            EVENTS.put(clazz.getSimpleName(), clazz);
        }
    }

    public Collection<String> getAbbreviations() {
        return ABBREVIATIONS.keySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void registerEvent(TriggerReactorCore plugin, Class<?> clazz, EventHook eventHook) {
        EventListener listener = new EventListener() {

            @Override
            public void handle(Event event) throws Exception {
                eventHook.onEvent(event);
            }

        };
        registeredListeners.put(eventHook, listener);
        Sponge.getEventManager().registerListener(plugin, (Class<? extends Event>) clazz, listener);
    }

    @Override
    public void reload() {
        for (Entry<EventHook, EventListener> entry : registeredListeners.entrySet()) {
            Sponge.getEventManager().unregisterListeners(entry.getValue());
        }
        registeredListeners.clear();
        super.reload();
    }

    @Override
    protected void unregisterEvent(TriggerReactorCore plugin, EventHook eventHook) {
        EventListener listener = registeredListeners.remove(eventHook);
        if (listener != null) {
            Sponge.getEventManager().unregisterListeners(listener);
        }
    }
    static final Map<String, Class<? extends Event>> EVENTS = new TreeMap<String, Class<? extends Event>>(
            String.CASE_INSENSITIVE_ORDER);
    static final List<Class<? extends Event>> BASEEVENTS = new ArrayList<Class<? extends Event>>();
    @SuppressWarnings("serial")
    private static final Map<String, Class<? extends Event>> ABBREVIATIONS = new HashMap<String, Class<?
            extends Event>>() {{
        put("onJoin", ClientConnectionEvent.Join.class);
        put("onQuit", ClientConnectionEvent.Disconnect.class);
        //put("onPlayerDeath", DestructEntityEvent.Death.class); same as entity death event
        put("onInteract", InteractBlockEvent.class);
        put("onInteractEntity", InteractEntityEvent.class);
        put("onChat", MessageEvent.class);
        put("onCommand", SendCommandEvent.class);

        //put("onEntitySpawn", EntitySpawnEvent.class);
        put("onEntityDeath", DestructEntityEvent.Death.class);

        put("onBlockPlace", ChangeBlockEvent.Place.class);
        put("onBlockBreak", ChangeBlockEvent.Break.class);

        put("onStart", TriggerReactorStartEvent.class);
        put("onStop", TriggerReactorStopEvent.class);
    }};
    private static final String basePackageName = "org.spongepowered.api.event";
}
