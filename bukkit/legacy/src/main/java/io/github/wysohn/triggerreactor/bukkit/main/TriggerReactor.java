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
import io.github.wysohn.triggerreactor.core.bridge.IWrapper;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

public class TriggerReactor extends AbstractJavaPlugin {
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
        return new CommonFunctions(core);
    }

    @Override
    public IWrapper getWrapper() {
        return new BukkitWrapper();
    }
	
	
}
