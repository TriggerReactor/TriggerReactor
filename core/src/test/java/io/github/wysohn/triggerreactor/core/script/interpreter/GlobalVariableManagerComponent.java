package io.github.wysohn.triggerreactor.core.script.interpreter;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.scope.PluginScope;
import modules.DummyLoggerModule;
import modules.FakeDataFolderModule;

@Component(modules = {
        DummyLoggerModule.class,
        FakeDataFolderModule.class,
        GlobalVariableSettingsModule.class,
})
@PluginScope
public interface GlobalVariableManagerComponent {
    GlobalVariableManager globalVariableManager();
}
