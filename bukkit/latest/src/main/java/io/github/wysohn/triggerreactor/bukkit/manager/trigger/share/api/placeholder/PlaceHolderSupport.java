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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.placeholder;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceHolderSupport extends APISupport {
    public PlaceHolderSupport(Object targetPluginInstance) {
        super(targetPluginInstance);
    }

    @Override
    public String getVariableName() {
        return "placeholder";
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() throws Exception {
        //init() is called only when PlaceholderAPI is enabled.
        new PlaceholderExpansionSupport(TriggerReactorAPI.pluginLifecycleController(),
                TriggerReactorAPI.getGlobalVariableManager()).register();
    }

    @Override
    public void onReload() throws RuntimeException {

    }

    /**
     * Translate placeholders to actual string.
     *
     * @param player
     * @param string string before the translation
     * @return translated string
     */
    public String parse(Player player, String string) {
        return PlaceholderAPI.setPlaceholders(player, string);
    }
}
