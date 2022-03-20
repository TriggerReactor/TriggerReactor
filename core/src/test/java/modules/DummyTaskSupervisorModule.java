package modules;

import dagger.Module;
import dagger.Provides;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;

import static org.mockito.Mockito.mock;

@Module
public abstract class DummyTaskSupervisorModule {
    @Provides
    static TaskSupervisor provideTaskSupervisor(){
        return mock(TaskSupervisor.class);
    }
}
