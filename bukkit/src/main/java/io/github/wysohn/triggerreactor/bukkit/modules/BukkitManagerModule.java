package io.github.wysohn.triggerreactor.bukkit.modules;

import dagger.Binds;
import dagger.Module;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.BukkitEventRegistryManager;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.BukkitGUIOpenHelper;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.IEventRegistry;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.IGUIOpenHelper;

@Module
public abstract class BukkitManagerModule {
    @Binds
    abstract IGUIOpenHelper bindIGUIOpenHelper(BukkitGUIOpenHelper impl);

    @Binds
    public abstract IEventRegistry bindBukkitEventRegistry(BukkitEventRegistryManager impl);
}
