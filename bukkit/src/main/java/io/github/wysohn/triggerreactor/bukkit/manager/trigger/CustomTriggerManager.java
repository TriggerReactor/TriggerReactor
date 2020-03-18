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

import io.github.wysohn.triggerreactor.bukkit.manager.event.TriggerReactorStartEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.event.TriggerReactorStopEvent;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractCustomTriggerManager;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class CustomTriggerManager extends AbstractCustomTriggerManager implements BukkitTriggerManager {
    static final Map<String, Class<? extends Event>> EVENTS = new TreeMap<String, Class<? extends Event>>(String.CASE_INSENSITIVE_ORDER);
    static final List<Class<? extends Event>> BASEEVENTS = new ArrayList<Class<? extends Event>>();

    @SuppressWarnings("serial")
    private static final Map<String, Class<? extends Event>> ABBREVIATIONS = new HashMap<String, Class<? extends Event>>() {{
        put("onJoin", PlayerJoinEvent.class);
        put("onQuit", PlayerQuitEvent.class);
        put("onPlayerDeath", PlayerDeathEvent.class);
        put("onInteract", PlayerInteractEvent.class);
        put("onInteractEntity", PlayerInteractEntityEvent.class);
        put("onChat", AsyncPlayerChatEvent.class);
        put("onCommand", PlayerCommandPreprocessEvent.class);

        //put("onEntitySpawn", EntitySpawnEvent.class);
        put("onEntityDeath", EntityDeathEvent.class);

        put("onBlockPlace", BlockPlaceEvent.class);
        put("onBlockMultiPlace", BlockMultiPlaceEvent.class);
        put("onBlockBreak", BlockBreakEvent.class);

        put("onStart", TriggerReactorStartEvent.class);
        put("onStop", TriggerReactorStopEvent.class);
    }};

    public CustomTriggerManager(TriggerReactorCore plugin) {
        super(plugin, new File(plugin.getDataFolder(), "CustomTrigger"));

        try {
            initEvents();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reload() {
        for (Entry<EventHook, Listener> entry : registeredListerners.entrySet()) {
            HandlerList.unregisterAll(entry.getValue());
        }
        registeredListerners.clear();
        super.reload();
    }

    private static final String basePackageName = "org.bukkit.event";

    public Collection<String> getAbbreviations() {
        return ABBREVIATIONS.keySet();
    }

    protected void initEvents() throws IOException {
        for (String clazzName : ReflectionUtil.getAllClasses(Bukkit.class.getClassLoader(), basePackageName)) {
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

    private final Map<EventHook, Listener> registeredListerners = new HashMap<>();

    @Override
    protected void registerEvent(TriggerReactorCore plugin, Class<?> clazz, EventHook eventHook) {
        Listener listener = new Listener() {
        };
        try {
            Bukkit.getPluginManager().registerEvent((Class<? extends Event>) clazz, listener, EventPriority.HIGHEST, new EventExecutor() {
                @Override
                public void execute(Listener arg0, Event arg1) throws EventException {
                    eventHook.onEvent(arg1);
                }
            }, plugin.getMain());

            registeredListerners.put(eventHook, listener);
        } catch (IllegalPluginAccessException e) {
            //event with no handler list will throw this exception
            //which means it's a base event
            if (!BASEEVENTS.contains(BASEEVENTS))
                BASEEVENTS.add((Class<? extends Event>) clazz);
        }
    }

    @Override
    protected void unregisterEvent(TriggerReactorCore plugin, EventHook eventHook) {
        Listener listener = registeredListerners.remove(eventHook);
        if (listener != null) {
            HandlerList.unregisterAll(listener);
        }
    }

    @Override
    protected Class<?> getEventFromName(String name) throws ClassNotFoundException {
        Class<? extends Event> event;
        if (ABBREVIATIONS.containsKey(name)) {
            event = ABBREVIATIONS.get(name);
        } else if (EVENTS.containsKey(name)) {
            event = EVENTS.get(name);
        } else {
            event = (Class<? extends Event>) Class.forName(name);
        }

        return event;
    }
}
