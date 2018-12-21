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
package io.github.wysohn.triggerreactor.sponge.manager.trigger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractCommandTriggerManager;
import io.github.wysohn.triggerreactor.sponge.manager.trigger.share.CommonFunctions;

public class CommandTriggerManager extends AbstractCommandTriggerManager implements SpongeConfigurationFileIO{
    public CommandTriggerManager(TriggerReactor plugin) {
        super(plugin, new CommonFunctions(plugin), new File(plugin.getDataFolder(), "CommandTrigger"));
    }

    @Listener(order = Order.EARLY)
    public void onCommand(SendCommandEvent e){
        Player player = e.getCause().first(Player.class).orElse(null);

        String cmd = e.getCommand();
        String[] args = e.getArguments().split(" ");

        CommandTrigger trigger = commandTriggerMap.get(cmd);
        if(trigger == null)
            trigger = aliasesMap.get(cmd);
        if(trigger == null)
            return;

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", player);
        varMap.put("command", cmd);
        varMap.put("args", args);
        varMap.put("argslength", args.length);

        trigger.activate(e, varMap);
        e.setCancelled(true);
    }

}
