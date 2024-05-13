package io.github.wysohn.triggerreactor.core.script.interpreter.statement;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.Token.Type;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterExecutionUnit;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterException;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.parser.Node;

public class IfConditionalStatement implements InterpreterExecutionUnit {

    @Override
    public boolean isExclusive() {
        return false;
    }

    @Override
    public boolean isCallable(Token token) {
        return "ELSEIF".equals(token.value) || "IF".equals(token.value);
    }

    @Override
    public boolean evaluate(Interpreter interpreter, Node node, InterpreterLocalContext localContext) throws InterpreterException {
        interpreter.next(node.getChildren().get(0), localContext);//[0] condition
        if (localContext.isStopFlag())
            return true;

        Token resultToken = localContext.popToken();

        if (interpreter.isVariable(resultToken)) {
            resultToken = interpreter.unwrapVariable(resultToken, localContext);
        }

        if (resultToken.type == Type.NULLVALUE) { // null check failed
            if (node.getChildren().size() > 2) {
                interpreter.next(node.getChildren().get(2), localContext);
            }
        } else { // normal IF statement
            if (resultToken.isBoolean()) {
                boolean result = (boolean) resultToken.value;
                if (result) {
                    interpreter.next(node.getChildren().get(1), localContext);//[1] true body
                } else if (node.getChildren().size() > 2) {
                    interpreter.next(node.getChildren().get(2), localContext);//[2] false body
                }
            } else if (resultToken.isInteger()) {
                int value = resultToken.toInteger();
                if (value != 0) {
                    interpreter.next(node.getChildren().get(1), localContext);
                } else if (node.getChildren().size() > 2) {
                    interpreter.next(node.getChildren().get(2), localContext);
                }
            } else if (resultToken.isDecimal()) {
                double value = resultToken.toDecimal();
                if (value != 0.0) {
                    interpreter.next(node.getChildren().get(1), localContext);
                } else if (node.getChildren().size() > 2) {
                    interpreter.next(node.getChildren().get(2), localContext);
                }
            } else if (resultToken.value != null) {//always true if not null
                interpreter.next(node.getChildren().get(1), localContext);
            } else {
                throw new InterpreterException("Unexpected token for IF statement! -- " + resultToken);
            }
        }

        return false;
    }
}
