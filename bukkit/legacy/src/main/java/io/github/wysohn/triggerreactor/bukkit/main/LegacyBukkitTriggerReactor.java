package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.components.BukkitTriggerReactorComponent;
import io.github.wysohn.triggerreactor.bukkit.tools.SerializableLocation;
import io.github.wysohn.triggerreactor.core.bridge.ICommand;
import io.github.wysohn.triggerreactor.core.main.CommandHandler;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandMapHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LegacyBukkitTriggerReactor extends JavaPlugin implements ICommandMapHandler {
    private BukkitTriggerReactorComponent component = DaggerLegacyBukkitTriggerReactorComponent.builder().build();
    private BukkitTriggerReactor bukkitTriggerReactor;
    private CommandHandler commandHandler;
    private IWrapper wrapper;

    private CustomCommandHandle customCommandHandle = new CustomCommandHandle();

    @Override
    public void onEnable() {
        bukkitTriggerReactor = component.bukkitTriggerReactor();
        commandHandler = component.commandHandler();
        wrapper = component.wrapper();

        if (!ConfigurationSerializable.class.isAssignableFrom(Location.class)) {
            ConfigurationSerialization.registerClass(SerializableLocation.class, "org.bukkit.Location");
        }
        Bukkit.getPluginManager().registerEvents(customCommandHandle, this);

        bukkitTriggerReactor.onEnable();
    }

    @Override
    public void onDisable() {
        bukkitTriggerReactor.onDisable();
    }

    @Override
    public void synchronizeCommandMap() {
        // do nothing. Not really necessary atm for legacy versions
    }

    @Override
    public boolean unregister(String triggerName) {
        return customCommandHandle.remove(triggerName) != null;
    }

    @Override
    public boolean commandExist(String triggerName) {
        return customCommandHandle.containsKey(triggerName);
    }

    @Override
    public ICommand register(String triggerName, String[] aliases) throws Duplicated {
        return null;
    }

    private class CustomCommandHandle extends HashMap<String, Command> implements Listener {
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
}
