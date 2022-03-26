package io.github.wysohn.triggerreactor.bukkit.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.bukkit.modules.BukkitPluginMainModule;
import io.github.wysohn.triggerreactor.bukkit.scope.JavaPluginLifetime;
import io.github.wysohn.triggerreactor.core.components.PluginMainComponent;
import org.bukkit.command.PluginCommand;

@Component(modules = {BukkitPluginMainModule.class})
@JavaPluginLifetime
public interface BukkitPluginMainComponent {
    PluginMainComponent.Builder getMainBuilder();

    @Component.Builder
    interface Builder {
        BukkitPluginMainComponent build();

        Builder pluginMainModule(BukkitPluginMainModule module);

        @BindsInstance
        Builder inject(PluginCommand command);
    }
}
