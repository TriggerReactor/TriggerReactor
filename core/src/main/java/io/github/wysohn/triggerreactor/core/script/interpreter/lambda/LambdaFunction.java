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
    private final InterpreterLocalContext localContext;
    private final InterpreterGlobalContext globalContext;

    public LambdaFunction(
        final LambdaParameter[] parameters,
        final Node body,
        final InterpreterLocalContext localContext,
        final InterpreterGlobalContext globalContext
    ) {
        this.parameters = parameters;
        this.body = body;
        this.localContext = localContext;
        this.globalContext = globalContext;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final int argsLength = args == null ? 0 : args.length;

        if (parameters.length != argsLength) {
            throw new InterpreterException("Number of Lambda parameters doesn't match. Caller provided " + args.length + "" +
                                               " arguments, yet the LAMBDA only has " + parameters.length + " ids. " + body);
        }

        // Should copy any states of local, global contexts on evaluation, so we can use access variables that
        // is defined after the lambda has been captured.
        //
        // Example:
        // ```trg
        // testFn = LAMBDA =>
        //   #MESSAGE "Hello " + playerName
        // ENDLAMBDA
        //
        // playerName = player.getName()   // <- Should be copied from the local context
        // testFn()
        final Interpreter lambdaBody = new Interpreter(body, localContext.copyState("LAMBDA"), globalContext);

        // Initialize arguments as variables in the lambda
        for (int i = 0; i < parameters.length; i++) {
            lambdaBody.getVars().put(parameters[i].id, args[i]);
        }

        lambdaBody.start();

        return lambdaBody.result();
    }

}
