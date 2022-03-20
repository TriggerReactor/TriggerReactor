package io.github.wysohn.triggerreactor.bukkit.modules;

import dagger.Binds;
import dagger.Module;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.BukkitGUIOpenHelper;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.IGUIOpenHelper;

@Module
public abstract class BukkitUtilModule {
    @Binds
    abstract IGUIOpenHelper bindIGUIOpenHelper(BukkitGUIOpenHelper impl);
}
