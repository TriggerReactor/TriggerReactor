package io.github.wysohn.triggerreactor.core.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.modules.CoreScriptEngineInitializerModule;
import io.github.wysohn.triggerreactor.core.scope.PostPluginLifetime;

import java.util.Set;

@Component(modules = CoreScriptEngineInitializerModule.class)
@PostPluginLifetime
public interface ScriptEngineInitializerComponent {
    Set<IScriptEngineInitializer> initializers();
}
