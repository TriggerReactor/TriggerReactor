package io.github.wysohn.triggerreactor.core.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.PluginConfigManager;
import io.github.wysohn.triggerreactor.core.modules.CoreManagerModule;
import io.github.wysohn.triggerreactor.core.scope.ManagerScope;

@Component(modules = CoreManagerModule.class,
           dependencies = {ConfigurationComponent.class, BootstrapComponent.class})
@ManagerScope
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
