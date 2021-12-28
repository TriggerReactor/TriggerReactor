package io.github.wysohn.triggerreactor.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.scope.PluginScope;
import modules.DummyPluginLifecycleModule;

@Component(modules = DummyPluginLifecycleModule.class)
@PluginScope
public interface PluginLifecycleTestComponent {
    IPluginLifecycleController controller();
}
