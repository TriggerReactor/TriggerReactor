package io.github.wysohn.triggerreactor.bukkit.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.bukkit.modules.LatestBukkitExternalAPIModule;
import io.github.wysohn.triggerreactor.bukkit.modules.LatestBukkitModule;
import io.github.wysohn.triggerreactor.bukkit.modules.LatestBukkitPluginMainModule;
import io.github.wysohn.triggerreactor.bukkit.scope.BukkitPluginBootstrapScope;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorMain;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

@Component(modules = {LatestBukkitPluginMainModule.class,
                      LatestBukkitModule.class,
                      LatestBukkitExternalAPIModule.class,},
           dependencies = {BukkitPluginMainComponent.class,})
@BukkitPluginBootstrapScope
public interface LatestBukkitPluginMainComponent {
    void inject(JavaPlugin javaPlugin);

    TriggerReactorMain main();

    Set<Manager> managers();

    IPluginLifecycleController pluginLifecycle();
}
