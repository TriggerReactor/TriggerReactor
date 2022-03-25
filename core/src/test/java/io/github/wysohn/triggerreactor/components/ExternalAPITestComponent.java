package io.github.wysohn.triggerreactor.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.ExternalAPIManager;
import io.github.wysohn.triggerreactor.core.scope.PluginLifetime;
import modules.DummyLoggerModule;
import modules.SampleExternalAPIProtoModule;

@Component(modules = {DummyLoggerModule.class,
                      SampleExternalAPIProtoModule.class,},
           dependencies = {PluginLifecycleTestComponent.class})
@PluginLifetime
public interface ExternalAPITestComponent {
    ExternalAPIManager manager();
}
