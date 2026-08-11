package io.github.wysohn.triggerreactor.core.script.interpreter.statement;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterExecutionUnit;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterException;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.parser.Node;

public class WhileLoopStatement implements InterpreterExecutionUnit {

    @Override
    public boolean isExclusive() {
        return false;
    }

    @Override
    public boolean isCallable(Token token) {
        return "WHILE".equals(token.value);
    }

    @Override
    public boolean evaluate(Interpreter interpreter, Node node, InterpreterLocalContext localContext) throws InterpreterException {
        long start = System.currentTimeMillis();

        Token resultToken = null;
        do {
            interpreter.next(node.getChildren().get(0), localContext);

            if (localContext.stackEmpty())
                throw new InterpreterException("Could not find condition for WHILE statement!");

            resultToken = localContext.popToken();

            if (interpreter.isVariable(resultToken)) {
                resultToken = interpreter.unwrapVariable(resultToken, localContext);
            }

            if (!(resultToken.value instanceof Boolean))
                throw new InterpreterException("Unexpected token for WHILE statement! -- " + resultToken);

            if ((boolean) resultToken.value) {
                interpreter.next(node.getChildren().get(1), localContext);
                if (localContext.isBreakFlag()) {
                    localContext.setBreakFlag(false);
                    break;
                }

                localContext.setBreakFlag(false);
                localContext.setContinueFlag(false);
            } else {
                break;
            }

            if (interpreter.globalContext.task.isServerThread()) {
                long timeTook = System.currentTimeMillis() - start;
                if (timeTook > 3000L)
                    throw new InterpreterException(
                            "WHILE loop took more than 3 seconds in Server Thread. This is usually "
                                    + "considered as 'too long' and can crash the server.");
            }
        } while (!localContext.isStopFlag());

        return false;
    }
}
