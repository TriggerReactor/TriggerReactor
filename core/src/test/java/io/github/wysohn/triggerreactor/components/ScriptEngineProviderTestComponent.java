package io.github.wysohn.triggerreactor.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.ScriptEngineProvider;
import modules.TestScriptEngineModule;

import javax.inject.Singleton;
import javax.script.ScriptEngineManager;
import java.util.logging.Logger;

@Component(modules = {TestScriptEngineModule.class})
@Singleton
public interface ScriptEngineProviderTestComponent {
    ScriptEngineProvider provider();

    @Component.Builder
    interface Builder {
        ScriptEngineProviderTestComponent build();

        @BindsInstance
        Builder logger(Logger logger);

        @BindsInstance
        Builder engineManager(ScriptEngineManager manager);
    }
}
