package io.github.wysohn.triggerreactor.core.script.interpreter.interrupt;

import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.parser.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * Interruption logic for the Interpreter.
 * <p>
 * Any component of it must be stateless and provide logics only as
 * it is used by multiple triggers concurrently.
 */
public class ProcessInterrupter implements IPerExecutorInterrupter, IPerNodeInterrupter, IPerPlaceholderInterrupter {
    private List<IPerExecutorInterrupter> executorInterrupterList = new LinkedList<>();
    private List<IPerNodeInterrupter> nodeInterrupterList = new LinkedList<>();
    private List<IPerPlaceholderInterrupter> placeholderInterrupterList = new LinkedList<>();

    private ProcessInterrupter() {

    }

    @Override
    public boolean onCommand(InterpreterLocalContext context, String command, Object[] args) {
        for (IPerExecutorInterrupter iPerExecutorInterrupter : executorInterrupterList) {
            if (iPerExecutorInterrupter.onCommand(context, command, args))
                return true;
        }
        return false;
    }

    @Override
    public boolean onNodeProcess(InterpreterLocalContext localContext, Node node) {
        for (IPerNodeInterrupter iPerNodeInterrupter : nodeInterrupterList) {
            if (iPerNodeInterrupter.onNodeProcess(localContext, node))
                return true;
        }
        return false;
    }

    @Override
    public Object onPlaceholder(InterpreterLocalContext context, String placeholder, Object[] args) {
        for (IPerPlaceholderInterrupter iPerPlaceholderInterrupter : placeholderInterrupterList) {
            Object result = iPerPlaceholderInterrupter.onPlaceholder(context, placeholder, args);
            if (result != null)
                return result;
        }
        return null;
    }

    public static class Builder {
        private final ProcessInterrupter interrupter = new ProcessInterrupter();

        private Builder() {

        }

        public ProcessInterrupter build() {
            return interrupter;
        }

        public Builder perExecutor(IPerExecutorInterrupter add) {
            interrupter.executorInterrupterList.add(add);
            return this;
        }

        public Builder perNode(IPerNodeInterrupter add) {
            interrupter.nodeInterrupterList.add(add);
            return this;
        }

        public Builder perPlaceholder(IPerPlaceholderInterrupter add) {
            interrupter.placeholderInterrupterList.add(add);
            return this;
        }

        public static Builder begin() {
            return new Builder();
        }
    }
}
