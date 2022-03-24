package io.github.wysohn.triggerreactor.core.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.modules.CoreUtilModule;
import io.github.wysohn.triggerreactor.core.scope.PostPluginLifetime;

@Component(modules = {CoreUtilModule.class},
           dependencies = {PluginLifecycleComponent.class,
                           BootstrapComponent.class})
@PostPluginLifetime
public interface UtilComponent {
    IThrowableHandler throwableHandler();
}
