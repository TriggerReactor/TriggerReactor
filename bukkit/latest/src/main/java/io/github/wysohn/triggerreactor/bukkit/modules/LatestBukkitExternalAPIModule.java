package io.github.wysohn.triggerreactor.bukkit.modules;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.coreprotect.CoreprotectSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.mcmmo.McMmoSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.placeholder.PlaceHolderSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.protocollib.ProtocolLibSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.vault.VaultSupport;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.worldguard.WorldguardSupport;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;

@Module
public abstract class LatestBukkitExternalAPIModule {
    @Provides
    @IntoMap
    @StringKey("CoreProtect")
    static Class<? extends AbstractAPISupport> provideCoreProtect() {
        return CoreprotectSupport.class;
    }

    @Provides
    @IntoMap
    @StringKey("mcMMO")
    static Class<? extends AbstractAPISupport> provideMcmmo() {
        return McMmoSupport.class;
    }

    @Provides
    @IntoMap
    @StringKey("PlaceholderAPI")
    static Class<? extends AbstractAPISupport> providePapi() {
        return PlaceHolderSupport.class;
    }

    @Provides
    @IntoMap
    @StringKey("ProtocolLib")
    static Class<? extends AbstractAPISupport> provideProtocollib() {
        return ProtocolLibSupport.class;
    }

    @Provides
    @IntoMap
    @StringKey("Vault")
    static Class<? extends AbstractAPISupport> provideVault() {
        return VaultSupport.class;
    }

    @Provides
    @IntoMap
    @StringKey("WorldGuard")
    static Class<? extends AbstractAPISupport> provideWG() {
        return WorldguardSupport.class;
    }
}
