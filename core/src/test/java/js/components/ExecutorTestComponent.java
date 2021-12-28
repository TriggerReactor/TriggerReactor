package js.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.ExecutorManager;
import js.modules.ExecutorTestModule;
import js.scope.JsTestScope;

@Component(modules = {ExecutorTestModule.class},
           dependencies = {ScriptEngineComponent.class})
@JsTestScope
public interface ExecutorTestComponent {
    ExecutorManager executorManager();

    @Component.Builder
    interface Builder {
        ExecutorTestComponent build();

        Builder engineComponent(ScriptEngineComponent component);

        @BindsInstance
        Builder wrapper(IWrapper wrapper);
    }
}
