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
import io.github.wysohn.triggerreactor.bukkit.tools.SerializableLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;

public class LegacyBukkitTriggerReactor extends BukkitTriggerReactor {
    private final CustomCommandHandle customCommandHandle = new CustomCommandHandle();

    @Override
    public void onEnable() {
        if (!ConfigurationSerializable.class.isAssignableFrom(Location.class)) {
            ConfigurationSerialization.registerClass(SerializableLocation.class, "org.bukkit.Location");
        }
        Bukkit.getPluginManager().registerEvents(customCommandHandle, this);

        super.onEnable();
    }

    @Override
    protected BukkitPluginMainModule getModule(Map<String, Command> rawCommands) {
        return createModule(this, customCommandHandle);
    }

    public static BukkitPluginMainModule createModule(Plugin plugin,
                                                      CustomCommandHandle handle){
        return new BukkitPluginMainModule(plugin,
                new BukkitWrapper(),
                new CommonFunctions(),
                new ScriptEngineManager(),
                getAPIProtoMap(),
                new LegacyBukkitCommandMapHandler(handle));
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
