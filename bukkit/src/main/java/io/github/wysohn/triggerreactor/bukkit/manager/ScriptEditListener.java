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
package io.github.wysohn.triggerreactor.bukkit.manager;

import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitPlayer;
import io.github.wysohn.triggerreactor.core.manager.ScriptEditManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.TabCompleteEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;

@Singleton
public class ScriptEditListener implements Listener {
    private final ScriptEditManager manager;

    @Inject
    public ScriptEditListener(ScriptEditManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (manager.onChat(new BukkitPlayer(event.getPlayer()),
                event.getMessage())) {
            event.setCancelled(true);
        } else {
            event.getRecipients()
                    .removeIf(receiver -> manager.isEditing(new BukkitPlayer(receiver)));
        }
    }

    @EventHandler
    public void onTab(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player))
            return;

        if (!manager.isEditing(new BukkitPlayer((Player) event.getSender())))
            return;

        manager.onTab(new BukkitPlayer((Player) event.getSender()), line ->
                event.setCompletions(Collections.singletonList(line)));
    }
}