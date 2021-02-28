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
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.lang.reflect.Field;
import java.util.Map;

public class TriggerReactor extends AbstractJavaPlugin {
    private SelfReference selfReference;

    @Override
    public void onEnable() {
        selfReference = new CommonFunctions(core);
        BukkitTriggerReactorCore.WRAPPER = new BukkitWrapper();
        if (!ConfigurationSerializable.class.isAssignableFrom(Location.class)) {
            ConfigurationSerialization.registerClass(SerializableLocation.class, "org.bukkit.Location");
        }
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
    public Map<String, Command> getCommandMap(TriggerReactorCore plugin) {
        try {
            Server server = Bukkit.getServer();

            Field f = server.getClass().getDeclaredField("commandMap");
            f.setAccessible(true);

            CommandMap scm = (CommandMap) f.get(server);

            Field f2 = scm.getClass().getDeclaredField("knownCommands");
            f2.setAccessible(true);

            return (Map<String, Command>) f2.get(scm);
        } catch (Exception ex) {
            ex.printStackTrace();

            core.getLogger().warning("Couldn't bind 'commandMap'. This may indicate that you are using very very old" +
                    " version of Bukkit. Please report this to TR team, so we can work on it.");
            core.getLogger().warning("Use /trg debug to see more details.");
            return null;
        }
    }

    @Override
    public void synchronizeCommandMap() {
        // do nothing. Not really necessary atm for legacy versions
    }
}
