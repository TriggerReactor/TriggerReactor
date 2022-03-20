package io.github.wysohn.triggerreactor.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.manager.ScriptEditManager;

@Component
public interface ScriptEditManagerTestComponent {
    ScriptEditManager getScriptEditManager();

    @Component.Builder
    interface Builder {
        ScriptEditManagerTestComponent build();

        @BindsInstance
        Builder throwableHandler(IThrowableHandler throwableHandler);
    }
}
