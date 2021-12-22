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

import io.github.wysohn.triggerreactor.core.manager.trigger.named.AbstractNamedTriggerManager;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NamedTriggerManager extends AbstractNamedTriggerManager implements BukkitTriggerManager {
    @Inject
    public NamedTriggerManager() {
        super("NamedTriggers");
    }

    @Override
    public void onDisable() {

    }
}
