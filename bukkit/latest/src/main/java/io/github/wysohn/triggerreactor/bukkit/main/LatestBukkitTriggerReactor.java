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
package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitWrapper;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.coreprotect.CoreprotectSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.mcmmo.McMmoSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.placeholder.PlaceHolderSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.protocollib.ProtocolLibSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.vault.VaultSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.worldguard.WorldguardSupport;
import io.github.wysohn.triggerreactor.bukkit.modules.BukkitPluginMainModule;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;

import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;

public class LatestBukkitTriggerReactor extends BukkitTriggerReactor {

    @Override
    protected BukkitPluginMainModule getModule(Map<String, Command> rawCommands) {
        return createModule(this, rawCommands);
    }

    public static BukkitPluginMainModule createModule(Plugin plugin,
                                                      Map<String, Command> rawCommands){
        return new BukkitPluginMainModule(plugin,
                new BukkitWrapper(),
                new CommonFunctions(),
                new ScriptEngineManager(),
                getAPIProtoMap(),
                new LatestBukkitCommandMapHandler(plugin, rawCommands));
    }

    private static Map<String, Class<? extends AbstractAPISupport>> getAPIProtoMap() {
        Map<String, Class<? extends AbstractAPISupport>> map = new HashMap<>();
        map.put("CoreProtect", CoreprotectSupport.class);
        map.put("mcMMO", McMmoSupport.class);
        map.put("PlaceholderAPI", PlaceHolderSupport.class);
        map.put("ProtocolLib", ProtocolLibSupport.class);
        map.put("Vault", VaultSupport.class);
        map.put("WorldGuard", WorldguardSupport.class);
        return map;
    }
}
