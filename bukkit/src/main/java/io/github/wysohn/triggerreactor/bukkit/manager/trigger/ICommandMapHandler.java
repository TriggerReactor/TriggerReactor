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

package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import org.bukkit.command.Command;

import java.util.Map;

public interface ICommandMapHandler {
    /**
     * Get the actual command map from the server.
     * Do not delete commands from this map unless the commands are what we registered.
     * If we do, we might be deleting commands that were registered by other plugins.
     *
     * @return the actual command map from the server.
     */
    Map<String, Command> getCommandMap();

    void synchronizeCommandMap();
}
