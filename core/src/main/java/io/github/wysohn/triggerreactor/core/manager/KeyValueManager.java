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
package io.github.wysohn.triggerreactor.core.manager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface KeyValueManager<V> {

    V get(Object key);

    boolean containsKey(Object key);

    Set<Entry<String, V>> entrySet();

    /**
     * Get the reference of actual map internally used.
     *
     * @return Map of key and value
     */
    Map<String, V> getBackedMap();

}