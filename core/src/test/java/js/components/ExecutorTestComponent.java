package js.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager;
import js.modules.ExecutorTestModule;

@Component(modules = ExecutorTestModule.class)
public interface ExecutorTestComponent {
    AbstractExecutorManager executorManager();
}
