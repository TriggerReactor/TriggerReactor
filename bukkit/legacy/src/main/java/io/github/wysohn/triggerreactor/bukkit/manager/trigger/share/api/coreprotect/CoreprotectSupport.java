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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.coreprotect;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.APISupportException;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

public class CoreprotectSupport extends APISupport {
    static {
        addSharedVars("coreprotect", CoreprotectSupport.class);
    }

    /**
     * I'm just too lazy to add all those methods. Please use this to access directly with api.
     * http://minerealm.com/community/viewtopic.php?f=32&t=16534
     */
    protected CoreProtectAPI api;

    public CoreprotectSupport(TriggerReactor plugin) {
        super(plugin, "CoreProtect");
    }

    @Override
    public void init() throws APISupportException {
        super.init();

        api = CoreProtect.getInstance().getAPI();

        try {
            api.getClass().getMethod("APIVersion");
        } catch (NoSuchMethodException e) {
            plugin.getLogger().warning("Found CoreProtect, but the version is too low.");
            throw new APISupportException("API version too low.");
        }

        if (api.APIVersion() < 4) {
            plugin.getLogger().warning("Found CoreProtect, but the version is too low.");
            throw new APISupportException("API version too low.");
        }
    }
}
