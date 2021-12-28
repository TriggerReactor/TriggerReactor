package js.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.scope.PluginScope;
import io.github.wysohn.triggerreactor.core.scope.ScriptEngineScope;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import js.modules.JSTestModule;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Set;

@Component(modules = JSTestModule.class)
@PluginScope
@ScriptEngineScope
public interface ScriptEngineComponent {
    IGameController gameController();

    IPluginLifecycleController pluginLifecycle();

    TaskSupervisor task();

    Set<IScriptEngineInitializer> initializers();

    ScriptEngineManager manager();

    ScriptEngine engine();

    @Component.Builder
    interface Builder {
        ScriptEngineComponent build();

        @BindsInstance
        Builder initializer(Set<IScriptEngineInitializer> initializer);
    }
}
