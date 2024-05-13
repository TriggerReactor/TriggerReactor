package io.github.wysohn.triggerreactor.core.script.interpreter.statement;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterExecutionUnit;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterException;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.interpreter.lambda.LambdaFunction;
import io.github.wysohn.triggerreactor.core.script.interpreter.lambda.LambdaParameter;
import io.github.wysohn.triggerreactor.core.script.parser.Node;

public class LambdaStatement implements InterpreterExecutionUnit {

    @Override
    public boolean isExclusive() {
        return false;
    }

    @Override
    public boolean isCallable(Token token) {
        return token.getType() == Token.Type.LAMBDA;
    }

    @Override
    public boolean evaluate(Interpreter interpreter, Node node, InterpreterLocalContext localContext) throws InterpreterException {
        if (node.getChildren().size() != 2)
            throw new InterpreterException(
                    "The LAMBDA node has " + node.getChildren().size() + " children instead of 2. " +
                            "Report this to us: " + node);

        Node parameters = node.getChildren().get(0);
        if (parameters.getToken().getType() != Token.Type.PARAMETERS)
            throw new InterpreterException("Expected parameters but found " + node);

        Node lambdaBody = node.getChildren().get(1);
        if (lambdaBody.getToken().getType() != Token.Type.LAMBDABODY)
            throw new InterpreterException("Expected lambda expression body but found " + node);

        LambdaParameter[] lambdaParameters = new LambdaParameter[parameters.getChildren().size()];
        for (int i = 0; i < lambdaParameters.length; i++) {
            Node idNode = parameters.getChildren().get(i);
            if (idNode.getToken().getType() != Token.Type.ID)
                throw new InterpreterException("Expected lambda parameter to be an id but found " + idNode);

            lambdaParameters[i] = new LambdaParameter(idNode);
        }

        localContext.pushToken(new Token(Token.Type.EPS,
                new LambdaFunction(lambdaParameters, lambdaBody, localContext, interpreter.globalContext),
                node.getToken()));

        return false;
    }
}
