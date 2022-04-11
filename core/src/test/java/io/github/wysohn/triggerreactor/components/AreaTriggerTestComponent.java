package io.github.wysohn.triggerreactor.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerFactory;
import io.github.wysohn.triggerreactor.core.scope.PluginLifetime;
import io.github.wysohn.triggerreactor.core.script.interpreter.IExecutorMap;
import io.github.wysohn.triggerreactor.core.script.interpreter.IPlaceholderMap;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import modules.DummyLoggerModule;
import modules.SampleExternalAPIProtoModule;

@Component(modules = {DummyLoggerModule.class,
                      SampleExternalAPIProtoModule.class})
@PluginLifetime
public interface AreaTriggerTestComponent {
    AreaTriggerFactory factory();

    @Component.Builder
    interface Builder {
        AreaTriggerTestComponent build();

        @BindsInstance
        Builder gameController(IGameController gameController);

        @BindsInstance
        Builder taskSupervisor(TaskSupervisor taskSupervisor);

        @BindsInstance
        Builder pluginLifecycle(IPluginLifecycleController pluginLifecycle);

        @BindsInstance
        Builder throwableHandler(IThrowableHandler throwableHandler);

        @BindsInstance
        Builder scriptEngineProvider(IScriptEngineProvider scriptEngineProvider);

        @BindsInstance
        Builder selfReference(SelfReference selfReference);

        @BindsInstance
        Builder executorMap(IExecutorMap executorMap);

        @BindsInstance
        Builder placeholderMap(IPlaceholderMap placeholderMap);
    }
}
