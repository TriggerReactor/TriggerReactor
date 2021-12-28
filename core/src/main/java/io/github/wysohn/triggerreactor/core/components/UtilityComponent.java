package io.github.wysohn.triggerreactor.core.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.modules.CoreUtilModule;
import io.github.wysohn.triggerreactor.core.scope.UtilityScope;

@Component(modules = CoreUtilModule.class,
           dependencies = PluginLifecycleComponent.class)
@UtilityScope
public interface UtilityComponent {
    IThrowableHandler throwableHandler();

    @Component.Builder
    interface Builder {
        UtilityComponent build();

        Builder lifecycleComponent(PluginLifecycleComponent component);

        @BindsInstance
        Builder gameController(IGameController gameController);
    }
}
