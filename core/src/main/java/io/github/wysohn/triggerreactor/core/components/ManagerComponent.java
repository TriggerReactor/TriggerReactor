package io.github.wysohn.triggerreactor.core.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.PluginConfigManager;
import io.github.wysohn.triggerreactor.core.modules.CoreManagerModule;

@Component(modules = CoreManagerModule.class,
           dependencies = {ConfigurationComponent.class, BootstrapComponent.class})
public interface ManagerComponent {
    GlobalVariableManager globalVariableManager();

    PluginConfigManager pluginConfigManager();

    @Component.Builder
    interface Builder {
        ManagerComponent builder();

        // dependencies
        Builder configurationComponent(ConfigurationComponent component);

        Builder bootstrapComponent(BootstrapComponent component);

        // injects
    }
}
