package io.github.wysohn.triggerreactor.core.script.interpreter.statement;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterExecutionUnit;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterException;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.parser.Node;

public class TryCatchStatement implements InterpreterExecutionUnit {

    @Override
    public boolean isExclusive() {
        return false;
    }

    @Override
    public boolean isCallable(Token token) {
        return "TRY".equals(token.value);
    }

    @Override
    public boolean evaluate(Interpreter interpreter, Node node, InterpreterLocalContext localContext) throws InterpreterException {
        if (node.getChildren().size() == 2 || node.getChildren().size() == 3) {
            try {
                interpreter.next(node.getChildren().get(0), localContext);
            } catch (Throwable e) {
                if (node.getChildren().get(1).getToken().type == Token.Type.CATCHBODY) {
                    Node catchBody = node.getChildren().get(1);

                    interpreter.next(catchBody.getChildren().get(0), localContext);

                    Throwable throwable = e;

                    Token idToken = localContext.popToken();
                    Token valueToken = new Token(Token.Type.OBJECT, throwable);

                    while ((throwable = throwable.getCause()) != null) {
                        valueToken = new Token(Token.Type.OBJECT, throwable);
                    }

                    interpreter.assignValue(idToken, valueToken, localContext);

                    interpreter.next(catchBody.getChildren().get(1), localContext);
                } else {
                    throw e;
                }
            } finally {
                if ((node.getChildren().size() == 2
                        && node.getChildren().get(1).getToken().type == Token.Type.FINALLYBODY)) {
                    interpreter.next(node.getChildren().get(1), localContext);
                } else if (node.getChildren().size() == 3
                        && node.getChildren().get(2).getToken().type == Token.Type.FINALLYBODY) {
                    interpreter.next(node.getChildren().get(2), localContext);
                }
            }
        } else if (node.getChildren().size() == 1) {
            throw new InterpreterException("Expected CATCH or FINALLY statement! -- " + node.getToken());
        } else {
            throw new InterpreterException("Unexpected token for TRY statement! -- " + node.getToken());
        }

        return false;
    }
}
