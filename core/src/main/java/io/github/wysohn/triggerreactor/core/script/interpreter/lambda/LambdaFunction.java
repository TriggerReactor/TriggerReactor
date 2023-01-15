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
    private final Interpreter lambdaBody;

    public LambdaFunction(LambdaParameter[] parameters,
                          Node body,
                          InterpreterLocalContext localContext,
                          InterpreterGlobalContext globalContext) {
        this.parameters = parameters;
        this.body = body;
        this.lambdaBody = InterpreterBuilder.start(globalContext, body)
                .overrideContext(localContext.copyState("LAMBDA"))
                .build();

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
