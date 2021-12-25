package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.CaseInsensitiveStringMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The globally shared states
 * <p>
 * This can be concurrently accessed by multiple threads, so
 * these variables must be accessed in a thread-safe way (or read only),
 * or there can be race-condition (or concurrent modification exception) occur.
 */
public class InterpreterGlobalContext {
    final Map<String, Placeholder> placeholderMap = new CaseInsensitiveStringMap<>();
    private final Executor EXECUTOR_STOP = (timing, vars, context, args) -> Executor.STOP;
    private final Executor EXECUTOR_BREAK = (timing, vars, context, args) -> Executor.BREAK;
    private final Executor EXECUTOR_CONTINUE = (timing, vars, context, args) -> Executor.CONTINUE;
    final Map<String, Executor> executorMap = new CaseInsensitiveStringMap<Executor>() {{
        put("STOP", EXECUTOR_STOP);
        put("BREAK", EXECUTOR_BREAK);
        put("CONTINUE", EXECUTOR_CONTINUE);
    }};
    TaskSupervisor task;
    Map<Object, Object> gvars = new ConcurrentHashMap<>();
    SelfReference selfReference = new SelfReference() {
    };
    ProcessInterrupter interrupter = null;
}
