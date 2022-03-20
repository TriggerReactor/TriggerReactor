package io.github.wysohn.triggerreactor.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.bukkit.modules.BukkitEventRegistryModule;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.IEventRegistry;
import org.bukkit.plugin.PluginManager;

import javax.inject.Named;
import javax.inject.Singleton;

@Component(modules = {BukkitEventRegistryModule.class})
@Singleton
public interface BukkitEventRegistryManagerTestComponent {
    IEventRegistry eventRegistry();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder pluginInstance(@Named("PluginInstance") Object pluginInstance);
        @BindsInstance
        Builder pluginManager(PluginManager pluginManager);

        BukkitEventRegistryManagerTestComponent build();
    }
}
