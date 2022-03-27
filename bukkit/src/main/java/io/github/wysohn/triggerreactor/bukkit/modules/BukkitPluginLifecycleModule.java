package io.github.wysohn.triggerreactor.bukkit.modules;

import dagger.Binds;
import dagger.Module;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitPluginLifecycle;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;

@Module
public abstract class BukkitPluginLifecycleModule {
    @Binds
    abstract IPluginLifecycleController bindPluginLifecycleController(BukkitPluginLifecycle impl);
}
