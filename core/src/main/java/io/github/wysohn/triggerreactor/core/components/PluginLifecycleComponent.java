package io.github.wysohn.triggerreactor.core.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.scope.PluginScope;

@Component
@PluginScope
public interface PluginLifecycleComponent {
    IPluginLifecycleController pluginLifecycleController();

    @Component.Builder
    interface Builder {
        PluginLifecycleComponent build();

        // injects
        @BindsInstance
        Builder pluginLifecycle(IPluginLifecycleController controller);
    }
}
