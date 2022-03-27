package io.github.wysohn.triggerreactor.bukkit.main;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CustomCommandHandle extends HashMap<String, Command> implements Listener {
    private final Map<String, String> aliasesMap = new HashMap<>();

    @Override
    public Command put(String key, Command value) {
        Command previous = super.put(key, value);
        Optional.ofNullable(previous).ifPresent(c -> c.getAliases().forEach(aliasesMap::remove));
        value.getAliases().forEach(alias -> aliasesMap.put(alias, key));
        return previous;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Command> m) {
        m.forEach((key, command) -> {
            Optional.ofNullable(super.get(key)).ifPresent(c -> c.getAliases().forEach(aliasesMap::remove));
            command.getAliases().forEach(alias -> aliasesMap.put(alias, key));
        });
        super.putAll(m);
    }

    @Override
    public Command remove(Object key) {
        Command remove = super.remove(key);
        Optional.ofNullable(remove).ifPresent(c -> c.getAliases().forEach(aliasesMap::remove));
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
            command = Optional.of(cmd).map(aliasesMap::get).map(super::get).orElse(null);
        if (command == null)
            return;
        e.setCancelled(true);

        command.execute(player, cmd, args);
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

        Command command = super.get(cmd);
        if (command == null)
            command = Optional.of(cmd).map(aliasesMap::get).map(super::get).orElse(null);
        if (command == null)
            return;

        e.getTabCompletions().addAll(command.tabComplete(sender, cmd, args));
    }
}