package io.github.wysohn.triggerreactor.core.script.interpreter.interrupt;

import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;

public interface IPerPlaceholderInterrupter {
    /**
     * This doesn't necessarily interrupt the interpreter, but it can be used to alter
     * the behavior or the placeholder that the interpreter is working on.
     *
     * @param context     the local context of the interpreter.
     * @param placeholder name of the placeholder
     * @param args        arguments provided to the placeholder
     * @return if not null, the placeholder value will be replaced to this value; if null,
     * it will follow the normal behavior (use the regular placeholder if exist).
     */
    Object onPlaceholder(InterpreterLocalContext context, String placeholder, Object[] args);
}
