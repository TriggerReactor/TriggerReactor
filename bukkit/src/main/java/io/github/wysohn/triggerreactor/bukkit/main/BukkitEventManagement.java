/*
 * Copyright (C) 2022. TriggerReactor Team
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

package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.core.main.IEventManagement;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;

public class BukkitEventManagement implements IEventManagement {
    @Override
    public Object createPlayerCommandEvent(ICommandSender sender, String label, String[] args) {
        Object unwrapped = sender.get();

        StringBuilder builder = new StringBuilder("/");
        builder.append(label);
        for (String arg : args) {
            builder.append(' ');
            builder.append(arg);
        }

        if (unwrapped instanceof Player) {
            return new PlayerCommandPreprocessEvent((Player) unwrapped, builder.toString());
        } else {
            throw new RuntimeException("Cannot create empty PlayerCommandPreprocessEvent for " + sender);
        }
    }

    @Override
    public Object createEmptyPlayerEvent(ICommandSender sender) {
        Object unwrapped = sender.get();

        if (unwrapped instanceof Player) {
            return new PlayerEvent((Player) unwrapped) {
                @Override
                public HandlerList getHandlers() {
                    return null;
                }
            };
        } else if (unwrapped instanceof CommandSender) {
            return new AbstractJavaPlugin.CommandSenderEvent((CommandSender) unwrapped);
        } else {
            throw new RuntimeException("Cannot create empty PlayerEvent for " + sender);
        }
    }

    @Override
    public void callEvent(IEvent event) {
        Bukkit.getPluginManager().callEvent(event.get());
    }

}
