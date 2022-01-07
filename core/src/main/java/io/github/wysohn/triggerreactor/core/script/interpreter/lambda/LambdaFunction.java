package io.github.wysohn.triggerreactor.core.script.interpreter.lambda;

import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterException;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterGlobalContext;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.parser.Node;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class LambdaFunction implements InvocationHandler {
    private final LambdaParameter[] parameters;
    private final Node body;
    private final Interpreter lambdaBody;
    private final InterpreterLocalContext localContext;
    private final InterpreterGlobalContext globalContext;

    public LambdaFunction(LambdaParameter[] parameters,
                          Node body,
                          InterpreterLocalContext localContext,
                          InterpreterGlobalContext globalContext) {
        this.parameters = parameters;
        this.body = body;
        this.lambdaBody = new Interpreter(body);

        this.localContext = localContext;
        this.globalContext = globalContext;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int argsLength = args == null ? 0 : args.length;

        if (parameters.length != argsLength)
            throw new InterpreterException(
                    "Number of Lambda parameters doesn't match. Caller provided " + args.length + " arguments, yet the LAMBDA only has " + parameters.length + " ids. " + body);

        // copy the local context
        InterpreterLocalContext copiedContext = this.localContext.copyState("LAMBDA");

        // if duplicated variable name is found, parameter name always has priority
        for (LambdaParameter parameter : parameters) {
            copiedContext.setVar(parameter.id, parameter.defValue);
        }

        // initialize arguments as variables in the lambda
        for (int i = 0; i < parameters.length; i++) {
            copiedContext.setVar(parameters[i].id, args[i]);
        }

        lambdaBody.start(copiedContext, globalContext);

        return lambdaBody.result();
    }
}
