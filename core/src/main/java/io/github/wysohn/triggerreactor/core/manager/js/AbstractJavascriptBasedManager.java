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
package io.github.wysohn.triggerreactor.core.manager.js;

import io.github.wysohn.triggerreactor.core.manager.KeyValueManager;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.evaluable.IEvaluable;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

public abstract class AbstractJavascriptBasedManager<T extends IEvaluable>
        extends Manager
        implements KeyValueManager<T> {


    protected final Map<String, T> overrides;
    protected final File folder;
    protected Map<String, T> evaluables = new HashMap<>();

    public AbstractJavascriptBasedManager(Map<String, T> overrides,
                                          File folder) {
        super();
        this.overrides = overrides;
        this.folder = folder;
    }

    /* (non-Javadoc)
     * @see KeyValueManager#get(java.lang.Object)
     */
    @Override
    public T get(Object key) {
        return evaluables.get(key);
    }

    /* (non-Javadoc)
     * @see KeyValueManager#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return evaluables.containsKey(key);
    }

    /* (non-Javadoc)
     * @see KeyValueManager#entrySet()
     */
    @Override
    public Set<Map.Entry<String, T>> entrySet() {
        Set<Map.Entry<String, T>> set = new HashSet<>();
        for (Map.Entry<String, T> entry : evaluables.entrySet()) {
            set.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
        }
        return set;
    }

    /* (non-Javadoc)
     * @see KeyValueManager#getExecutorMap()
     */
    @Override
    public Map<String, T> getBackedMap() {
        return evaluables;
    }

    protected File[] listJSFiles() {
        FileFilter filter = pathname -> pathname.isDirectory() || pathname.getName().endsWith(".js");
        return Objects.requireNonNull(folder.listFiles(filter));
    }
}