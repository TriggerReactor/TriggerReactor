package io.github.wysohn.triggerreactor.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.modules.ConfigSourceFactoryModule;
import modules.DummyLoggerModule;
import modules.FakeDataFolderModule;

@Component(modules = {DummyLoggerModule.class,
                      FakeDataFolderModule.class,
                      ConfigSourceFactoryModule.class})
public interface GlobalVariableManagerTestComponent {
    GlobalVariableManager getGlobalVariableManager();
}
