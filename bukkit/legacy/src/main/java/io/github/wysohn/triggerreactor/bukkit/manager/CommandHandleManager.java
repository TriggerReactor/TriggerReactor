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

package io.github.wysohn.triggerreactor.bukkit.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public
class CommandHandleManager extends Manager implements Listener {
    private final MapAdapter mapAdapter = new MapAdapter();
    @Inject
    private Plugin plugin;

    @Inject
    private CommandHandleManager() {

    }

    @Override
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void reload() {

    }

    @Override
    public void shutdown() {

    }

    public MapAdapter getMapAdapter() {
        return mapAdapter;
    }

    @EventHandler
    public void onTabComplete(PlayerChatTabCompleteEvent e) {
        Player sender = e.getPlayer();
        String[] split = e.getChatMessage().split(" ");

        String cmd = split[0];
        //cmd = cmd.replaceAll("/", "");
        String[] args = new String[split.length - 1];
        for (int i = 0; i < args.length; i++)
            args[i] = split[i + 1];

        Command command = mapAdapter.get(cmd);
        if (command == null)
            command = Optional.of(cmd)
                    .map(mapAdapter.aliasesMap::get)
                    .map(mapAdapter::get)
                    .orElse(null);
        if (command == null)
            return;

        e.getTabCompletions().addAll(command.tabComplete(sender, cmd, args));
    }

    private class MapAdapter extends HashMap<String, Command> {
        private final Map<String, String> aliasesMap = new HashMap<>();

        @Override
        public Command put(String key, Command value) {
            Command previous = super.put(key, value);
            Optional.ofNullable(previous)
                    .ifPresent(c -> c.getAliases().forEach(aliasesMap::remove));
            value.getAliases().forEach(alias -> aliasesMap.put(alias, key));
            return previous;
        }

        @Override
        public void putAll(Map<? extends String, ? extends Command> m) {
            m.forEach((key, command) -> {
                Optional.ofNullable(super.get(key))
                        .ifPresent(c -> c.getAliases().forEach(aliasesMap::remove));
                command.getAliases().forEach(alias -> aliasesMap.put(alias, key));
            });
            super.putAll(m);
        }

        @Override
        public Command remove(Object key) {
            Command remove = super.remove(key);
            Optional.ofNullable(remove)
                    .ifPresent(c -> c.getAliases().forEach(aliasesMap::remove));
            return remove;
        }

        @Override
        public void clear() {
            super.clear();
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

            Command command = super.get(cmd);
            if (command == null)
                command = Optional.of(cmd)
                        .map(aliasesMap::get)
                        .map(super::get)
                        .orElse(null);
            if (command == null)
                return;
            e.setCancelled(true);

            command.execute(player, cmd, args);
        }
    }
}