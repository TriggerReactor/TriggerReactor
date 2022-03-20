package io.github.wysohn.triggerreactor.bukkit.modules;

import dagger.Binds;
import dagger.Module;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.BukkitEventRegistryManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.IEventRegistry;

@Module
public abstract class BukkitEventRegistryModule {
    @Binds
    public abstract IEventRegistry bindBukkitEventRegistry(BukkitEventRegistryManager impl);
}
