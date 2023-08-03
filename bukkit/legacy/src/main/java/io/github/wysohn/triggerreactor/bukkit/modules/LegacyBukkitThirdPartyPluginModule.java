package io.github.wysohn.triggerreactor.bukkit.modules;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.ProvidesIntoMap;
import com.google.inject.multibindings.StringMapKey;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.coreprotect.CoreprotectSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.mcmmo.McMmoSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.placeholder.PlaceHolderSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.protocollib.ProtocolLibSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.vault.VaultSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.worldguard.WorldguardSupport;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;

public class LegacyBukkitThirdPartyPluginModule extends AbstractModule {
    @ProvidesIntoMap
    @StringMapKey("coreprotect")
    public Class<? extends AbstractAPISupport> provideCoreProtect() {
        return CoreprotectSupport.class;
    }

    @ProvidesIntoMap
    @StringMapKey("mcmmo")
    public Class<? extends AbstractAPISupport> provideMcMmo() {
        return McMmoSupport.class;
    }

    @ProvidesIntoMap
    @StringMapKey("placeholder")
    public Class<? extends AbstractAPISupport> providePlaceHolder() {
        return PlaceHolderSupport.class;
    }

    @ProvidesIntoMap
    @StringMapKey("protocollib")
    public Class<? extends AbstractAPISupport> provideProtocolLib() {
        return ProtocolLibSupport.class;
    }

    @ProvidesIntoMap
    @StringMapKey("vault")
    public Class<? extends AbstractAPISupport> provideVault() {
        return VaultSupport.class;
    }

    @ProvidesIntoMap
    @StringMapKey("worldguard")
    public Class<? extends AbstractAPISupport> provideWorldGuard() {
        return WorldguardSupport.class;
    }
}