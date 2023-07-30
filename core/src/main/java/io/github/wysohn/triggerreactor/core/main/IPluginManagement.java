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

/**
 * This interface provides methods that can be used to control
 * any plugin specific behaviors. For example, spawning an entity
 * is <b>not</b> intended to be here since it is more of the 'game specific'
 * behavior.
 * <p>
 * However, it would make sense to have a method that can be used to
 * disable a plugin, executing a command, etc. is more of the 'plugin specific'
 * behavior, so those methods should be here.
 */
public interface IPluginManagement {
    /**
     * Run a command as a console.
     *
     * @param command the command to be executed (without the slash)
     */
    void runCommandAsConsole(String command);

    /**
     * Gets the current running platform type for the server implementation.
     *
     * @return the current platform type
     */
    Platform getPlatform();
}
