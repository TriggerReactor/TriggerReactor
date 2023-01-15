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
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.CaseInsensitiveStringMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The globally shared states
 *
 * This can be concurrently accessed by multiple threads, so
 * these variables must be accessed in a thread-safe way (or read only),
 * or there can be race-condition (or concurrent modification exception) occur.
 */
public class InterpreterGlobalContext {
    private final Executor EXECUTOR_STOP = (timing, vars, context, args) -> Executor.STOP;
    private final Executor EXECUTOR_BREAK = (timing, vars, context, args) -> Executor.BREAK;
    private final Executor EXECUTOR_CONTINUE = (timing, vars, context, args) -> Executor.CONTINUE;

    TaskSupervisor task;
    final Map<String, Executor> executorMap = new CaseInsensitiveStringMap<Executor>(){{
        put("STOP", EXECUTOR_STOP);
        put("BREAK", EXECUTOR_BREAK);
        put("CONTINUE", EXECUTOR_CONTINUE);
    }};
    final Map<String, Placeholder> placeholderMap = new CaseInsensitiveStringMap<>();
    final Map<Object, Object> gvars = new ConcurrentHashMap<>();
    SelfReference selfReference = new SelfReference() {
    };
    ProcessInterrupter interrupter = null;
    public IExceptionHandle exceptionHandle;
}
