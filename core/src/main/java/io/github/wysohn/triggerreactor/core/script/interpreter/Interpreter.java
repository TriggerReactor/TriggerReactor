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
package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.Token.Type;
import io.github.wysohn.triggerreactor.core.script.interpreter.lambda.LambdaFunction;
import io.github.wysohn.triggerreactor.core.script.interpreter.statement.*;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.parser.Parser;
import io.github.wysohn.triggerreactor.core.script.wrapper.Accessor;
import io.github.wysohn.triggerreactor.core.script.wrapper.IScriptObject;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class Interpreter {
    public InterpreterGlobalContext globalContext;

    private Map<Type, InterpreterExecutionUnit> statements = new HashMap<Type, InterpreterExecutionUnit>() {{
        put(Type.IF ,new IfConditionalStatement());
        put(Type.TRY, new TryCatchStatement());
        put(Type.WHILE, new WhileLoopStatement());
        put(Type.FOR, new ForLoopStatement());
        put(Type.LAMBDA, new LambdaStatement());
        put(Type.SWITCH, new SwitchStatement());
        put(Type.SYNC, new SyncStatement());
        put(Type.ASYNC, new ASyncStatement());
        put(Type.SHORT_CIRCUIT, new ShortCircuitStatement());
    }};

    private Node root;

    Interpreter(Node root) {
        this.root = root;
    }

    public Map<String, Executor> getExecutorMap() {
        return globalContext.executorMap;
    }

    public Map<String, Placeholder> getPlaceholderMap() {
        return globalContext.placeholderMap;
    }

    public Map<String, Object> getGvars() {
        return globalContext.gvars;
    }

    public SelfReference getSelfReference() {
        return globalContext.selfReference;
    }

    /**
     * Check if the root node, local context, and global context are all set.
     * <p>
     * Otherwise, it will throw an exception.
     */
    void verifyPreCondition() {
        ValidationUtil.notNull(this.root);
        ValidationUtil.notNull(this.globalContext);
    }

    /**
     * Start interpretation.
     *
     * @param triggerCause The triggerCause that can be used by Executors.
     *                     This is usually Event object for Bukkit plugin.
     * @throws InterpreterException
     */
    public void start(Object triggerCause, InterpreterLocalContext context) throws InterpreterException {
        ValidationUtil.notNull(context);
        verifyPreCondition();

        try (Timings.Timing t = context.getTiming()
                .getTiming("Code Interpretation")
                .begin(globalContext.task.isServerThread())) {
            for (int i = 0; i < root.getChildren().size(); i++)
                next(root.getChildren().get(i), context);
        }
    }

    /**
     * Get possible result produced by this interpreter execution.
     * <p>
     * For example, if the code is simply 4 + 5 * 6 without any assignment,
     * the result will be 34.
     *
     * @param localContext
     * @param localContext
     * @return the result (top of the stack); null if there is no result.
     */
    public Object result(InterpreterLocalContext localContext) throws InterpreterException {
        if (localContext.stackEmpty())
            return null;

        Token token = localContext.popToken();
        if (isVariable(token)) {
            token = unwrapVariable(token, localContext);
        }

        return token.value;
    }

    //Check if stopFlag is on before pop Token from stack.
    public void next(Node node, InterpreterLocalContext localContext) throws InterpreterException {
        if (localContext.isStopFlag())
            return;

        // Verify that the current node token is Statement and execute.
        boolean isMatched = false;
        List<InterpreterExecutionUnit> statementIterator = statements.values().stream()
                .filter(s -> !s.isExclusive()) // if isExclusive is true, take off Statement instance.
                .collect(Collectors.toList());

        for (InterpreterExecutionUnit state : statementIterator) {
            if (state.isCallable(node.getToken())) {
                // if return is ture. continue returning process.
                if (state.evaluate(this, node, localContext)) return;

                isMatched = true;
                break;
            }
        }

        if (!isMatched) {
            for (int i = 0; i < node.getChildren().size(); i++) {
                //ignore rest of body and continue if continue flag is set
                if (localContext.isContinueFlag())
                    continue;
                //ignore rest of body and stop
                if (localContext.isBreakFlag())
                    break;

                Node child = node.getChildren().get(i);
                next(child, localContext);

                if (i == 0) {
                    /* Short-Circuit Evaluation */
                    InterpreterExecutionUnit shortCircuit = statements.get(Type.SHORT_CIRCUIT);
                    if (shortCircuit.isCallable(node.getToken())) {
                        if (shortCircuit.evaluate(this, node, localContext)) return;
                    }

                }
            }
        }

        Integer result = interpret(node, localContext);
        if (result != null) {
            switch (result) {
                case Executor.STOP:
                    localContext.setStopFlag(true);
                    return;
                case Executor.WAIT:
                    localContext.setWaitFlag(true);
                    synchronized (localContext.waitLock) {
                        while (localContext.isWaitFlag()) {
                            try {
                                localContext.waitLock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case Executor.BREAK:
                    localContext.setBreakFlag(true);
                    return;
                case Executor.CONTINUE:
                    localContext.setContinueFlag(true);
                    return;
                default:
                    throw new InterpreterException(result + " is not a valid return code!");
            }
        }
    }

    public void assignValue(Token id, Token value, InterpreterLocalContext localContext) throws InterpreterException {
        if (id.type == Type.ACCESS) {
            Accessor accessor = (Accessor) id.value;
            try {
                if (value.type == Type.NULLVALUE) {
                    accessor.setTargetValue(null);
                } else {
                    if (isVariable(value)) {
                        value = unwrapVariable(value, localContext);
                    }

                    accessor.setTargetValue(value.value);
                }
            } catch (NoSuchFieldException e) {
                throw new InterpreterException("Unknown field " + id.value + "." + value.value);
            } catch (Exception e) {
                throw new InterpreterException("Unknown error ", e);
            }
        } else if (id.type == Type.GID || id.type == Type.GID_TEMP) {
            if (value.type == Type.NULLVALUE) {
                if (id.type == Type.GID) {
                    globalContext.gvars.remove(id.value.toString());
                } else {
                    globalContext.tempGvars.remove(id.value.toString());
                }

            } else {
                if (isVariable(value)) {
                    value = unwrapVariable(value, localContext);
                }

                if (id.type == Type.GID) {
                    globalContext.gvars.put(id.value.toString(), value.value);
                } else {
                    globalContext.tempGvars.put(id.value.toString(), value.value);
                }
            }
        } else if (id.type == Type.ID) {
            if (isVariable(value)) {
                value = unwrapVariable(value, localContext);
            }

            localContext.setVar(id.value.toString(), value.value);
        } else {
            throw new InterpreterException(
                    "Cannot assign value to " + (id.value == null ? null : id.value.getClass().getSimpleName()));
        }
    }

    private void callFunction(Token right, Token left, Object[] args, InterpreterLocalContext localContext) throws InterpreterException {
        callFunction(left.value.getClass(), right, left, args, localContext);
    }

    private void callFunction(Class<?> type, Token right, Token left, Object[] args, InterpreterLocalContext localContext) throws InterpreterException {
        Object result;

        if (type == null) {
            type = left.value.getClass();
        }

        if (localContext.hasImport((String) right.value)) {
            Class<?> clazz = localContext.getImport((String) right.value);

            try {
                result = ReflectionUtil.constructNew(clazz, args);
            } catch (Exception e) {
                throw new InterpreterException(
                        "Cannot create new instance with " + right + " of " + clazz.getSimpleName(), e);
            }
        } else if (left.type == Type.CLAZZ) {
            Class<?> clazz = (Class<?>) left.value;

            try {
                result = ReflectionUtil.invokeMethod(clazz, null, (String) right.value, args);
            } catch (IllegalAccessException e) {
                throw new InterpreterException("Function " + right + " is not visible.", e);
            } catch (NoSuchMethodException e) {
                throw new InterpreterException("Function " + right + " does not exist or parameter types not match.",
                        e);
            } catch (InvocationTargetException e) {
                throw new InterpreterException("Error whilst executing function " + right, e);
            } catch (IllegalArgumentException e) {
                throw new InterpreterException(
                        "Could not execute function " + right + " due to inappropriate arguments.", e);
            }
        } else {
            try {
                final Object obj = localContext.getVar((String) right.value);
                if (obj instanceof LambdaFunction) {
                    result = ReflectionUtil.invokeMethod(obj, "invoke", null, null, args);
                } else {
                    result = ReflectionUtil.invokeMethod(type, left.value, (String) right.value, args);
                }
            } catch (IllegalAccessException e) {
                throw new InterpreterException("Function " + right + " is not visible.", e);
            } catch (NoSuchMethodException e) {
                throw new InterpreterException("Function " + right + " does not exist or parameter types not match.",
                        e);
            } catch (InvocationTargetException e) {
                throw new InterpreterException("Error whilst executing function " + right, e);
            } catch (IllegalArgumentException e) {
                throw new InterpreterException(
                        "Could not execute function " + right + " due to inappropriate arguments.", e);
            }
        }

        if (result != null) {
            if (isPrimitive(result)) {
                localContext.pushToken(new Token(Type.EPS, result, right));
            } else {
                localContext.pushToken(new Token(Type.OBJECT, result, right));
            }
        } else {
            localContext.pushToken(new Token(Type.NULLVALUE, null, right));
        }
    }

    private boolean isPrimitive(Object obj) {
        return obj.getClass() == Boolean.class
                || obj.getClass() == Integer.class
                || obj.getClass() == Double.class
                || obj.getClass() == String.class;
    }

    public boolean isVariable(Token token) {
        return token.type == Type.ID
                || token.type == Type.GID
                || token.type == Type.GID_TEMP
                || token.type == Type.ACCESS;
    }

    public Token unwrapVariable(Token varToken, InterpreterLocalContext localContext) throws InterpreterException {
        if (varToken.type == Type.ID) {
            if (localContext.hasImport((String) varToken.value)) {
                Class<?> clazz = localContext.getImport((String) varToken.value);
                return new Token(Type.CLAZZ, clazz, varToken.row, varToken.col);
            }

            Object var = localContext.getVars().get(varToken.value);

            return parseValue(var, varToken);
        } else if (varToken.type == Type.GID) {
            return parseValue(globalContext.gvars.get((String) varToken.value), varToken);
        } else if (varToken.type == Type.GID_TEMP) {
            return parseValue(globalContext.tempGvars.get((String) varToken.value),
                    varToken);
        } else if (varToken.type == Type.ACCESS) {
            Accessor accessor = (Accessor) varToken.value;
            Object var;
            try {
                var = accessor.evaluateTarget();
            } catch (NoSuchFieldException e) {
                throw new InterpreterException("Unknown field " + accessor, e);
            } catch (Exception e) {
                throw new InterpreterException("Unknown error " + e.getMessage(), e);
            }

            return parseValue(var, varToken);
        } else {
            throw new InterpreterException("Unresolved id " + varToken);
        }
    }

    public Token parseValue(Object var, Token origin) {
        if (var == null) {
            return new Token(Type.NULLVALUE, null, origin);
        } else if (var.getClass() == Integer.class) {
            return new Token(Type.INTEGER, var, origin);
        } else if (var.getClass() == Double.class) {
            return new Token(Type.DECIMAL, var, origin);
        } else if (var.getClass() == String.class) {
            return new Token(Type.STRING, var, origin);
        } else if (var.getClass() == Boolean.class) {
            return new Token(Type.BOOLEAN, var, origin);
        } else if (var instanceof IScriptObject) {
            return new Token(Type.OBJECT, ((IScriptObject) var).get(), origin);
        } else {
            return new Token(Type.OBJECT, var, origin);
        }
    }

    /**
     * @param node
     * @param localContext
     * @param localContext
     * @return return codes in Executor. null if execution continues.
     * @throws InterpreterException
     */
    private Integer interpret(Node node, InterpreterLocalContext localContext) throws InterpreterException {
        try {
            if (localContext.getInterrupter() != null && localContext.getInterrupter().onNodeProcess(localContext, node)) {
                return Executor.STOP;
            }

            if (node.getToken().type == Type.BODY
                    || node.getToken().type == Type.CATCHBODY
                    || node.getToken().type == Type.FINALLYBODY
                    || node.getToken().type == Type.LAMBDA
                    || node.getToken().type == Type.SWITCH
                    || node.getToken().type == Type.CASE
                    || node.getToken().type == Type.CASEBODY
                    || "IF".equals(node.getToken().value)
                    || "ELSEIF".equals(node.getToken().value)
                    || "WHILE".equals(node.getToken().value)) {
                return null;
            } else if ("IS".equals(node.getToken().value)) {
                Token right = localContext.popToken();
                Token left = localContext.popToken();

                if (isVariable(right)) {
                    right = unwrapVariable(right, localContext);
                }

                if (!(right.value instanceof Class))
                    throw new RuntimeException(right + " is not a Class!");

                if (isVariable(left)) {
                    left = unwrapVariable(left, localContext);
                }

                Class<?> clazz = (Class<?>) right.value;
                localContext.pushToken(new Token(Type.BOOLEAN, clazz.isInstance(left.value), node.getToken()));
            } else if (node.getToken().type == Type.EXECUTOR) {
                String command = (String) node.getToken().value;

                Object[] args = new Object[node.getChildren().size()];
                for (int i = args.length - 1; i >= 0; i--) {
                    Token argument = localContext.popToken();

                    if (isVariable(argument)) {
                        argument = unwrapVariable(argument, localContext);
                    }

                    args[i] = argument.value;
                }

                if (localContext.getInterrupter() != null && localContext.getInterrupter().onCommand(localContext, command, args)) {
                    return null;
                } else {
                    /*
                     * Warning) Most of the executors do not depend on the state of the Interpreter,
                     * so most of them can be in the GlobalInterpreterContext and be shared with other
                     * Interpreter executions. But #WAIT is a little bit different.
                     *
                     * Since it holds the monitor of the Interpreter in WAITING state, #WAIT executor has
                     * to be unique instance per each Interpreter. Therefore, #WAIT must be existing
                     * individually per Interpreter and should not be shared with other Interpreter
                     * instances.
                     */
                    if ("WAIT".equalsIgnoreCase(command)) {
                        Executor executorWait = (timing, vars, triggerCause, args1) -> {
                            if (globalContext.task.isServerThread()) {
                                throw new RuntimeException("WAIT is illegal in sync mode!");
                            }

                            if (args1.length < 1)
                                throw new RuntimeException("Missing arguments [Decimal].");

                            if (!(args1[0] instanceof Number))
                                throw new RuntimeException(args1[0] + " is not a number!");

                            double secs = ((Number) args1[0]).doubleValue();
                            long later = (long) (secs * 1000);

                            globalContext.task.runTaskLater(() -> {
                                synchronized (localContext.waitLock) {
                                    localContext.setWaitFlag(false);
                                    localContext.waitLock.notify();
                                }
                            }, later);
                            return Executor.WAIT;
                        };
                        return executorWait.evaluate(localContext.getTiming(),
                                localContext.getVars(),
                                localContext.getTriggerCause(),
                                args);
                    }

                    if (!globalContext.executorMap.containsKey(command))
                        throw new InterpreterException("No executor named #" + command + " found!");

                    return (Integer) globalContext.executorMap.get(command).evaluate(localContext.getTiming(),
                            localContext.getVars(),
                            localContext.getTriggerCause(),
                            args);
                }
            } else if (node.getToken().type == Type.PLACEHOLDER) {
                String placeholderName = (String) node.getToken().value;

                Object[] args = new Object[node.getChildren().size()];
                for (int i = args.length - 1; i >= 0; i--) {
                    Token argument = localContext.popToken();

                    if (isVariable(argument)) {
                        argument = unwrapVariable(argument, localContext);
                    }

                    args[i] = argument.value;
                }

                Object replaced = null;
                if (localContext.getInterrupter() != null) {
                    replaced = localContext.getInterrupter().onPlaceholder(localContext, placeholderName, args);
                }

                if (replaced == null && !globalContext.placeholderMap.containsKey(placeholderName))
                    throw new InterpreterException("No placeholder named $" + placeholderName + " found!");

                if (replaced == null) {
                    replaced = globalContext.placeholderMap.get(placeholderName).evaluate(localContext.getTiming(),
                            localContext.getVars(),
                            localContext.getTriggerCause(),
                            args);
                }

                if (replaced instanceof Number) {
                    double d = ((Number) replaced).doubleValue();
                    if (d % 1 == 0) {
                        // whole number
                        localContext.pushToken(new Token(Type.INTEGER, (int) d, node.getToken()));
                    } else {
                        localContext.pushToken(new Token(Type.DECIMAL, d, node.getToken()));
                    }
                } else if (replaced == null) {
                    localContext.pushToken(new Token(Type.NULLVALUE, null, node.getToken()));
                } else {
                    localContext.pushToken(new Token(Type.EPS, replaced, node.getToken()));
                }
            } else if (node.getToken().type == Type.OPERATOR_A) {
                String tokenValue = (String) node.getToken().value;

                Token right = "~".equals(tokenValue) ? null : localContext.popToken();
                Token left = localContext.popToken();

                if (right != null && isVariable(right)) {
                    right = unwrapVariable(right, localContext);
                }

                if (isVariable(left)) {
                    left = unwrapVariable(left, localContext);
                }

                if ("+".equals(tokenValue)
                        && (left.type == Type.STRING || (right != null && right.type == Type.STRING))) {
                    localContext.pushToken(new Token(Type.STRING, String.valueOf(left.value) + Optional.ofNullable(right)
                            .map(r -> r.value).orElse("null"), node.getToken()));
                } else if ("&".equals(tokenValue) || "^".equals(tokenValue) || "|".equals(tokenValue)) {
                    if (left.type == Type.BOOLEAN && right != null && right.type == Type.BOOLEAN) {
                        boolean result;
                        switch (tokenValue) {
                            case "&":
                                result = left.toBoolean() & right.toBoolean();
                                break;
                            case "^":
                                result = left.toBoolean() ^ right.toBoolean();
                                break;
                            default: //case "|"
                                result = left.toBoolean() | right.toBoolean();
                        }

                        localContext.pushToken(new Token(Type.BOOLEAN, result, node.getToken().row, node.getToken().col));
                    } else {
                        if (right == null || !left.isNumeric() || left.isDecimal() || !right.isNumeric()
                                || right.isDecimal())
                            throw new InterpreterException(
                                    "Cannot execute bitwise operation on value [" + left + "] and [" + right
                                            + "]! Operands should both be boolean or integer.");

                        int result;
                        switch (tokenValue) {
                            case "&":
                                result = left.toInteger() & right.toInteger();
                                break;
                            case "^":
                                result = left.toInteger() ^ right.toInteger();
                                break;
                            default: //case "|"
                                result = left.toInteger() | right.toInteger();
                                break;
                        }
                        localContext.pushToken(new Token(Type.INTEGER, result, node.getToken().row, node.getToken().col));
                    }
                } else if ("~".equals(tokenValue) || "<<".equals(tokenValue) || ">>".equals(tokenValue) || ">>>".equals(
                        tokenValue)) {
                    if (!left.isNumeric() || left.isDecimal())
                        throw new InterpreterException(
                                "Cannot execute bit shift operation on non-integer value [" + left + "]!");

                    if (right != null && (!right.isNumeric() || right.isDecimal()))
                        throw new InterpreterException(
                                "Cannot execute bit shift operation on non-integer value [" + right + "]!");

                    int result;
                    switch (tokenValue) {
                        case "~":
                            result = ~left.toInteger();
                            break;
                        case "<<":
                            if (right == null)
                                throw new InterpreterException(
                                        "Bitwise operator encountered null: " + left + " << null");

                            result = left.toInteger() << right.toInteger();
                            break;
                        case ">>":
                            if (right == null)
                                throw new InterpreterException(
                                        "Bitwise operator encountered null: " + left + " >> null");

                            result = left.toInteger() >> right.toInteger();
                            break;
                        default: //case ">>>"
                            if (right == null)
                                throw new InterpreterException(
                                        "Bitwise operator encountered null: " + left + " >>> null");

                            result = left.toInteger() >>> right.toInteger();
                            break;
                    }
                    localContext.pushToken(new Token(Type.INTEGER, result, node.getToken().row, node.getToken().col));
                } else {
                    if (!left.isNumeric())
                        throw new InterpreterException(
                                "Cannot execute arithmetic operation on non-numeric value [" + left + "]!");

                    if (right == null || !right.isNumeric())
                        throw new InterpreterException(
                                "Cannot execute arithmetic operation on non-numeric value [" + right + "]!");

                    boolean integer = true;
                    if (left.isDecimal() || right.isDecimal()) {
                        integer = false;
                    }

                    Number result;
                    switch (tokenValue) {
                        case "+":
                            result = integer
                                    ? left.toInteger() + right.toInteger()
                                    : left.toDecimal() + right.toDecimal();
                            break;
                        case "-":
                            result = integer
                                    ? left.toInteger() - right.toInteger()
                                    : left.toDecimal() - right.toDecimal();
                            break;
                        case "*":
                            result = integer
                                    ? left.toInteger() * right.toInteger()
                                    : left.toDecimal() * right.toDecimal();
                            break;
                        case "/":
                            result = integer
                                    ? left.toInteger() / right.toInteger()
                                    : left.toDecimal() / right.toDecimal();
                            break;
                        case "%":
                            result = integer
                                    ? left.toInteger() % right.toInteger()
                                    : left.toDecimal() % right.toDecimal();
                            break;
                        default:
                            throw new InterpreterException(
                                    "Cannot interpret the unknown operator " + node.getToken().value);
                    }

                    if (integer) {
                        localContext.pushToken(new Token(Type.INTEGER,
                                result.intValue(),
                                node.getToken().row,
                                node.getToken().col));
                    } else {
                        localContext.pushToken(new Token(Type.DECIMAL,
                                result.doubleValue(),
                                node.getToken().row,
                                node.getToken().col));
                    }
                }
            } else if (node.getToken().type == Type.OPERATOR_UNARY) {
                if ("-".equals(node.getToken().value)) {
                    Token value = localContext.popToken();

                    if (isVariable(value)) {
                        value = unwrapVariable(value, localContext);
                    }

                    if (!value.isNumeric())
                        throw new InterpreterException(
                                "Cannot do unary minus operation for non-numeric value " + value);

                    localContext.pushToken(value.isInteger() ? new Token(Type.INTEGER,
                            -value.toInteger(),
                            value.row,
                            value.col)
                            : new Token(Type.DECIMAL, -value.toDecimal(), value.row, value.col));
                } else {
                    Token var = localContext.popToken();

                    if (isVariable(var)) {
                        Token unwrappedVar = unwrapVariable(var, localContext);

                        if (!unwrappedVar.isNumeric())
                            throw new InterpreterException("Cannot do unary operation for non-numeric value " + var);

                        boolean processed = false;
                        if ("++expr".equals(node.getToken().value)) {
                            assignValue(var,
                                    unwrappedVar.isInteger() ? new Token(Type.INTEGER,
                                            unwrappedVar.toInteger() + 1,
                                            var.row,
                                            var.col)
                                            : new Token(Type.DECIMAL,
                                            unwrappedVar.toDecimal() + 1,
                                            var.row,
                                            var.col), localContext);
                            processed = true;
                        } else if ("--expr".equals(node.getToken().value)) {
                            assignValue(var,
                                    unwrappedVar.isInteger() ? new Token(Type.INTEGER,
                                            unwrappedVar.toInteger() - 1,
                                            var.row,
                                            var.col)
                                            : new Token(Type.DECIMAL,
                                            unwrappedVar.toDecimal() - 1,
                                            var.row,
                                            var.col), localContext);
                            processed = true;
                        }

                        unwrappedVar = unwrapVariable(var, localContext);
                        localContext.pushToken(unwrappedVar.isInteger() ? new Token(Type.INTEGER,
                                unwrappedVar.toInteger(),
                                var.row,
                                var.col)
                                : new Token(Type.DECIMAL,
                                unwrappedVar.toDecimal(),
                                var.row,
                                var.col));

                        if ("expr++".equals(node.getToken().value)) {
                            assignValue(var,
                                    unwrappedVar.isInteger() ? new Token(Type.INTEGER,
                                            unwrappedVar.toInteger() + 1,
                                            var.row,
                                            var.col)
                                            : new Token(Type.DECIMAL,
                                            unwrappedVar.toDecimal() + 1,
                                            var.row,
                                            var.col), localContext);
                            processed = true;
                        } else if ("expr--".equals(node.getToken().value)) {
                            assignValue(var,
                                    unwrappedVar.isInteger() ? new Token(Type.INTEGER,
                                            unwrappedVar.toInteger() - 1,
                                            var.row,
                                            var.col)
                                            : new Token(Type.DECIMAL,
                                            unwrappedVar.toDecimal() - 1,
                                            var.row,
                                            var.col), localContext);
                            processed = true;
                        }

                        if (!processed) {
                            throw new InterpreterException(
                                    "Cannot interpret the unknown unary operator " + node.getToken().value);
                        }
                    } else {
                        throw new InterpreterException(
                                "Cannot do unary increment/decrement operation for non-variable" + var);
                    }
                }
            } else if (node.getToken().type == Type.OPERATOR_L) {
                if ("!".equals(node.getToken().value)) {
                    Token boolval = localContext.popToken();

                    if (isVariable(boolval)) {
                        boolval = unwrapVariable(boolval, localContext);
                    }

                    if (boolval.type == Type.NULLVALUE) {// treat null as false
                        localContext.pushToken(new Token(Type.BOOLEAN, true, node.getToken()));
                    } else if (boolval.isBoolean()) {
                        localContext.pushToken(new Token(Type.BOOLEAN, !boolval.toBoolean(), node.getToken()));
                    } else if (boolval.isDecimal()) {
                        localContext.pushToken(new Token(Type.BOOLEAN, boolval.toDecimal() == 0.0, node.getToken()));
                    } else if (boolval.isInteger()) {
                        localContext.pushToken(new Token(Type.BOOLEAN, boolval.toInteger() == 0, node.getToken()));
                    } else {
                        throw new InterpreterException("Cannot negate non-boolean value " + boolval);
                    }
                } else {
                    Token right = localContext.popToken();
                    Token left = localContext.popToken();

                    if (isVariable(right)) {
                        right = unwrapVariable(right, localContext);
                    }

                    if (isVariable(left)) {
                        left = unwrapVariable(left, localContext);
                    }

                    switch ((String) node.getToken().value) {
                        case "<":
                            if (!left.isNumeric() || !right.isNumeric())
                                throw new InterpreterException("Only numeric values can be compared!");

                            localContext.pushToken(new Token(Type.BOOLEAN,
                                    (left.isInteger() ? left.toInteger() : left.toDecimal()) < (
                                            right.isInteger()
                                                    ? right.toInteger()
                                                    : right.toDecimal()),
                                    node.getToken()));
                            break;
                        case ">":
                            if (!left.isNumeric() || !right.isNumeric())
                                throw new InterpreterException("Only numeric values can be compared!");

                            localContext.pushToken(new Token(Type.BOOLEAN,
                                    (left.isInteger() ? left.toInteger() : left.toDecimal()) > (
                                            right.isInteger()
                                                    ? right.toInteger()
                                                    : right.toDecimal()),
                                    node.getToken()));
                            break;
                        case "<=":
                            if (!left.isNumeric() || !right.isNumeric())
                                throw new InterpreterException("Only numeric values can be compared!");

                            localContext.pushToken(new Token(Type.BOOLEAN,
                                    (left.isInteger() ? left.toInteger() : left.toDecimal()) <= (
                                            right.isInteger()
                                                    ? right.toInteger()
                                                    : right.toDecimal()),
                                    node.getToken()));
                            break;
                        case ">=":
                            if (!left.isNumeric() || !right.isNumeric())
                                throw new InterpreterException("Only numeric values can be compared!");

                            localContext.pushToken(new Token(Type.BOOLEAN,
                                    (left.isInteger() ? left.toInteger() : left.toDecimal()) >= (
                                            right.isInteger()
                                                    ? right.toInteger()
                                                    : right.toDecimal()),
                                    node.getToken()));
                            break;
                        case "==":
                            if (left.type == Type.NULLVALUE || right.type == Type.NULLVALUE) {
                                if (left.value == null && right.value == null) {
                                    localContext.pushToken(new Token(Type.BOOLEAN, true, node.getToken()));
                                } else if (left.value == null) {
                                    localContext.pushToken(new Token(Type.BOOLEAN, right.value == null, node.getToken()));
                                } else {
                                    localContext.pushToken(new Token(Type.BOOLEAN, left.value == null, node.getToken()));
                                }
                            } else {
                                localContext.pushToken(new Token(Type.BOOLEAN,
                                        left.value.equals(right.value),
                                        node.getToken()));
                            }

                            break;
                        case "!=":
                            if (left.type == Type.NULLVALUE || right.type == Type.NULLVALUE) {
                                if (left.value == null && right.value == null) {
                                    localContext.pushToken(new Token(Type.BOOLEAN, false, node.getToken()));
                                } else if (left.value == null) {
                                    localContext.pushToken(new Token(Type.BOOLEAN, right.value != null, node.getToken()));
                                } else {
                                    localContext.pushToken(new Token(Type.BOOLEAN, left.value != null, node.getToken()));
                                }
                            } else {
                                localContext.pushToken(new Token(Type.BOOLEAN,
                                        !left.value.equals(right.value),
                                        node.getToken()));
                            }
                            break;
                        case "&&":
                            localContext.pushToken(new Token(Type.BOOLEAN,
                                    left.toBoolean() && right.toBoolean(),
                                    node.getToken()));
                            break;
                        case "||":
                            localContext.pushToken(new Token(Type.BOOLEAN,
                                    left.toBoolean() || right.toBoolean(),
                                    node.getToken()));
                            break;
                    }
                }
            } else if (node.getToken().type == Type.OPERATOR) {
                Token right, left;
                switch ((String) node.getToken().value) {
                    case "@":
                        // id
                        right = localContext.popToken();
                        // type to cast to
                        left = localContext.popToken();

                        if (isVariable(left)) {
                            left = unwrapVariable(left, localContext);
                        }

                        if (left.value == null)
                            throw new InterpreterException("Cannot cast to null type. Make sure it's IMPORTed. " + right);

                        if (!(left.value instanceof Class))
                            throw new InterpreterException("Cannot cast to non-class/interface type." + left);

                        localContext.pushToken(new Token(Type.ID, right.value, right).castTo((Class<?>) left.value));
                        break;
                    case "=":
                        right = localContext.popToken();
                        left = localContext.popToken();

                        assignValue(left, right, localContext);
                        break;
                    case ".":
                        right = localContext.popToken();
                        //function call
                        if (right.type == Type.CALL) {
                            Object[] args = new Object[localContext.getCallArgsSize()];
                            for (int i = localContext.getCallArgsSize() - 1; i >= 0; i--) {
                                Token argument = localContext.popToken();

                                if (isVariable(argument)) {
                                    argument = unwrapVariable(argument, localContext);
                                }

                                args[i] = argument.value;
                            }
                            localContext.setCallArgsSize(0);

                            left = localContext.popToken();

                            if (left.type == Type.THIS) {
                                callFunction(new Token(Type.OBJECT, right.value, node.getToken()),
                                        new Token(Type.OBJECT, globalContext.selfReference, node.getToken()),
                                        args, localContext);
                            } else {
                                Token temp = left;

                                if (isVariable(left)) {
                                    left = unwrapVariable(left, localContext);
                                }

                                if (left.getType() == Type.NULLVALUE) {
                                    throw new InterpreterException(
                                            "Cannot access " + right + "! " + temp.value + " is null.");
                                }

                                if (left.isObject()) { // method call for target object
                                    callFunction(temp.getCastTo(), right, left, args, localContext);
                                } else if (left.isBoxedPrimitive()) { // special case: numeric class access
                                    callFunction(temp.getCastTo(), right, left, args, localContext);
                                } else if (left.value instanceof Accessor) {
                                    Accessor accessor = (Accessor) left.value;

                                    Object var;
                                    try {
                                        var = accessor.evaluateTarget();
                                    } catch (NoSuchFieldException e) {
                                        throw new InterpreterException("Unknown field " + accessor, e);
                                    } catch (Exception e) {
                                        throw new InterpreterException("Unknown error " + e.getMessage(), e);
                                    }

                                    callFunction(temp.getCastTo(), right, new Token(Type.EPS, var, node.getToken()), args, localContext);
                                } else {
                                    throw new InterpreterException(
                                            "Unexpected value " + left + " for target of " + right
                                                    + ". " + "Is " + left + "." + right
                                                    + " what you were trying to do?");
                                }
                            }
                        }
                        //field access
                        else {
                            left = localContext.popToken();

                            if (left.type == Type.THIS) {
                                localContext.pushToken(right);
                            } else {
                                Token temp = left;

                                if (isVariable(left)) {
                                    left = unwrapVariable(left, localContext);
                                }

                                if (left.getType() == Type.NULLVALUE) {
                                    throw new InterpreterException(
                                            "Cannot access " + right + "! " + temp.value + " is null.");
                                }

                                if (left.isObject() || left.isArray()) {
                                    localContext.pushToken(new Token(Type.ACCESS,
                                            new Accessor(left.value, (String) right.value),
                                            node.getToken()).castTo(right.getCastTo()));
                                } else {
                                    Accessor accessor = (Accessor) left.value;

                                    Object var;
                                    try {
                                        var = accessor.evaluateTarget();
                                    } catch (NoSuchFieldException e) {
                                        throw new InterpreterException("Unknown field " + accessor, e);
                                    } catch (Exception e) {
                                        throw new InterpreterException("Unknown error " + e.getMessage(), e);
                                    }

                                    localContext.pushToken(new Token(Type.ACCESS,
                                            new Accessor(var, (String) right.value),
                                            node.getToken()));
                                }
                            }
                        }
                        break;
                }
            } else if (node.getToken().type == Type.ARRAYACCESS) {
                Token right = localContext.popToken();
                Token left = localContext.popToken();

                if (isVariable(left)) {
                    left = unwrapVariable(left, localContext);
                }

                if (isVariable(right)) {
                    right = unwrapVariable(right, localContext);
                }

                if (!left.isArray())
                    throw new InterpreterException(left + " is not an array!");

                if (!right.isInteger())
                    throw new InterpreterException(right + " is not a valid index for array!");

                localContext.pushToken(new Token(Type.ACCESS, new Accessor(left.value, right.toInteger()), node.getToken()));
            } else if (node.getToken().type == Type.THIS) {
                localContext.pushToken(node.getToken());
            } else if (node.getToken().type == Type.ID) {
                localContext.pushToken(node.getToken());
            } else if (node.getToken().type == Type.GID || node.getToken().type == Type.GID_TEMP) {
                Token keyToken = localContext.popToken();

                if (isVariable(keyToken)) {
                    keyToken = unwrapVariable(keyToken, localContext);
                }

                if (keyToken.getType() != Type.STRING) {
                    throw new InterpreterException(keyToken + " is not a valid global variable id.");
                }

                localContext.pushToken(new Token(node.getToken().type, keyToken.value, node.getToken()));
            } else if (node.getToken().type == Type.CALL) {
                localContext.pushToken(node.getToken());
                localContext.setCallArgsSize(node.getChildren().size());
            } else if (node.getToken().type == Type.STRING) {
                localContext.pushToken(new Token(node.getToken().type, node.getToken().value, node.getToken()));
            } else if (node.getToken().type == Type.INTEGER) {
                localContext.pushToken(new Token(node.getToken().type,
                        Integer.parseInt((String) node.getToken().value),
                        node.getToken()));
            } else if (node.getToken().type == Type.DECIMAL) {
                localContext.pushToken(new Token(node.getToken().type,
                        Double.parseDouble((String) node.getToken().value),
                        node.getToken()));
            } else if (node.getToken().type == Type.BOOLEAN) {
                localContext.pushToken(new Token(node.getToken().type,
                        Boolean.parseBoolean((String) node.getToken().value),
                        node.getToken()));
            } else if (node.getToken().type == Type.EPS) {
                localContext.pushToken(new Token(node.getToken().type, node.getToken().value, node.getToken()));
            } else if (node.getToken().type == Type.NULLVALUE) {
                localContext.pushToken(new Token(node.getToken().type, null, node.getToken()));
            } else if (node.getToken().type == Type.IMPORT) {
                Class<?> clazz = Class.forName((String) node.getToken().getValue());
                String name = clazz.getSimpleName();
                if (node.getChildren().size() == 1) {
                    name = (String) node.getChildren().get(0).getToken().value;
                }
                localContext.setImport(name, clazz);
            } else if (node.getToken().type == Type.RANGE) {
                localContext.pushToken(new Token(node.getToken().type, node.getToken().value, node.getToken()));
            } else {
                throw new InterpreterException("Cannot interpret the unknown node " + node.getToken().type.name());
            }
        } catch (Exception e) {
            throw new InterpreterException("Error " + node.getToken().toStringRowColOnly(), e);
        }

        return null;
    }

    static {
        Parser.addDeprecationSupervisor(((type, value) -> type == Type.ID && "MODIFYPLAYER".equals(value)));
        Parser.addDeprecationSupervisor(((type, value) -> type == Type.ID && value.contains("$")));
    }
}
