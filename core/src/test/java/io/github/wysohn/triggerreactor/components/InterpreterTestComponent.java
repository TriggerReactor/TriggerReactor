package io.github.wysohn.triggerreactor.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.IResourceProvider;
import io.github.wysohn.triggerreactor.core.scope.PluginLifetime;
import io.github.wysohn.triggerreactor.core.script.interpreter.IExecutorMap;
import io.github.wysohn.triggerreactor.core.script.interpreter.IPlaceholderMap;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterGlobalContext;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import modules.DummyLoggerModule;
import modules.FakeDataFolderModule;

@Component(modules = {DummyLoggerModule.class, FakeDataFolderModule.class})
@PluginLifetime
public interface InterpreterTestComponent {
    InterpreterGlobalContext globalContext();

    @Component.Builder
    interface Builder {
        InterpreterTestComponent build();

        @BindsInstance
        Builder taskSupervisor(TaskSupervisor taskSupervisor);

        @BindsInstance
        Builder selfReference(SelfReference selfReference);

        @BindsInstance
        Builder wrapper(IWrapper wrapper);

        @BindsInstance
        Builder gameController(IGameController gameController);

        @BindsInstance
        Builder pluginLifecycle(IPluginLifecycleController pluginLifecycle);

        @BindsInstance
        Builder resourceProvider(IResourceProvider resourceProvider);

        @BindsInstance
        Builder placeholderMap(IPlaceholderMap placeholderMap);

        @BindsInstance
        Builder executorMap(IExecutorMap executorMap);
    }
}
