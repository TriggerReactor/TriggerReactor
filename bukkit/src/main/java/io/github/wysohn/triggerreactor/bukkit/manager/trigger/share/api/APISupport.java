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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api;


import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import org.bukkit.plugin.Plugin;

public abstract class APISupport extends AbstractAPISupport {
    private final Plugin target;

    public APISupport(Object targetPluginInstance) {
        super(targetPluginInstance);
        target = (Plugin) targetPluginInstance;
    }

    @Override
    public String toString() {
        return target.getDescription().getFullName();
    }
}
