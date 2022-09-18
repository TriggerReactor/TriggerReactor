package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitWrapper;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.coreprotect.CoreprotectSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.mcmmo.McMmoSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.placeholder.PlaceHolderSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.protocollib.ProtocolLibSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.vault.VaultSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.worldguard.WorldguardSupport;
import io.github.wysohn.triggerreactor.bukkit.tools.SerializableLocation;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TriggerReactor extends AbstractJavaPlugin {
    private SelfReference selfReference;
    private CustomCommandHandle customCommandHandle = new CustomCommandHandle();

    @Override
    public void onEnable() {
        selfReference = new CommonFunctions(core);
        BukkitTriggerReactorCore.WRAPPER = new BukkitWrapper();
        if (!ConfigurationSerializable.class.isAssignableFrom(Location.class)) {
            ConfigurationSerialization.registerClass(SerializableLocation.class, "org.bukkit.Location");
        }

        Bukkit.getPluginManager().registerEvents(customCommandHandle, this);

        super.onEnable();
    }

    @Override
    protected void registerAPIs() {
        APISupport.addSharedVars("coreprotect", CoreprotectSupport.class);
        APISupport.addSharedVars("mcmmo", McMmoSupport.class);
        APISupport.addSharedVars("placeholder", PlaceHolderSupport.class);
        APISupport.addSharedVars("protocollib", ProtocolLibSupport.class);
        APISupport.addSharedVars("vault", VaultSupport.class);
        APISupport.addSharedVars("worldguard", WorldguardSupport.class);
    }

    @Override
    public SelfReference getSelfReference() {
        return selfReference;
    }

    @Override
    public Map<String, Command> getCommandMap() {
        return customCommandHandle;
//        try {
//            Server server = Bukkit.getServer();
//
//            Field f = server.getClass().getDeclaredField("commandMap");
//            f.setAccessible(true);
//
//            CommandMap scm = (CommandMap) f.get(server);
//
//            Field f2 = scm.getClass().getDeclaredField("knownCommands");
//            f2.setAccessible(true);
//
//            return (Map<String, Command>) f2.get(scm);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//
//            core.getLogger().warning("Couldn't bind 'commandMap'. This may indicate that you are using very very old" +
//                    " version of Bukkit. Please report this to TR team, so we can work on it.");
//            core.getLogger().warning("Use /trg debug to see more details.");
//            return null;
//        }
    }

    @Override
    public void synchronizeCommandMap() {
        // do nothing. Not really necessary atm for legacy versions
    }

    private class CustomCommandHandle extends HashMap<String, Command> implements Listener {
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
                command = Optional.of(cmd)
                        .map(aliasesMap::get)
                        .map(super::get)
                        .orElse(null);
            if (command == null)
                return;

            e.getTabCompletions().addAll(command.tabComplete(sender, cmd, args));
        }
    }
}
