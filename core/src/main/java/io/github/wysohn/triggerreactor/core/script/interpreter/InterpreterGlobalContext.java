package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.scope.PluginLifetime;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The globally shared states
 * <p>
 * This can be concurrently accessed by multiple threads, so
 * these variables must be accessed in a thread-safe way (or read only),
 * or there can be race-condition (or concurrent modification exception) occur.
 */
@PluginLifetime
public class InterpreterGlobalContext {
    @Inject
    TaskSupervisor task;
    @Inject
    SelfReference selfReference;

    @Inject
    IExecutorMap executorMap;
    @Inject
    IPlaceholderMap placeholderMap;

    Map<Object, Object> gvars = new ConcurrentHashMap<>();

    @Inject
    InterpreterGlobalContext() {

    }
}
