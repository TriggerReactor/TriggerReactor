package io.github.wysohn.triggerreactor.bukkit.modules;

import dagger.Binds;
import dagger.Module;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitWrapper;
import io.github.wysohn.triggerreactor.bukkit.components.BukkitPluginMainComponent;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

@Module(subcomponents = BukkitPluginMainComponent.class)
public abstract class LatestBukkitPluginMainModule {
    @Binds
    abstract SelfReference bindSelfReference(CommonFunctions fn);

    @Binds
    abstract IWrapper bindWrapper(BukkitWrapper wrapper);
}
