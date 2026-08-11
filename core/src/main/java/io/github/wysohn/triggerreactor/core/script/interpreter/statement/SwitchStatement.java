package io.github.wysohn.triggerreactor.core.script.interpreter.statement;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterExecutionUnit;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterException;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.core.script.parser.Node;

public class SwitchStatement implements InterpreterExecutionUnit {

    @Override
    public boolean isExclusive() {
        return false;
    }

    @Override
    public boolean isCallable(Token token) {
        return token.getType() == Token.Type.SWITCH;
    }

    @Override
    public boolean evaluate(Interpreter interpreter, Node node, InterpreterLocalContext localContext) throws InterpreterException {
        if (node.getChildren().size() < 2) {
            throw new InterpreterException("Too few children in SWITCH expression! Expected at least 2 children but actual is " + node.getChildren().size());
        }

        final Node variableNameNode = node.getChildren().get(0);
        interpreter.next(variableNameNode, localContext);

        if (localContext.isStopFlag()) {
            return true;
        }

        final Token variableNameToken = tryUnwrapVariable(interpreter, localContext.popToken(), localContext);
        final Token.Type variableType = variableNameToken.getType();

        boolean matches = false;
        iterCaseOrDefaultNodes:
        for (int i = 1; i < node.getChildren().size(); i++) {
            final Node caseNode = node.getChildren().get(i);
            if (!Token.Type.CASE.equals(caseNode.getToken().getType())) {
                throw new InterpreterException("Expected case but found " + caseNode);
            }

            final Node parameters = caseNode.getChildren().get(0);
            if (!Token.Type.PARAMETERS.equals(parameters.getToken().getType())) {
                throw new InterpreterException("Expected parameters but found " + parameters);
            }

            // Handle `default` clause first because DEFAULT statement does not have parameters.
            final boolean isDefaultClause = "<DEFAULT>".equals(caseNode.getToken().value);
            if (isDefaultClause) {
                final Node caseBody = caseNode.getChildren().get(1);
                if (!Token.Type.CASEBODY.equals(caseBody.getToken().getType())) {
                    throw new InterpreterException("Expected case body but found " + parameters);
                }

                matches = true;
                interpreter.next(caseBody, localContext);
                break;
            }

            // Handle RANGE token.
            if (parameters.getChildren().size() == 3 && parameters.getChildren().get(1).getToken().type == Token.Type.RANGE) {
                // The expression's value should be numeric type.
                if (!variableNameToken.isInteger() && !variableNameToken.isDecimal()) {
                    throw new InterpreterException("Variable value must be numeric type! Actual is " + variableNameToken);
                }

                // # Init
                final Node initNode = parameters.getChildren().get(0);
                interpreter.next(initNode, localContext);

                if (localContext.isStopFlag()) {
                    return true;
                }

                final Token initToken = tryUnwrapVariable(interpreter, localContext.popToken(), localContext);
                if (!initToken.isInteger() && !initToken.isDecimal()) {
                    throw new InterpreterException("Init value must be an numeric value! Actual is " + initToken);
                }

                // # Bound
                final Node boundNode = parameters.getChildren().get(1);
                interpreter.next(boundNode, localContext);

                if (localContext.isStopFlag()) {
                    return true;
                }

                final Token boundToken = tryUnwrapVariable(interpreter, localContext.popToken(), localContext);

                final String boundVal = (String) boundToken.getValue();
                final boolean inclusive = "<RANGE_INCLUSIVE>".equals(boundVal);
                if (!(inclusive || "<RANGE_EXCLUSIVE>".equals(boundVal))) {
                    throw new InterpreterException("Range expression must be a '<RANGE_INCLUSIVE>' or '<RANGE_EXCLUSIVE>'. Actual is " + boundVal);
                }

                // # Limit
                final Node limitNode = parameters.getChildren().get(2);
                interpreter.next(limitNode, localContext);

                if (localContext.isStopFlag()) {
                    return true;
                }

                final Token limitToken = tryUnwrapVariable(interpreter, localContext.popToken(), localContext);
                if (!limitToken.isInteger() && !limitToken.isDecimal()) {
                    throw new InterpreterException("Limitation value must be numeric value! Actual is " + limitToken);
                }

                final boolean shouldRun;
                if (variableNameToken.isInteger()) {
                    final int maybeStart = initToken.toInteger();
                    final int maybeEnd = limitToken.toInteger();
                    final int start = Integer.min(maybeStart, maybeEnd);
                    final int end = Integer.max(maybeStart, maybeEnd);
                    final int delta = variableNameToken.toInteger();

                    if (inclusive) {
                        shouldRun = start <= delta && delta <= end;
                    } else {
                        shouldRun = start <= delta && delta < end;
                    }
                } else {
                    final double maybeStart = initToken.toDecimal();
                    final double maybeEnd = limitToken.toDecimal();
                    final double start = Double.min(maybeStart, maybeEnd);
                    final double end = Double.max(maybeStart, maybeEnd);
                    final double delta = variableNameToken.toDecimal();

                    if (inclusive) {
                        shouldRun = start <= delta && delta <= end;
                    } else {
                        shouldRun = start <= delta && delta < end;
                    }
                }

                if (shouldRun) {
                    final Node caseBody = caseNode.getChildren().get(1);
                    if (!Token.Type.CASEBODY.equals(caseBody.getToken().getType())) {
                        throw new InterpreterException("Expected case body but found " + parameters);
                    }

                    matches = true;
                    interpreter.next(caseBody, localContext);
                    break;
                }
            } else {
                for (int j = 0; j < parameters.getChildren().size(); j++) {
                    final Node parameter = parameters.getChildren().get(j);
                    interpreter.next(parameter, localContext);

                    if (localContext.isStopFlag()) {
                        return true;
                    }

                    final Token rawParameterToken = localContext.popToken();
                    final Token parameterToken;
                    // Smart casting for enum types, otherwise do default conversions.
                    if (variableNameToken.isEnum() && rawParameterToken.isString()) {
                        Token maybeParameterToken;
                        try {
                            final Class<Enum> enumClass = (Class<Enum>) variableNameToken.value.getClass();
                            maybeParameterToken = interpreter.parseValue(
                                    Enum.valueOf(enumClass, rawParameterToken.value.toString()),
                                    rawParameterToken
                            );
                        } catch (final IllegalArgumentException ignored) {
                            maybeParameterToken = tryUnwrapVariable(interpreter, rawParameterToken, localContext);
                        }

                        parameterToken = maybeParameterToken;
                    } else {
                        parameterToken = tryUnwrapVariable(interpreter, rawParameterToken, localContext);
                    }

                    final Token.Type parameterType = parameterToken.getType();
                    if (variableType != Token.Type.EPS && parameterType != Token.Type.EPS && !variableType.equals(parameterType)) {
                        throw new InterpreterException("Mismatched type for parameter " + rawParameterToken + "! Expected " + variableType + " but found " + parameterType);
                    }

                    if (variableNameToken.getValue().equals(parameterToken.getValue())) {
                        final Node caseBody = caseNode.getChildren().get(1);
                        if (!Token.Type.CASEBODY.equals(caseBody.getToken().getType())) {
                            throw new InterpreterException("Expected case body but found " + parameters);
                        }

                        matches = true;
                        interpreter.next(caseBody, localContext);
                        break iterCaseOrDefaultNodes;
                    }
                }
            }
        }

        if (!matches) {
            throw new InterpreterException("No matched arm");
        }

        return false;
    }

    private Token tryUnwrapVariable(Interpreter interpreter, final Token mayVariableToken, final InterpreterLocalContext localContext) throws InterpreterException {
        if (interpreter.isVariable(mayVariableToken)) {
            return interpreter.unwrapVariable(mayVariableToken, localContext);
        }

        return mayVariableToken;
    }
}
