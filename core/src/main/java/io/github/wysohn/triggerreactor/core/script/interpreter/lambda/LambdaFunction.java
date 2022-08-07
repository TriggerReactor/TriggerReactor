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

    public LambdaFunction(LambdaParameter[] parameters,
                          Node body,
                          InterpreterLocalContext localContext,
                          InterpreterGlobalContext globalContext){
        this.parameters = parameters;
        this.body = body;
        this.lambdaBody = new Interpreter(body, localContext.copyState("LAMBDA"), globalContext);

        // if duplicated variable name is found, parameter name always has priority
        for (LambdaParameter parameter : parameters) {
            this.lambdaBody.getVars().put(parameter.id, parameter.defValue);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int argsLength = args == null ? 0 : args.length;

        if(parameters.length != argsLength)
            throw new InterpreterException("Number of Lambda parameters doesn't match. Caller provided "+args.length+"" +
                    " arguments, yet the LAMBDA only has "+parameters.length+" ids. "+body);

        // initialize arguments as variables in the lambda
        for (int i = 0; i < parameters.length; i++) {
            lambdaBody.getVars().put(parameters[i].id, args[i]);
        }

        lambdaBody.start();

        return lambdaBody.result();
    }
}
