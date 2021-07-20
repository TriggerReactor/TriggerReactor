package io.github.wysohn.triggerreactor.core.script.interpreter;

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
    TaskSupervisor task;
    final Map<String, Executor> executorMap = new CaseInsensitiveStringMap<>();
    final Map<String, Placeholder> placeholderMap = new CaseInsensitiveStringMap<>();
    Map<Object, Object> gvars = new ConcurrentHashMap<>();
    SelfReference selfReference = new SelfReference() {
    };
    ProcessInterrupter interrupter = null;
}
