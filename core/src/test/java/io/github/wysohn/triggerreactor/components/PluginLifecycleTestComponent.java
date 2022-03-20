package io.github.wysohn.triggerreactor.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import modules.DummyPluginLifecycleModule;

@Component(modules = DummyPluginLifecycleModule.class)
public interface PluginLifecycleTestComponent {
    IPluginLifecycleController controller();
}
