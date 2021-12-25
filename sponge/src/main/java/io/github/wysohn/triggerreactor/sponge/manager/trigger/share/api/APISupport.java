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
package io.github.wysohn.triggerreactor.sponge.manager.trigger.share.api;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.APISupportException;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.share.api.nucleus.NucleusSupport;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.HashMap;
import java.util.Map;

public abstract class APISupport extends AbstractAPISupport {
    private final String targetPluginName;
    protected PluginContainer target;
    public APISupport(TriggerReactorCore plugin, String targetPluginName) {
        super(plugin);
        ValidationUtil.notNull(plugin);
        ValidationUtil.notNull(targetPluginName);

        this.targetPluginName = targetPluginName;
    }

    @Override
    public void init() throws APISupportException {
        PluginContainer plugin = Sponge.getPluginManager().getPlugin(targetPluginName).orElse(null);
        if (plugin == null) throw new APISupportException(targetPluginName);

        target = plugin;

        this.plugin.getLogger()
                .info("Enabled support for " + targetPluginName + " " + target.getDescription()
                        .orElse("No description") + " " + target.getVersion().orElse("v. ?"));
    }
    @SuppressWarnings("serial")
    private static Map<String, Class<? extends AbstractAPISupport>> sharedVars = new HashMap<String, Class<? extends AbstractAPISupport>>() {{
        put("nucleus", NucleusSupport.class);
    }};

    public static Map<String, Class<? extends AbstractAPISupport>> getSharedVars() {
        return sharedVars;
    }
}
