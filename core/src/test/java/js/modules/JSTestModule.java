package js.modules;

import dagger.Module;
import dagger.Provides;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.modules.CoreScriptEngineManagerModule;
import io.github.wysohn.triggerreactor.core.scope.PluginScope;
import io.github.wysohn.triggerreactor.core.scope.ScriptEngineScope;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import modules.DummyLoggerModule;
import modules.FakeDataFolderModule;

import javax.script.ScriptEngineManager;

import static org.mockito.Mockito.mock;

@Module(includes = {DummyLoggerModule.class, FakeDataFolderModule.class, CoreScriptEngineManagerModule.class})
public abstract class JSTestModule {
    @Provides
    @ScriptEngineScope
    static ScriptEngineManager engineManager(){
        return new ScriptEngineManager();
    }

    @Provides
    @PluginScope
    static IGameController gameController() {
        return mock(IGameController.class);
    }

    @Provides
    @PluginScope
    static IPluginLifecycleController pluginLifecycleController() {
        return mock(IPluginLifecycleController.class);
    }

    @Provides
    @PluginScope
    static TaskSupervisor taskSupervisor() {
        return mock(TaskSupervisor.class);
    }
}
