/*
 * Copyright (C) 2023. TriggerReactor Team
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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.placeholder;

import com.google.inject.Injector;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.APISupportException;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

public class PlaceHolderSupport extends APISupport {
    private final PluginDescriptionFile pluginDesc;
    private final GlobalVariableManager globalVariableManager;

    public PlaceHolderSupport(Injector injector) {
        super(injector, "PlaceholderAPI");

        pluginDesc = injector.getInstance(PluginDescriptionFile.class);
        globalVariableManager = injector.getInstance(GlobalVariableManager.class);
    }

    @Override
    public void init() throws APISupportException {
        super.init();

        //init() is called only when PlaceholderAPI is enabled.
        new PlaceholderExpansionSupport(pluginDesc, globalVariableManager).register();
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
