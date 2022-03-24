package io.github.wysohn.triggerreactor.core.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.PluginConfigManager;
import io.github.wysohn.triggerreactor.core.modules.ConstantsModule;
import io.github.wysohn.triggerreactor.core.modules.CoreManagerModule;
import io.github.wysohn.triggerreactor.core.scope.ManagerLifetime;

import java.util.Set;

@Component(modules = {CoreManagerModule.class,
                      ConstantsModule.class},
           dependencies = {ConfigurationComponent.class,
                           PluginLifecycleComponent.class,
                           BootstrapComponent.class,
                           ExternalAPIComponent.class,
                           InventoryUtilComponent.class,
                           UtilComponent.class})
@ManagerLifetime
public interface ManagerComponent {
    GlobalVariableManager globalVariableManager();

    PluginConfigManager pluginConfigManager();

    Set<Manager> managers();

    @Component.Builder
    interface Builder {
        ManagerComponent builder();

        // dependencies
        Builder configurationComponent(ConfigurationComponent component);

        Builder pluginLifecycleComponent(PluginLifecycleComponent component);

        Builder bootstrapComponent(BootstrapComponent component);

        Builder externalAPIComponent(ExternalAPIComponent component);

        Builder inventoryUtilComponent(InventoryUtilComponent component);

        Builder utilComponent(UtilComponent component);

        // injects
    }
}
