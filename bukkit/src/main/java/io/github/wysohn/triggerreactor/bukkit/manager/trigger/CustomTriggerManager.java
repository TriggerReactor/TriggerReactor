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

import io.github.wysohn.triggerreactor.core.main.TriggerReactorMain;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.AbstractCustomTriggerManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Singleton
public class CustomTriggerManager extends AbstractCustomTriggerManager implements BukkitTriggerManager {
    private final Map<EventHook, Listener> registeredListerners = new HashMap<>();
    @Inject
    @Named("PluginInstance")
    Object pluginInstance;
    @Inject
    public CustomTriggerManager() {
        super("CustomTrigger");
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onReload() {
        for (Entry<EventHook, Listener> entry : registeredListerners.entrySet()) {
            HandlerList.unregisterAll(entry.getValue());
        }
        registeredListerners.clear();
        super.onReload();
    }

    @Override
    protected void registerEvent(TriggerReactorMain plugin, Class<?> clazz, EventHook eventHook) {
        Listener listener = new Listener() {
        };
        try {
            Bukkit.getPluginManager()
                    .registerEvent((Class<? extends Event>) clazz,
                                   listener,
                                   EventPriority.HIGHEST,
                                   (l, event) -> eventHook.onEvent(event),
                                   (Plugin) pluginInstance);

            registeredListerners.put(eventHook, listener);
        } catch (IllegalPluginAccessException e) {
            //event with no handler list will throw this exception
            //which means it's a base event
            if (!BASEEVENTS.contains(BASEEVENTS)) BASEEVENTS.add((Class<? extends Event>) clazz);
        }
    }

    @Override
    protected void unregisterEvent(TriggerReactorMain plugin, EventHook eventHook) {
        Listener listener = registeredListerners.remove(eventHook);
        if (listener != null) {
            HandlerList.unregisterAll(listener);
        }
    }
    private static final List<Class<? extends Event>> BASEEVENTS = new ArrayList<Class<? extends Event>>();
}
