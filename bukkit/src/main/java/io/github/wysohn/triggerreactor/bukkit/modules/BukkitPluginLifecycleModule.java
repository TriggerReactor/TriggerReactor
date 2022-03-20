package io.github.wysohn.triggerreactor.bukkit.modules;

import dagger.Binds;
import dagger.Module;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitCommandMapHandler;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitPluginLifecycle;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandMapHandler;

@Module
public abstract class BukkitPluginLifecycleModule {
    @Binds
    abstract IPluginLifecycleController bindPluginLifecycleController(BukkitPluginLifecycle impl);

    @Binds
    abstract ICommandMapHandler bindCommandMapHandler(BukkitCommandMapHandler impl);
}
