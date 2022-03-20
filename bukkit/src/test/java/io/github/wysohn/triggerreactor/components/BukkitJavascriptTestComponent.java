package io.github.wysohn.triggerreactor.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.ExecutorManager;
import io.github.wysohn.triggerreactor.core.manager.PlaceholderManager;
import io.github.wysohn.triggerreactor.core.modules.CoreScriptEngineInitializerModule;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import modules.DummyLoggerModule;
import modules.FakeDataFolderModule;

import javax.script.ScriptEngineManager;

@Component(modules = {CoreScriptEngineInitializerModule.class,
                      DummyLoggerModule.class,
                      FakeDataFolderModule.class})
public interface BukkitJavascriptTestComponent {
    ExecutorManager executorManager();

    PlaceholderManager placeholderManager();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder scriptEngineManager(ScriptEngineManager scriptEngineManager);

        @BindsInstance
        Builder gameController(IGameController gameController);

        @BindsInstance
        Builder pluginLifecycle(IPluginLifecycleController pluginLifecycle);

        @BindsInstance
        Builder taskSupervisor(TaskSupervisor taskSupervisor);

        @BindsInstance
        Builder wrapper(IWrapper wrapper);

        BukkitJavascriptTestComponent build();
    }
}
