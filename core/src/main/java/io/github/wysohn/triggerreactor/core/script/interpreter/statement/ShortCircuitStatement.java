package io.github.wysohn.triggerreactor.core.script.interpreter.statement;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterExecutionUnit;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterException;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.parser.Node;

public class ShortCircuitStatement implements InterpreterExecutionUnit {

    @Override
    public boolean isExclusive() {
        return true;
    }

    @Override
    public boolean isCallable(Token token) {
        return "&&".equals(token.value) || "||".equals(token.value);
    }

    @Override
    public boolean evaluate(Interpreter interpreter, Node node, InterpreterLocalContext localContext) throws InterpreterException {
        if ("&&".equals(node.getToken().value)) {
            Token leftBool = localContext.popToken();
            if (interpreter.isVariable(leftBool)) {
                leftBool = interpreter.unwrapVariable(leftBool, localContext);
            }
            localContext.pushToken(leftBool);

            if (!leftBool.isBoolean())
                throw new InterpreterException("Left of && operator should be Boolean but was " + leftBool);

            boolean result = leftBool.toBoolean();

            if (!result) { //false anyway
                return true;
            }
        } else if ("||".equals(node.getToken().value)) {
            Token leftBool = localContext.popToken();
            if (interpreter.isVariable(leftBool)) {
                leftBool = interpreter.unwrapVariable(leftBool, localContext);
            }
            localContext.pushToken(leftBool);

            if (!leftBool.isBoolean())
                throw new InterpreterException("Left of || operator should be Boolean but was " + leftBool);

            boolean result = leftBool.toBoolean();

            if (result) { //true anyway
                return true;
            }
        }

        return false;
    }
}
