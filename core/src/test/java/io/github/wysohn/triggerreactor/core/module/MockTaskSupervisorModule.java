package io.github.wysohn.triggerreactor.core.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;

import static org.mockito.Mockito.mock;

public class MockTaskSupervisorModule extends AbstractModule {
    private final TaskSupervisor taskSupervisor;

    public MockTaskSupervisorModule(TaskSupervisor taskSupervisor) {
        this.taskSupervisor = taskSupervisor;
    }

    @Provides
    public TaskSupervisor provideTaskSupervisor() {
        return mock(TaskSupervisor.class);
    }
}
