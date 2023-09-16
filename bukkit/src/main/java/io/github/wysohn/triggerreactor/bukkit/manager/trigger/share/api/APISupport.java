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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api;

import com.google.inject.Injector;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.APISupportException;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import org.apache.commons.lang.Validate;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public abstract class APISupport extends AbstractAPISupport {
    private final String targetPluginName;

    protected Plugin target;

    private Logger logger;
    private Server server;

    public APISupport(Injector injector, String targetPluginName) {
        super(injector);
        Validate.notNull(injector);
        Validate.notNull(targetPluginName);

        this.targetPluginName = targetPluginName;

        this.logger = injector.getInstance(Logger.class);
        this.server = injector.getInstance(Server.class);
    }

    /**
     * Initialize this API. It may throw APISupportException if the plugin is not found.
     *
     * @throws APISupportException throw this exception when the supporting API is not loaded or not found.
     */
    @Override
    public void init() throws APISupportException {
        Plugin plugin = server.getPluginManager().getPlugin(targetPluginName);
        if (plugin == null || !plugin.isEnabled())
            throw new APISupportException(targetPluginName);

        target = plugin;

        this.logger.info("Enabled support for " + targetPluginName + " " + target.getDescription().getFullName());
    }

//    @SuppressWarnings("serial")
//    private static Map<String, Class<? extends AbstractAPISupport>> sharedVars
//            = new HashMap<String, Class<? extends AbstractAPISupport>>();
//
//    public static boolean addSharedVars(String name, Class<? extends AbstractAPISupport> clazz) {
//        return sharedVars.put(name, clazz) == null;
//    }
//
//    public static Map<String, Class<? extends AbstractAPISupport>> getSharedVars() {
//        return sharedVars;
//    }
}
