package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.parser.Node;

public interface InterpreterExecutionUnit {

    /**
     * If not used at the beginning of the next method, but used individually, please return true.
     *
     * @return
     */
    boolean isExclusive();

    /**
     * Identification condition of Statements
     *
     * @param token
     * @return
     */
    boolean isCallable(Token token);

    /**
     * Execute the Statement.
     *
     * @param interpreter running interpreter instance
     * @param node
     * @param localContext
     * @return Whether the following processes stop or proceed. (default: false)
     * @throws InterpreterException
     */
    boolean evaluate(Interpreter interpreter, Node node, InterpreterLocalContext localContext) throws InterpreterException;
}
