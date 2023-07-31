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

public interface IPluginLifecycle {
    /**
     * Called when the plugin is about to be enabled. In other words, this is a
     * good place to initialize data that is needed for any other third party
     * plugin.
     */
    default void load() {

    }

    /**
     * Called after the plugin is successfully loaded and is ready to be enabled.
     * In other words, all the third party plugins are loaded and ready to be
     * used, so this is a good place to do any initialization that requires other
     * plugins.
     */
    void initialize();

    /**
     * Called when the player used reload command.
     */
    void reload();

    /**
     * Called when the plugin is about to be disabled. In other words, this is a
     * good place to save current state before the plugin is disabled.
     * <p>
     * Note that this method will block the main thread until it is finished.
     */
    void shutdown();

    /**
     * Called after the plugin is disabled.
     */
    default void unload() {

    }
}
