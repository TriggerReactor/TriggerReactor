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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.AbstractCommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTrigger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CommandTriggerManager extends AbstractCommandTriggerManager implements BukkitTriggerManager {
    public CommandTriggerManager(TriggerReactorCore plugin) {
        super(plugin, new File(plugin.getDataFolder(), "CommandTrigger"));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        String[] split = e.getMessage().split(" ");

        String cmd = split[0];
        cmd = cmd.replaceAll("/", "");
        String[] args = new String[split.length - 1];
        for (int i = 0; i < args.length; i++)
            args[i] = split[i + 1];

        CommandTrigger trigger = triggers.get(cmd);
        if (trigger == null)
            trigger = aliasesMap.get(cmd);
        if (trigger == null)
            return;
        e.setCancelled(true);

        for (String permission : trigger.getPermissions()) {
            if (!player.hasPermission(permission)) {
                player.sendMessage(ChatColor.RED + "[TR] You don't have permission!");
                if (plugin.isDebugging()) {
                    plugin.getLogger().info("Player " + player.getName() + " executed command " + cmd
                            + " but didn't have permission " + permission + "");
                }
                return;
            }
        }

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", player);
        varMap.put("command", cmd);
        varMap.put("args", args);
        varMap.put("argslength", args.length);

        trigger.activate(e, varMap);
    }

}
