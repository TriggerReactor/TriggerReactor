/*
 *     Copyright (C) 2021 wysohn and contributors
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.main;

/**
 * Provides plugin specific description and lifecycle controls.
 * <p>
 * It sounds like a duplicate of {@link TriggerReactorMain} but this interface
 * is specifically to control the lifecycle of the plugin, not the implementation
 * of the plugin functionality. Think it as if this interface is the 'meta' application
 * of the TriggerReactor plugin.
 */
public interface IPluginLifecycleController {
    /**
     * Disable this plugin.
     */
    void disablePlugin();

    /**
     * get Author of plugin
     *
     * @return author name of the plugin as String.
     */
    String getAuthor();

    /**
     * Get plugin instance of name 'pluginName'
     *
     * @param pluginName
     * @param <T>        type to be cast into. 'Plugin' in Bukkit API for e.g.
     * @return null if target is not available; instance otherwise. Note that
     * it still returns the plugin instance even if the target plugin is not enabled.
     */
    <T> T getPlugin(String pluginName);

    /**
     * get Plugin's description.
     *
     * @return returns the full name of the plugin and its version.
     */
    String getPluginDescription();

    /**
     * get Plugin's version as String
     *
     * @return version of the plugin as String.
     */
    String getVersion();

    boolean isDebugging();

    void setDebugging(boolean bool);

    /**
     * Check if the target plugin is available and enabled.
     *
     * @param pluginName
     * @return
     */
    boolean isEnabled(String pluginName);

    /**
     * Check if this plugin is enabled.
     *
     * @return true if enabled; false if disabled.
     */
    boolean isEnabled();
}
