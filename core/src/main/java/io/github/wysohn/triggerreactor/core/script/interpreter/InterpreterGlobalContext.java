package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.CaseInsensitiveStringMap;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

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
public class InterpreterGlobalContext {
    final Map<String, Placeholder> placeholderMap = new CaseInsensitiveStringMap<>();
    final Map<String, Executor> executorMap = new CaseInsensitiveStringMap<>();
    TaskSupervisor task;
    Map<Object, Object> gvars = new ConcurrentHashMap<>();
    SelfReference selfReference = new SelfReference() {
    };

    @Inject
    InterpreterGlobalContext() {

    }

    public static class Builder {
        private final InterpreterGlobalContext context = new InterpreterGlobalContext();

        private Builder() {

        }

        public Builder putPlaceholders(Map<String, Placeholder> map) {
            context.placeholderMap.putAll(map);
            return this;
        }

        public Builder putExecutors(Map<String, Executor> map) {
            context.executorMap.putAll(map);
            return this;
        }

        public Builder task(TaskSupervisor task) {
            context.task = task;
            return this;
        }

        public Builder putGlobalVariables(Map<Object, Object> map) {
            context.gvars.putAll(map);
            return this;
        }

        public Builder selfReference(SelfReference reference) {
            context.selfReference = reference;
            return this;
        }

        public InterpreterGlobalContext build() {
            ValidationUtil.notNull(context.task);
            ValidationUtil.notNull(context.selfReference);

            context.executorMap.put("STOP", (timing, localContext, vars, args) -> Executor.STOP);
            context.executorMap.put("BREAK", (timing, localContext, vars, args) -> Executor.BREAK);
            context.executorMap.put("CONTINUE", (timing, localContext, vars, args) -> Executor.CONTINUE);

            return context;
        }

        public static Builder begin() {
            return new Builder();
        }
    }
}
