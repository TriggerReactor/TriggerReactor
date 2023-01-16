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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.coreprotect;

import com.google.inject.Injector;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.APISupportException;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

import java.util.logging.Logger;

public class CoreprotectSupport extends APISupport {
    /**
     * I'm just too lazy to add all those methods. Please use this to access directly with api.
     * http://minerealm.com/community/viewtopic.php?f=32&t=16534
     */
    protected CoreProtectAPI api;
    private final Logger logger;

    public CoreprotectSupport(Injector injector) {
        super(injector, "CoreProtect");

        this.logger = injector.getInstance(Logger.class);
    }

    @Override
    public void init() throws APISupportException {
        super.init();

        api = CoreProtect.getInstance().getAPI();

        try {
            api.getClass().getMethod("APIVersion");
        } catch (NoSuchMethodException e) {
            logger.warning("Found CoreProtect, but the version is too low.");
            throw new APISupportException("API version too low.");
        }

        if (api.APIVersion() < 4) {
            logger.warning("Found CoreProtect, but the version is too low.");
            throw new APISupportException("API version too low.");
        }
    }
}
