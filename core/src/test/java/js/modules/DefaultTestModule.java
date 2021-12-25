package js.modules;

import dagger.Module;
import dagger.Provides;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import modules.DummyLoggerModule;
import modules.FakeDataFolderModule;

import javax.script.ScriptEngineManager;

import static org.mockito.Mockito.mock;

@Module(includes = {DummyLoggerModule.class, FakeDataFolderModule.class,})
public abstract class DefaultTestModule {
    @Provides
    static IGameController gameController() {
        return mock(IGameController.class);
    }

    @Provides
    static IPluginLifecycleController pluginLifecycleController() {
        return mock(IPluginLifecycleController.class);
    }

    @Provides
    static TaskSupervisor taskSupervisor() {
        return mock(TaskSupervisor.class);
    }

    @Provides
    static ScriptEngineManager scriptEngineManager() {
        return new ScriptEngineManager();
    }
}
