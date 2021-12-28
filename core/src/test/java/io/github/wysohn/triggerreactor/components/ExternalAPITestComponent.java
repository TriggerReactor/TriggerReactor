package io.github.wysohn.triggerreactor.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.ExternalAPIManager;
import io.github.wysohn.triggerreactor.core.scope.APIScope;
import modules.DummyLoggerModule;
import modules.SampleExternalAPIProtoModule;

@Component(modules = {DummyLoggerModule.class, SampleExternalAPIProtoModule.class,},
           dependencies = {PluginLifecycleTestComponent.class})
@APIScope
public interface ExternalAPITestComponent {
    ExternalAPIManager manager();
}
