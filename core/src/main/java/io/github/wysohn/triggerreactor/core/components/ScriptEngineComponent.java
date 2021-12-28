package io.github.wysohn.triggerreactor.core.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.modules.CoreScriptEngineManagerModule;
import io.github.wysohn.triggerreactor.core.scope.ScriptEngineScope;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@Component(modules = {CoreScriptEngineManagerModule.class})
@ScriptEngineScope
public interface ScriptEngineComponent {
    ScriptEngineManager manager();

    ScriptEngine engine();

    @Component.Builder
    interface Builder {
        ScriptEngineComponent build();

        // injects
        @BindsInstance
        Builder scriptEngineManager(ScriptEngineManager scriptEngineManager);
    }
}
