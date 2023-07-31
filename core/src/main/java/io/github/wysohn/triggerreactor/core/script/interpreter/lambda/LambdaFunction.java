/*
 * Copyright (C) 2023. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.script.interpreter.lambda;

import io.github.wysohn.triggerreactor.core.script.interpreter.*;
import io.github.wysohn.triggerreactor.core.script.parser.Node;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class LambdaFunction implements InvocationHandler {
    private final LambdaParameter[] parameters;
    private final Node body;
    private final InterpreterLocalContext originalLocalContext;
    private final InterpreterGlobalContext globalContext;

    public LambdaFunction(LambdaParameter[] parameters,
                          Node body,
                          InterpreterLocalContext localContext,
                          InterpreterGlobalContext globalContext) {
        this.parameters = parameters;
        this.body = body;
        this.originalLocalContext = localContext;
        this.globalContext = globalContext;

        // if duplicated variable name is found, parameter name always has priority
        for (LambdaParameter parameter : parameters) {
            originalLocalContext.setVar(parameter.id, parameter.defValue);
        }
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final int argsLength = args == null ? 0 : args.length;

        if(parameters.length != argsLength)
            throw new InterpreterException("Number of Lambda parameters doesn't match. Caller provided "+argsLength+
                    " arguments, yet the LAMBDA only has "+parameters.length+" ids. "+body);

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
        // ```
        final Interpreter lambdaBody = InterpreterBuilder.start(globalContext, body)
                .build();

        // TODO consider the case where testFn() is executed in a different thread, and the playerName is changed
        //   in the main thread. Since the order of thread execution is not guaranteed, the playerName may be
        //   different from the one when the lambda was captured.
        // Example:
        // ```trg
        // testFn = LAMBDA =>
        //   #MESSAGE "Hello " + playerName
        // ENDLAMBDA
        //
        // ASYNC
        //   testFn()
        // ENDASYNC
        // playerName = player.getName()   // <- This may be changed before the testFn() is executed.
        // ```
        final InterpreterLocalContext copiedLocalContext = originalLocalContext.copyState("LAMBDA");

        // Initialize arguments as variables in the lambda
        for (int i = 0; i < parameters.length; i++) {
            copiedLocalContext.setVar(parameters[i].id, args[i]);
        }

        lambdaBody.start(originalLocalContext.getTriggerCause(), copiedLocalContext);

        return lambdaBody.result(copiedLocalContext);
    }
}
