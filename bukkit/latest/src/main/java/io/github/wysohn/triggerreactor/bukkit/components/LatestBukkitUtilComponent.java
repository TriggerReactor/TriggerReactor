package io.github.wysohn.triggerreactor.bukkit.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.bukkit.modules.LatestBukkitUtilModule;
import io.github.wysohn.triggerreactor.bukkit.scope.BukkitPluginChildLifetime;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

@Component(modules = LatestBukkitUtilModule.class,
           dependencies = {TriggerComponent.class})
@BukkitPluginChildLifetime
public interface LatestBukkitUtilComponent {
    IWrapper wrapper();

    SelfReference selfReference();
}
