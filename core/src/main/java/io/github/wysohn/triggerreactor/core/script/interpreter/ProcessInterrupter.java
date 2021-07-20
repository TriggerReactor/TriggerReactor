package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.script.parser.Node;

public interface ProcessInterrupter {
    /**
     * This will be called every time when a node is processing.
     *
     * @param node the current node
     * @return return true will terminate execution
     */
    boolean onNodeProcess(Node node);

    /**
     * @param context
     * @param args
     * @boolean true if consumed it; false to let interpreter continue working on it.
     */
    boolean onCommand(Object context, String command, Object[] args);

    /**
     * To process placeholders that need information of Triggers
     *
     * @param context
     * @param placeholder
     * @param args
     */
    Object onPlaceholder(Object context, String placeholder, Object[] args);
}
