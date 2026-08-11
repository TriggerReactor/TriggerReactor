package io.github.wysohn.triggerreactor.core.script.interpreter.statement;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterExecutionUnit;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterException;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.parser.Node;

import java.lang.reflect.Array;

public class ForLoopStatement implements InterpreterExecutionUnit {

    @Override
    public boolean isExclusive() {
        return false;
    }

    @Override
    public boolean isCallable(Token token) {
        return "FOR".equals(token.value);
    }

    @Override
    public boolean evaluate(Interpreter interpreter, Node node, InterpreterLocalContext localContext) throws InterpreterException {
        interpreter.next(node.getChildren().get(0), localContext);


        if (localContext.isStopFlag())
            return true;
        Token idToken = localContext.popToken();

        if (idToken == null)
            throw new InterpreterException("Iteration variable for FOR statement not found!");

        if (node.getChildren().get(1).getToken().type != Token.Type.ITERATOR)
            throw new InterpreterException("Expected <ITERATOR> but found " + node.getChildren().get(1).getToken());
        Node iterNode = node.getChildren().get(1);

        if (iterNode.getChildren().size() == 1) {
            interpreter.next(iterNode.getChildren().get(0), localContext);

            if (localContext.isStopFlag())
                return true;
            Token valueToken = localContext.popToken();

            if (interpreter.isVariable(valueToken)) {
                valueToken = interpreter.unwrapVariable(valueToken, localContext);
            }

            if (!valueToken.isIterable())
                throw new InterpreterException(valueToken + " is not iterable!");

            if (valueToken.isArray()) {
                for (int i = 0; i < Array.getLength(valueToken.value); i++) {
                    Object obj = Array.get(valueToken.value, i);
                    if (localContext.isStopFlag())
                        break;

                    interpreter.assignValue(idToken, interpreter.parseValue(obj, valueToken), localContext);
                    interpreter.next(node.getChildren().get(2), localContext);
                    if (localContext.isBreakFlag()) {
                        localContext.setBreakFlag(false);
                        break;
                    }

                    localContext.setBreakFlag(false);
                    localContext.setContinueFlag(false);
                }
            } else {
                for (Object obj : (Iterable<?>) valueToken.value) {
                    if (localContext.isStopFlag())
                        break;

                    interpreter.assignValue(idToken, interpreter.parseValue(obj, valueToken), localContext);
                    interpreter.next(node.getChildren().get(2), localContext);
                    if (localContext.isBreakFlag()) {
                        localContext.setBreakFlag(false);
                        break;
                    }

                    localContext.setBreakFlag(false);
                    localContext.setContinueFlag(false);
                }
            }
        } else if (iterNode.getChildren().size() == 3) {
            // # Init
            Node initNode = iterNode.getChildren().get(0);
            interpreter.next(initNode, localContext);

            if (localContext.isStopFlag()) {
                return true;
            }

            Token initToken = localContext.popToken();
            if (interpreter.isVariable(initToken)) {
                initToken = interpreter.unwrapVariable(initToken, localContext);
            }

            if (!initToken.isInteger())
                throw new InterpreterException("Init value must be an Integer value! -- " + initToken);

            // # Bound
            final Node boundNode = iterNode.getChildren().get(1);
            interpreter.next(boundNode, localContext);

            if (localContext.isStopFlag()) {
                return true;
            }

            Token boundToken = localContext.popToken();
            if (interpreter.isVariable(boundToken)) {
                boundToken = interpreter.unwrapVariable(boundToken, localContext);
            }

            final String boundVal = (String) boundToken.getValue();
            final boolean inclusive = "<RANGE_INCLUSIVE>".equals(boundVal);
            if (!(inclusive || "<RANGE_EXCLUSIVE>".equals(boundVal))) {
                throw new InterpreterException("Range expression must be a '<RANGE_INCLUSIVE>' or '<RANGE_EXCLUSIVE>'. Actual is " + boundVal);
            }

            final int bound = inclusive ? 1 : 0;

            // # Limit
            Node limitNode = iterNode.getChildren().get(2);
            interpreter.next(limitNode, localContext);

            if (localContext.isStopFlag()) {
                return true;
            }

            Token limitToken = localContext.popToken();
            if (interpreter.isVariable(limitToken)) {
                limitToken = interpreter.unwrapVariable(limitToken, localContext);
            }

            if (!limitToken.isInteger()) {
                throw new InterpreterException("Limitation value must be an Integer value! Actual is " + limitToken);
            }

            final int start = initToken.toInteger();
            final int end = limitToken.toInteger();
            final boolean reversed = start > end;

            for (int i = start; ; ) {
                if (reversed && i <= end - bound) break;
                else if (!reversed && i >= end + bound) break;

                interpreter.assignValue(idToken, new Token(Token.Type.INTEGER, i, iterNode.getToken()), localContext);
                interpreter.next(node.getChildren().get(2), localContext);
                if (localContext.isBreakFlag()) {
                    localContext.setBreakFlag(false);
                    break;
                }

                localContext.setBreakFlag(false);
                localContext.setContinueFlag(false);

                if (reversed) i--;
                else i++;
            }
        } else {
            throw new InterpreterException("Number of <ITERATOR> must be 1 or 2!");
        }

        return false;
    }
}
