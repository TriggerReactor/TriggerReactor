package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.main.AbstractBukkitTriggerReactor;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.coreprotect.CoreprotectSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.mcmmo.McMmoSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.placeholder.PlaceHolderSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.protocollib.ProtocolLibSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.vault.VaultSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.worldguard.WorldguardSupport;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

public class TriggerReactor extends AbstractBukkitTriggerReactor {
	private SelfReference selfReference;
	
	@Override
	public void onEnable() {
		super.onEnable();
		
		selfReference = new CommonFunctions(javaPluginBridge);
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
	
	
}
