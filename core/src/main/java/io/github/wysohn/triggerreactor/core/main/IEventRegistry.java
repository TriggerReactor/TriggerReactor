/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.core.IEventHook;

import java.util.Collection;

public interface IEventRegistry {

    boolean eventExist(String eventStr);

    /**
     * First it tries to return Event in ABBREVIATIONS if such name exists. If it wasn't found, then it simply
     * treat the eventStr as full class name and try to get the Event using {@link Class#forName(String)} method.
     * ex) 1. onJoin -> 2. org.bukkit.event.player.PlayerJoinEvent
     *
     * @param eventStr name of event to search
     * @return the event class
     * @throws ClassNotFoundException throws if search fails or the result event is
     *                                a event that cannot receive events (abstract events).
     */
    Class<?> getEvent(String eventStr) throws ClassNotFoundException;

    /**
     * Hook event to handle it manually.
     *
     * @param plugin
     * @param clazz
     * @param eventHook
     */
    void registerEvent(Class<?> clazz, IEventHook eventHook);

    void unregisterEvent(IEventHook eventHook);

    void unregisterAll();

    Collection<String> getAbbreviations();
}
