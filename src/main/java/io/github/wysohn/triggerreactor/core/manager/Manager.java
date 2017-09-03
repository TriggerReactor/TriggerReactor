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
package io.github.wysohn.triggerreactor.core.manager;

import java.util.ArrayList;
import java.util.List;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;

/**
 * The base class represents Manager. Child classes are responsible for only one objective per manager.
 * So if the child class is ClickTriggerManager, it only has to deal with ClickTrigger.
 * @author wysohn
 *
 */
public abstract class Manager{
    private static final List<Manager> managers = new ArrayList<Manager>();
    public static List<Manager> getManagers() {
        return managers;
    }

    protected final TriggerReactor plugin;

    public Manager(TriggerReactor plugin) {
        this.plugin = plugin;

        managers.add(this);

        plugin.registerEvents(this);
    }

    /**
     * Reload all triggers
     */
    public abstract void reload();

    /**
     * Save all triggers
     */
    public abstract void saveAll();
}
