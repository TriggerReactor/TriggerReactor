package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.manager.ExternalAPIManager;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterGlobalContext;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;

import java.util.Map;

@Component
public interface CommandTriggerTestComponent {
    CommandTriggerFactory factory();

    @Component.Builder
    interface Builder {
        CommandTriggerTestComponent build();

        @BindsInstance
        Builder externalAPI(ExternalAPIManager externalAPI);

        @BindsInstance
        Builder globalContext(InterpreterGlobalContext globalContext);

        @BindsInstance
        Builder gameController(IGameController gameController);

        @BindsInstance
        Builder taskSupervisor(TaskSupervisor taskSupervisor);

        @BindsInstance
        Builder throwableHandler(IThrowableHandler throwableHandler);

        @BindsInstance
        Builder scriptEngineProvider(IScriptEngineProvider scriptEngineProvider);

        @BindsInstance
        Builder tabCompleterMap(Map<String, DynamicTabCompleter> map);
    }
}
