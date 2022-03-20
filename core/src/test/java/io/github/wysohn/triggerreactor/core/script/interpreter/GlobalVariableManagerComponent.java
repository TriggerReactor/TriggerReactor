package io.github.wysohn.triggerreactor.core.script.interpreter;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import modules.DummyLoggerModule;
import modules.FakeDataFolderModule;

@Component(modules = {DummyLoggerModule.class, FakeDataFolderModule.class, GlobalVariableSettingsModule.class,})
public interface GlobalVariableManagerComponent {
    GlobalVariableManager globalVariableManager();
}
