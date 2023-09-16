/*
 * Copyright (C) 2023. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.main.IGameManagement;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.IGlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.SharedVariableManager;
import io.github.wysohn.triggerreactor.core.manager.js.executor.ExecutorManager;
import io.github.wysohn.triggerreactor.core.manager.js.placeholder.PlaceholderManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerDependencyFacade;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterGlobalContext;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;

import static org.mockito.Mockito.mock;

public class TestTriggerDependencyModule extends AbstractModule {
    private SharedVariableManager sharedVariableManager;
    private ExecutorManager executorManager;
    private PlaceholderManager placeholderManager;
    private IGlobalVariableManager IGlobalVariableManager;
    private IPluginManagement pluginManagement;
    private IGameManagement gameManagement;
    private TaskSupervisor taskSupervisor;
    private IExceptionHandle exceptionHandle;
    private InterpreterGlobalContext globalContext;
    private ITriggerDependencyFacade triggerDependencyFacade;

    private TestTriggerDependencyModule() {

    }

    @Provides
    public SharedVariableManager provideSharedVariableManager() {
        return sharedVariableManager;
    }

    @Provides
    public ExecutorManager provideExecutorManager() {
        return executorManager;
    }

    @Provides
    public PlaceholderManager providePlaceholderManager() {
        return placeholderManager;
    }

    @Provides
    public IGlobalVariableManager provideGlobalVariableManager() {
        return IGlobalVariableManager;
    }

    @Provides
    public IPluginManagement providePluginManagement() {
        return pluginManagement;
    }

    @Provides
    public IGameManagement provideGameManagement() {
        return gameManagement;
    }

    @Provides
    public TaskSupervisor provideTaskSupervisor() {
        return taskSupervisor;
    }

    @Provides
    public IExceptionHandle provideExceptionHandle() {
        return exceptionHandle;
    }

    @Provides
    public InterpreterGlobalContext provideGlobalContext() {
        return globalContext;
    }

    @Provides
    public ITriggerDependencyFacade provideTriggerDependencyFacade() {
        return triggerDependencyFacade;
    }

    public static class Builder {
        // fill with default mocks
        private SharedVariableManager sharedVariableManager = mock(SharedVariableManager.class);
        private ExecutorManager executorManager = mock(ExecutorManager.class);
        private PlaceholderManager placeholderManager = mock(PlaceholderManager.class);
        private IGlobalVariableManager IGlobalVariableManager = mock(GlobalVariableManager.class);
        private IPluginManagement pluginManagement = mock(IPluginManagement.class);
        private IGameManagement gameManagement = mock(IGameManagement.class);
        private TaskSupervisor taskSupervisor = mock(TaskSupervisor.class);
        private IExceptionHandle exceptionHandle = mock(IExceptionHandle.class);
        private InterpreterGlobalContext globalContext = mock(InterpreterGlobalContext.class);
        private ITriggerDependencyFacade triggerDependencyFacade = mock(ITriggerDependencyFacade.class);

        private Builder() {

        }

        public Builder sharedVariableManager(SharedVariableManager sharedVariableManager) {
            this.sharedVariableManager = sharedVariableManager;
            return this;
        }

        public Builder executorManager(ExecutorManager executorManager) {
            this.executorManager = executorManager;
            return this;
        }

        public Builder placeholderManager(PlaceholderManager placeholderManager) {
            this.placeholderManager = placeholderManager;
            return this;
        }

        public Builder globalVariableManager(IGlobalVariableManager IGlobalVariableManager) {
            this.IGlobalVariableManager = IGlobalVariableManager;
            return this;
        }

        public Builder pluginManagement(IPluginManagement pluginManagement) {
            this.pluginManagement = pluginManagement;
            return this;
        }

        public Builder gameManagement(IGameManagement gameManagement) {
            this.gameManagement = gameManagement;
            return this;
        }

        public Builder taskSupervisor(TaskSupervisor taskSupervisor) {
            this.taskSupervisor = taskSupervisor;
            return this;
        }

        public Builder exceptionHandle(IExceptionHandle exceptionHandle) {
            this.exceptionHandle = exceptionHandle;
            return this;
        }

        public Builder globalContext(InterpreterGlobalContext globalContext) {
            this.globalContext = globalContext;
            return this;
        }

        public Builder triggerDependencyFacade(ITriggerDependencyFacade triggerDependencyFacade) {
            this.triggerDependencyFacade = triggerDependencyFacade;
            return this;
        }

        public TestTriggerDependencyModule build() {
            TestTriggerDependencyModule module = new TestTriggerDependencyModule();
            module.sharedVariableManager = sharedVariableManager;
            module.executorManager = executorManager;
            module.placeholderManager = placeholderManager;
            module.IGlobalVariableManager = IGlobalVariableManager;
            module.pluginManagement = pluginManagement;
            module.gameManagement = gameManagement;
            module.taskSupervisor = taskSupervisor;
            module.exceptionHandle = exceptionHandle;
            module.globalContext = globalContext;
            module.triggerDependencyFacade = triggerDependencyFacade;
            return module;
        }

        public static Builder begin() {
            return new Builder();
        }
    }
}
