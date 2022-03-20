package io.github.wysohn.triggerreactor.core.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.modules.CoreScriptEngineInitializerModule;

import java.util.Set;

@Component(modules = CoreScriptEngineInitializerModule.class,
           dependencies = PluginMainComponent.class)
public interface ScriptEngineInitializerComponent {
    Set<IScriptEngineInitializer> initializers();
}
