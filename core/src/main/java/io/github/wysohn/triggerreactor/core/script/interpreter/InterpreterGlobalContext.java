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

package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.manager.IGlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.js.IBackedMapProvider;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.CaseInsensitiveStringMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * The globally shared states
 * <p>
 * This can be concurrently accessed by multiple threads, so
 * these variables must be accessed in a thread-safe way (or read only),
 * or there can be race-condition (or concurrent modification exception) occur.
 */
@Singleton
public class InterpreterGlobalContext {
    @Inject
    public TaskSupervisor task;
    @Inject
    SelfReference selfReference;
    @Inject
    public IExceptionHandle exceptionHandle;

    final Map<String, Executor> executorMap;
    final Map<String, Placeholder> placeholderMap;
    final Map<Object, Object> gvars;

    @Inject
    private InterpreterGlobalContext(IBackedMapProvider<Executor> executorManager,
                                     IBackedMapProvider<Placeholder> placeholderManager,
                                     IGlobalVariableManager IGlobalVariableManager) {
        executorMap = DelegatingExecutorMap.wrap(executorManager.getBackedMap());
        placeholderMap = placeholderManager.getBackedMap();
        gvars = IGlobalVariableManager.getGlobalVariableAdapter();
    }

    /**
     * Backed maps are immutable, so we must proxy the map to allow
     * pre-defined executors to be used.
     */
    private static class DelegatingExecutorMap extends CaseInsensitiveStringMap<Executor> {
        private final Map<String, Executor> delegate;

        DelegatingExecutorMap(Map<String, Executor> delegate) {
            this.delegate = delegate;

            put("STOP", (timing1, vars1, context1, args1) -> Executor.STOP);
            put("BREAK", (timing1, vars1, context1, args1) -> Executor.BREAK);
            put("CONTINUE", (timing, vars, context, args) -> Executor.CONTINUE);
        }

        @Override
        public Executor get(Object key) {
            if (super.containsKey(key))
                return super.get(key);
            return delegate.get(key);
        }

        @Override
        public boolean containsKey(Object key) {
            return super.containsKey(key) || delegate.containsKey(key);
        }

        static DelegatingExecutorMap wrap(Map<String, Executor> delegate) {
            return new DelegatingExecutorMap(delegate);
        }
    }
}
