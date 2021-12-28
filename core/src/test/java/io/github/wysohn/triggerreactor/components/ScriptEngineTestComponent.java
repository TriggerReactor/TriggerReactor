package io.github.wysohn.triggerreactor.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.modules.CoreScriptEngineInitializerModule;
import io.github.wysohn.triggerreactor.core.scope.ScriptEngineInitScope;
import modules.DummyWrapperModule;
import modules.TestScriptEngineModule;

import javax.inject.Singleton;

@Component(modules = {CoreScriptEngineInitializerModule.class, TestScriptEngineModule.class, DummyWrapperModule.class})
@Singleton
@ScriptEngineInitScope
public interface ScriptEngineTestComponent {
    IScriptEngineProvider scriptEngineProvider();
}
