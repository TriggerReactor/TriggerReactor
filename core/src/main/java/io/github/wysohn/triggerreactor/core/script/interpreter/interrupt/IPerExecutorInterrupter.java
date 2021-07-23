package io.github.wysohn.triggerreactor.core.script.interpreter.interrupt;

import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;

public interface IPerExecutorInterrupter {
    /**
     * Intercept the interpreter process by altering the Executor behavior.
     *
     * @param context the local context of the interpreter.
     * @param command name of the executor
     * @param args    arguments provided to the executor
     * @return true to stop the interpretation immediately; false to let interpreter continue working on it.
     */
    boolean onCommand(InterpreterLocalContext context, String command, Object[] args);
}
