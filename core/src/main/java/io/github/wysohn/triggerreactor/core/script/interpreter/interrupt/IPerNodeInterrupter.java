package io.github.wysohn.triggerreactor.core.script.interpreter.interrupt;

import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.parser.Node;

public interface IPerNodeInterrupter {
    /**
     * Stop or continue the interpretation based on the given context and the node
     * that the interpreter is currently traversing
     *
     * @param node the current node
     * @return true to stop the interpretation immediately; false to let interpreter continue working on it.
     */
    boolean onNodeProcess(InterpreterLocalContext localContext, Node node);
}
