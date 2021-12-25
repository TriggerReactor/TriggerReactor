/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.core.script.interpreter;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.Token.Type;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import io.github.wysohn.triggerreactor.core.script.interpreter.lambda.LambdaFunction;
import io.github.wysohn.triggerreactor.core.script.interpreter.lambda.LambdaParameter;
import io.github.wysohn.triggerreactor.core.script.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.parser.Parser;
import io.github.wysohn.triggerreactor.core.script.wrapper.Accessor;
import io.github.wysohn.triggerreactor.core.script.wrapper.IScriptObject;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class Interpreter {
    static {
        Parser.addDeprecationSupervisor(((type, value) -> type == Type.ID && "MODIFYPLAYER".equals(value)));
        Parser.addDeprecationSupervisor(((type, value) -> type == Type.ID && value.contains("$")));
    }

    private final Node root;
    private final InterpreterLocalContext context;
    private final InterpreterGlobalContext globalContext;
    private final Object waitLock = new Object();
    /**
     * Warning) Most of the executors are not depending on the state of the Interpreter,
     * so most of them can be in the GlobalInterpreterContext and be shared with other
     * Interpreter executions. But #WAIT is a little bit different.
     * <p>
     * Since it holds the monitor of the Interpreter in WAITING state, #WAIT executor has
     * to be unique instance per each Interpreter. Therefore, #WAIT must be existing
     * individually per Interpreter and should not be shared with other Interpreter
     * instances.
     */
    private final Executor EXECUTOR_WAIT = new Executor() {
        @Override
        public Integer execute(Timings.Timing timing, Map<String, Object> vars, Object triggerCause, Object... args) {
            if (globalContext.task.isServerThread()) {
                throw new RuntimeException("WAIT is illegal in sync mode!");
            }

            if (args.length < 1) throw new RuntimeException("Missing arguments [Decimal].");

            if (!(args[0] instanceof Number)) throw new RuntimeException(args[0] + " is not a number!");

            double secs = ((Number) args[0]).doubleValue();
            long later = (long) (secs * 1000);
            TaskSupervisor.runTaskLater(() -> {
                synchronized (waitLock) {
                    context.setWaitFlag(false);
                    waitLock.notify();
                }
            }, later);
            return WAIT;
        }
    };
    private boolean started = false;

    /*    public Interpreter(Node root, Map<String, Executor> executorMap, Map<String, Object> gvars,
                SelfReference selfReference, InterpretCondition condition) {
            this.root = root;
            for(Entry<String, Executor> entry : executorMap.entrySet())
                this.executorMap.put(entry.getKey(), entry.getValue());
            this.gvars = gvars;
            this.vars = new HashMap<>();
            this.selfReference = selfReference;
            this.condition = condition;

            initDefaultExecutors();
        }
    */
    public Interpreter(Node root, InterpreterLocalContext context, InterpreterGlobalContext globalContext) {
        this.root = root;
        this.context = context;
        this.globalContext = globalContext;
    }

    public Interpreter(Node root) {
        this(root, new InterpreterLocalContext(), new InterpreterGlobalContext());
    }

    private void assignValue(Token id, Token value) throws InterpreterException {
        if (id.type == Type.ACCESS) {
            Accessor accessor = (Accessor) id.value;
            try {
                if (value.type == Type.NULLVALUE) {
                    accessor.setTargetValue(null);
                } else {
                    if (isVariable(value)) {
                        value = unwrapVariable(value);
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
                globalContext.gvars.remove(id.type == Type.GID ? id.value.toString() : new TemporaryGlobalVariableKey(id.value.toString()));
            } else {
                if (isVariable(value)) {
                    value = unwrapVariable(value);
                }

                globalContext.gvars.put(id.type == Type.GID ? id.value.toString() : new TemporaryGlobalVariableKey(id.value.toString()),
                                        value.value);
            }
        } else if (id.type == Type.ID) {
            if (isVariable(value)) {
                value = unwrapVariable(value);
            }

            context.getVars().put(id.value.toString(), value.value);
        } else {
            throw new InterpreterException("Cannot assign value to " + id.value == null ? null : id.value.getClass()
                    .getSimpleName());
        }
    }

    private void callFunction(Token right, Token left, Object[] args) throws InterpreterException {
        Object result;

        if (context.getImportMap().containsKey(right.value)) {
            Class<?> clazz = context.getImportMap().get(right.value);

            try {
                result = ReflectionUtil.constructNew(clazz, args);
            } catch (Exception e) {
                throw new InterpreterException("Cannot create new instance with " + right + " of " + clazz.getSimpleName(),
                                               e);
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
                throw new InterpreterException("Error while executing fuction " + right, e);
            } catch (IllegalArgumentException e) {
                throw new InterpreterException("Could not execute function " + right + " due to innapropriate arguments.",
                                               e);
            }
        } else {
            try {
                result = ReflectionUtil.invokeMethod(left.value, (String) right.value, args);
            } catch (IllegalAccessException e) {
                throw new InterpreterException("Function " + right + " is not visible.", e);
            } catch (NoSuchMethodException e) {
                throw new InterpreterException("Function " + right + " does not exist or parameter types not match.",
                                               e);
            } catch (InvocationTargetException e) {
                throw new InterpreterException("Error while executing fuction " + right, e);
            } catch (IllegalArgumentException e) {
                throw new InterpreterException("Could not execute function " + right + " due to innapropriate arguments.",
                                               e);
            }
        }

        if (result != null) {
            if (isPrimitive(result)) {
                context.pushToken(new Token(Type.EPS, result, right));
            } else {
                context.pushToken(new Token(Type.OBJECT, result, right));
            }
        } else {
            context.pushToken(new Token(Type.NULLVALUE, null, right));
        }
    }

    public Map<String, Executor> getExecutorMap() {
        return globalContext.executorMap;
    }

    public void setExecutorMap(Map<String, Executor> executorMap) {
        if (started) throw new RuntimeException("Cannot change the interpreter property after started.");

        for (Entry<String, Executor> entry : executorMap.entrySet())
            globalContext.executorMap.put(entry.getKey(), entry.getValue());
    }

    public Map<Object, Object> getGvars() {
        return globalContext.gvars;
    }

    public void setGvars(Map<Object, Object> gvars) {
        if (started) throw new RuntimeException("Cannot change the interpreter property after started.");

        globalContext.gvars = gvars;
    }

    public Map<String, Placeholder> getPlaceholderMap() {
        return globalContext.placeholderMap;
    }

    public void setPlaceholderMap(Map<String, Placeholder> placeholderMap) {
        if (started) throw new RuntimeException("Cannot change the interpreter property after started.");

        for (Entry<String, Placeholder> entry : placeholderMap.entrySet())
            globalContext.placeholderMap.put(entry.getKey(), entry.getValue());
    }

    public SelfReference getSelfReference() {
        return globalContext.selfReference;
    }

    public void setSelfReference(SelfReference selfReference) {
        if (started) throw new RuntimeException("Cannot change the interpreter property after started.");

        globalContext.selfReference = selfReference;
    }

    /**
     * Map of local variables. Fill this map with necessary pairs depends on the context.
     *
     * @return
     */
    public Map<String, Object> getVars() {
        return context.getVars();
    }

    public void setVars(Map<String, Object> vars) {
        context.setVars(vars);
    }

    /**
     * @param node
     * @return return codes in Executor. null if execution continues.
     * @throws InterpreterException
     */
    private Integer interpret(Node node) throws InterpreterException {
        try {
            if (globalContext.interrupter != null && globalContext.interrupter.onNodeProcess(context, node)) {
                return Executor.STOP;
            }

            if (node.getToken().type == Type.BODY || node.getToken().type == Type.CATCHBODY || node.getToken().type == Type.FINALLYBODY || node.getToken().type == Type.LAMBDA || "IF".equals(
                    node.getToken().value) || "ELSEIF".equals(node.getToken().value) || "WHILE".equals(node.getToken().value)) {
                return null;
            } else if ("IS".equals(node.getToken().value)) {
                Token right = context.popToken();
                Token left = context.popToken();

                if (isVariable(right)) {
                    right = unwrapVariable(right);
                }

                if (!(right.value instanceof Class)) throw new RuntimeException(right + " is not a Class!");

                if (isVariable(left)) {
                    left = unwrapVariable(left);
                }

                Class<?> clazz = (Class<?>) right.value;
                context.pushToken(new Token(Type.BOOLEAN, clazz.isInstance(left.value), node.getToken()));
            } else if (node.getToken().type == Type.EXECUTOR) {
                String command = (String) node.getToken().value;

                Object[] args = new Object[node.getChildren().size()];
                for (int i = args.length - 1; i >= 0; i--) {
                    Token argument = context.popToken();

                    if (isVariable(argument)) {
                        argument = unwrapVariable(argument);
                    }

                    args[i] = argument.value;
                }

                if (globalContext.interrupter != null && globalContext.interrupter.onCommand(context, command, args)) {
                    return null;
                } else {
                    if ("WAIT".equalsIgnoreCase(command)) {
                        return EXECUTOR_WAIT.execute(context.getTiming(),
                                                     context.getVars(),
                                                     context.getTriggerCause(),
                                                     args);
                    }

                    if (!globalContext.executorMap.containsKey(command))
                        throw new InterpreterException("No executor named #" + command + " found!");

                    return globalContext.executorMap.get(command)
                            .execute(context.getTiming(), context.getVars(), context.getTriggerCause(), args);
                }
            } else if (node.getToken().type == Type.PLACEHOLDER) {
                String placeholderName = (String) node.getToken().value;

                Object[] args = new Object[node.getChildren().size()];
                for (int i = args.length - 1; i >= 0; i--) {
                    Token argument = context.popToken();

                    if (isVariable(argument)) {
                        argument = unwrapVariable(argument);
                    }

                    args[i] = argument.value;
                }

                Object replaced = null;
                if (globalContext.interrupter != null) {
                    replaced = globalContext.interrupter.onPlaceholder(context, placeholderName, args);
                }

                if (replaced == null && !globalContext.placeholderMap.containsKey(placeholderName))
                    throw new InterpreterException("No placeholder named $" + placeholderName + " found!");

                if (replaced == null) {
                    replaced = globalContext.placeholderMap.get(placeholderName)
                            .parse(context.getTiming(), context.getTriggerCause(), context.getVars(), args);
                }

                if (replaced instanceof Number) {
                    double d = ((Number) replaced).doubleValue();
                    if (d % 1 == 0) {
                        // whole number
                        context.pushToken(new Token(Type.INTEGER, (int) d, node.getToken()));
                    } else {
                        context.pushToken(new Token(Type.DECIMAL, d, node.getToken()));
                    }
                } else if (replaced == null) {
                    context.pushToken(new Token(Type.NULLVALUE, null, node.getToken()));
                } else {
                    context.pushToken(new Token(Type.EPS, replaced, node.getToken()));
                }
            } else if (node.getToken().type == Type.OPERATOR_A) {
                String tokenValue = (String) node.getToken().value;

                Token right = "~".equals(tokenValue) ? null : context.popToken();
                Token left = context.popToken();

                if (right != null && isVariable(right)) {
                    right = unwrapVariable(right);
                }

                if (isVariable(left)) {
                    left = unwrapVariable(left);
                }

                if ("+".equals(tokenValue) && (left.type == Type.STRING || (right != null && right.type == Type.STRING))) {
                    context.pushToken(new Token(Type.STRING,
                                                String.valueOf(left.value) + Optional.ofNullable(right)
                                                        .map(r -> r.value)
                                                        .orElse("null"),
                                                node.getToken()));
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

                        context.pushToken(new Token(Type.BOOLEAN, result, node.getToken().row, node.getToken().col));
                    } else {
                        if (right == null || !left.isNumeric() || left.isDecimal() || !right.isNumeric() || right.isDecimal())
                            throw new InterpreterException("Cannot execute bitwise operation on value [" + left + "] and [" + right + "]! Operands should both be boolean or integer.");

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
                        context.pushToken(new Token(Type.INTEGER, result, node.getToken().row, node.getToken().col));
                    }
                } else if ("~".equals(tokenValue) || "<<".equals(tokenValue) || ">>".equals(tokenValue) || ">>>".equals(
                        tokenValue)) {
                    if (!left.isNumeric() || left.isDecimal())
                        throw new InterpreterException("Cannot execute bit shift operation on non-integer value [" + left + "]!");

                    if (right != null && (!right.isNumeric() || right.isDecimal()))
                        throw new InterpreterException("Cannot execute bit shift operation on non-integer value [" + right + "]!");

                    int result;
                    switch (tokenValue) {
                        case "~":
                            result = ~left.toInteger();
                            break;
                        case "<<":
                            if (right == null)
                                throw new InterpreterException("Bitwise operator encountered null: " + left + " << null");

                            result = left.toInteger() << right.toInteger();
                            break;
                        case ">>":
                            if (right == null)
                                throw new InterpreterException("Bitwise operator encountered null: " + left + " >> null");

                            result = left.toInteger() >> right.toInteger();
                            break;
                        default: //case ">>>"
                            if (right == null)
                                throw new InterpreterException("Bitwise operator encountered null: " + left + " >>> null");

                            result = left.toInteger() >>> right.toInteger();
                            break;
                    }
                    context.pushToken(new Token(Type.INTEGER, result, node.getToken().row, node.getToken().col));
                } else {
                    if (!left.isNumeric())
                        throw new InterpreterException("Cannot execute arithmetic operation on non-numeric value [" + left + "]!");

                    if (right == null || !right.isNumeric())
                        throw new InterpreterException("Cannot execute arithmetic operation on non-numeric value [" + right + "]!");

                    boolean integer = true;
                    if (left.isDecimal() || right.isDecimal()) {
                        integer = false;
                    }

                    Number result;
                    switch (tokenValue) {
                        case "+":
                            result = integer ? left.toInteger() + right.toInteger() : left.toDecimal() + right.toDecimal();
                            break;
                        case "-":
                            result = integer ? left.toInteger() - right.toInteger() : left.toDecimal() - right.toDecimal();
                            break;
                        case "*":
                            result = integer ? left.toInteger() * right.toInteger() : left.toDecimal() * right.toDecimal();
                            break;
                        case "/":
                            result = integer ? left.toInteger() / right.toInteger() : left.toDecimal() / right.toDecimal();
                            break;
                        case "%":
                            result = integer ? left.toInteger() % right.toInteger() : left.toDecimal() % right.toDecimal();
                            break;
                        default:
                            throw new InterpreterException("Cannot interpret the unknown operator " + node.getToken().value);
                    }

                    if (integer) {
                        context.pushToken(new Token(Type.INTEGER,
                                                    result.intValue(),
                                                    node.getToken().row,
                                                    node.getToken().col));
                    } else {
                        context.pushToken(new Token(Type.DECIMAL,
                                                    result.doubleValue(),
                                                    node.getToken().row,
                                                    node.getToken().col));
                    }
                }
            } else if (node.getToken().type == Type.OPERATOR_UNARY) {
                if ("-".equals(node.getToken().value)) {
                    Token value = context.popToken();

                    if (isVariable(value)) {
                        value = unwrapVariable(value);
                    }

                    if (!value.isNumeric())
                        throw new InterpreterException("Cannot do unary minus operation for non-numeric value " + value);

                    context.pushToken(value.isInteger() ? new Token(Type.INTEGER,
                                                                    -value.toInteger(),
                                                                    value.row,
                                                                    value.col) : new Token(Type.DECIMAL,
                                                                                           -value.toDecimal(),
                                                                                           value.row,
                                                                                           value.col));
                } else {
                    Token var = context.popToken();

                    if (isVariable(var)) {
                        Token unwrappedVar = unwrapVariable(var);

                        if (!unwrappedVar.isNumeric())
                            throw new InterpreterException("Cannot do unary operation for non-numeric value " + var);

                        boolean processed = false;
                        if ("++expr".equals(node.getToken().value)) {
                            assignValue(var,
                                        unwrappedVar.isInteger() ? new Token(Type.INTEGER,
                                                                             unwrappedVar.toInteger() + 1,
                                                                             var.row,
                                                                             var.col) : new Token(Type.DECIMAL,
                                                                                                  unwrappedVar.toDecimal() + 1,
                                                                                                  var.row,
                                                                                                  var.col));
                            processed = true;
                        } else if ("--expr".equals(node.getToken().value)) {
                            assignValue(var,
                                        unwrappedVar.isInteger() ? new Token(Type.INTEGER,
                                                                             unwrappedVar.toInteger() - 1,
                                                                             var.row,
                                                                             var.col) : new Token(Type.DECIMAL,
                                                                                                  unwrappedVar.toDecimal() - 1,
                                                                                                  var.row,
                                                                                                  var.col));
                            processed = true;
                        }

                        unwrappedVar = unwrapVariable(var);
                        context.pushToken(unwrappedVar.isInteger() ? new Token(Type.INTEGER,
                                                                               unwrappedVar.toInteger(),
                                                                               var.row,
                                                                               var.col) : new Token(Type.DECIMAL,
                                                                                                    unwrappedVar.toDecimal(),
                                                                                                    var.row,
                                                                                                    var.col));

                        if ("expr++".equals(node.getToken().value)) {
                            assignValue(var,
                                        unwrappedVar.isInteger() ? new Token(Type.INTEGER,
                                                                             unwrappedVar.toInteger() + 1,
                                                                             var.row,
                                                                             var.col) : new Token(Type.DECIMAL,
                                                                                                  unwrappedVar.toDecimal() + 1,
                                                                                                  var.row,
                                                                                                  var.col));
                            processed = true;
                        } else if ("expr--".equals(node.getToken().value)) {
                            assignValue(var,
                                        unwrappedVar.isInteger() ? new Token(Type.INTEGER,
                                                                             unwrappedVar.toInteger() - 1,
                                                                             var.row,
                                                                             var.col) : new Token(Type.DECIMAL,
                                                                                                  unwrappedVar.toDecimal() - 1,
                                                                                                  var.row,
                                                                                                  var.col));
                            processed = true;
                        }

                        if (!processed) {
                            throw new InterpreterException("Cannot interpret the unknown unary operator " + node.getToken().value);
                        }
                    } else {
                        throw new InterpreterException("Cannot do unary increment/decrement operation for non-variable" + var);
                    }
                }
            } else if (node.getToken().type == Type.OPERATOR_L) {
                if ("!".equals(node.getToken().value)) {
                    Token boolval = context.popToken();

                    if (isVariable(boolval)) {
                        boolval = unwrapVariable(boolval);
                    }

                    if (boolval.type == Type.NULLVALUE) {// treat null as false
                        context.pushToken(new Token(Type.BOOLEAN, true, node.getToken()));
                    } else if (boolval.isBoolean()) {
                        context.pushToken(new Token(Type.BOOLEAN, !boolval.toBoolean(), node.getToken()));
                    } else if (boolval.isDecimal()) {
                        context.pushToken(new Token(Type.BOOLEAN, boolval.toDecimal() == 0.0, node.getToken()));
                    } else if (boolval.isInteger()) {
                        context.pushToken(new Token(Type.BOOLEAN, boolval.toInteger() == 0, node.getToken()));
                    } else {
                        throw new InterpreterException("Cannot negate non-boolean value " + boolval);
                    }
                } else {
                    Token right = context.popToken();
                    Token left = context.popToken();

                    if (isVariable(right)) {
                        right = unwrapVariable(right);
                    }

                    if (isVariable(left)) {
                        left = unwrapVariable(left);
                    }

                    switch ((String) node.getToken().value) {
                        case "<":
                            if (!left.isNumeric() || !right.isNumeric())
                                throw new InterpreterException("Only numeric values can be compared!");

                            context.pushToken(new Token(Type.BOOLEAN,
                                                        (left.isInteger() ? left.toInteger() : left.toDecimal()) < (right.isInteger() ? right.toInteger() : right.toDecimal()),
                                                        node.getToken()));
                            break;
                        case ">":
                            if (!left.isNumeric() || !right.isNumeric())
                                throw new InterpreterException("Only numeric values can be compared!");

                            context.pushToken(new Token(Type.BOOLEAN,
                                                        (left.isInteger() ? left.toInteger() : left.toDecimal()) > (right.isInteger() ? right.toInteger() : right.toDecimal()),
                                                        node.getToken()));
                            break;
                        case "<=":
                            if (!left.isNumeric() || !right.isNumeric())
                                throw new InterpreterException("Only numeric values can be compared!");

                            context.pushToken(new Token(Type.BOOLEAN,
                                                        (left.isInteger() ? left.toInteger() : left.toDecimal()) <= (right.isInteger() ? right.toInteger() : right.toDecimal()),
                                                        node.getToken()));
                            break;
                        case ">=":
                            if (!left.isNumeric() || !right.isNumeric())
                                throw new InterpreterException("Only numeric values can be compared!");

                            context.pushToken(new Token(Type.BOOLEAN,
                                                        (left.isInteger() ? left.toInteger() : left.toDecimal()) >= (right.isInteger() ? right.toInteger() : right.toDecimal()),
                                                        node.getToken()));
                            break;
                        case "==":
                            if (left.type == Type.NULLVALUE || right.type == Type.NULLVALUE) {
                                if (left.value == null && right.value == null) {
                                    context.pushToken(new Token(Type.BOOLEAN, true, node.getToken()));
                                } else if (left.value == null) {
                                    context.pushToken(new Token(Type.BOOLEAN, right.value == null, node.getToken()));
                                } else {
                                    context.pushToken(new Token(Type.BOOLEAN, left.value == null, node.getToken()));
                                }
                            } else {
                                context.pushToken(new Token(Type.BOOLEAN,
                                                            left.value.equals(right.value),
                                                            node.getToken()));
                            }

                            break;
                        case "!=":
                            if (left.type == Type.NULLVALUE || right.type == Type.NULLVALUE) {
                                if (left.value == null && right.value == null) {
                                    context.pushToken(new Token(Type.BOOLEAN, false, node.getToken()));
                                } else if (left.value == null) {
                                    context.pushToken(new Token(Type.BOOLEAN, right.value != null, node.getToken()));
                                } else {
                                    context.pushToken(new Token(Type.BOOLEAN, left.value != null, node.getToken()));
                                }
                            } else {
                                context.pushToken(new Token(Type.BOOLEAN,
                                                            !left.value.equals(right.value),
                                                            node.getToken()));
                            }
                            break;
                        case "&&":
                            context.pushToken(new Token(Type.BOOLEAN,
                                                        left.toBoolean() && right.toBoolean(),
                                                        node.getToken()));
                            break;
                        case "||":
                            context.pushToken(new Token(Type.BOOLEAN,
                                                        left.toBoolean() || right.toBoolean(),
                                                        node.getToken()));
                            break;
                    }
                }
            } else if (node.getToken().type == Type.OPERATOR) {
                Token right, left;
                switch ((String) node.getToken().value) {
                    case "=":
                        right = context.popToken();
                        left = context.popToken();

                        assignValue(left, right);
                        break;
                    case ".":
                        right = context.popToken();
                        //function call
                        if (right.type == Type.CALL) {
                            Object[] args = new Object[context.getCallArgsSize()];
                            for (int i = context.getCallArgsSize() - 1; i >= 0; i--) {
                                Token argument = context.popToken();

                                if (isVariable(argument)) {
                                    argument = unwrapVariable(argument);
                                }

                                args[i] = argument.value;
                            }
                            context.setCallArgsSize(0);

                            left = context.popToken();

                            if (left.type == Type.THIS) {
                                callFunction(new Token(Type.OBJECT, right.value, node.getToken()),
                                             new Token(Type.OBJECT, globalContext.selfReference, node.getToken()),
                                             args);
                            } else {
                                Token temp = left;

                                if (isVariable(left)) {
                                    left = unwrapVariable(left);
                                }

                                if (left.getType() == Type.NULLVALUE) {
                                    throw new InterpreterException("Cannot access " + right + "! " + temp.value + " is null.");
                                }

                                if (left.isObject()) { // method call for target object
                                    callFunction(right, left, args);
                                } else if (left.isBoxedPrimitive()) { // special case: numeric class access
                                    callFunction(right, left, args);
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

                                    callFunction(right, new Token(Type.EPS, var, node.getToken()), args);
                                } else {
                                    throw new InterpreterException("Unexpected value " + left + " for target of " + right + ". " + "Is " + left + "." + right + " what you were trying to do?");
                                }
                            }
                        }
                        //field access
                        else {
                            left = context.popToken();

                            if (left.type == Type.THIS) {
                                context.pushToken(right);
                            } else {
                                Token temp = left;

                                if (isVariable(left)) {
                                    left = unwrapVariable(left);
                                }

                                if (left.getType() == Type.NULLVALUE) {
                                    throw new InterpreterException("Cannot access " + right + "! " + temp.value + " is null.");
                                }

                                if (left.isObject() || left.isArray()) {
                                    context.pushToken(new Token(Type.ACCESS,
                                                                new Accessor(left.value, (String) right.value),
                                                                node.getToken()));
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

                                    context.pushToken(new Token(Type.ACCESS,
                                                                new Accessor(var, (String) right.value),
                                                                node.getToken()));
                                }
                            }
                        }
                        break;
                }
            } else if (node.getToken().type == Type.ARRAYACCESS) {
                Token right = context.popToken();
                Token left = context.popToken();

                if (isVariable(left)) {
                    left = unwrapVariable(left);
                }

                if (isVariable(right)) {
                    right = unwrapVariable(right);
                }

                if (!left.isArray()) throw new InterpreterException(left + " is not an array!");

                if (!right.isInteger()) throw new InterpreterException(right + " is not a valid index for array!");

                context.pushToken(new Token(Type.ACCESS, new Accessor(left.value, right.toInteger()), node.getToken()));
            } else if (node.getToken().type == Type.THIS) {
                context.pushToken(node.getToken());
            } else if (node.getToken().type == Type.ID) {
                context.pushToken(node.getToken());
            } else if (node.getToken().type == Type.GID || node.getToken().type == Type.GID_TEMP) {
                Token keyToken = context.popToken();

                if (isVariable(keyToken)) {
                    keyToken = unwrapVariable(keyToken);
                }

                if (keyToken.getType() != Type.STRING) {
                    throw new InterpreterException(keyToken + " is not a valid global variable id.");
                }

                context.pushToken(new Token(node.getToken().type, keyToken.value, node.getToken()));
            } else if (node.getToken().type == Type.CALL) {
                context.pushToken(node.getToken());
                context.setCallArgsSize(node.getChildren().size());
            } else if (node.getToken().type == Type.STRING) {
                context.pushToken(new Token(node.getToken().type, node.getToken().value, node.getToken()));
            } else if (node.getToken().type == Type.INTEGER) {
                context.pushToken(new Token(node.getToken().type,
                                            Integer.parseInt((String) node.getToken().value),
                                            node.getToken()));
            } else if (node.getToken().type == Type.DECIMAL) {
                context.pushToken(new Token(node.getToken().type,
                                            Double.parseDouble((String) node.getToken().value),
                                            node.getToken()));
            } else if (node.getToken().type == Type.BOOLEAN) {
                context.pushToken(new Token(node.getToken().type,
                                            Boolean.parseBoolean((String) node.getToken().value),
                                            node.getToken()));
            } else if (node.getToken().type == Type.EPS) {
                context.pushToken(new Token(node.getToken().type, node.getToken().value, node.getToken()));
            } else if (node.getToken().type == Type.NULLVALUE) {
                context.pushToken(new Token(node.getToken().type, null, node.getToken()));
            } else if (node.getToken().type == Type.IMPORT) {
                Class<?> clazz = Class.forName((String) node.getToken().getValue());
                context.getImportMap().put(clazz.getSimpleName(), clazz);
            } else {
                throw new InterpreterException("Cannot interpret the unknown node " + node.getToken().type.name());
            }
        } catch (Exception e) {
            throw new InterpreterException("Error " + node.getToken().toStringRowColOnly(), e);
        }

        return null;
    }

    private boolean isPrimitive(Object obj) {
        return obj.getClass() == Boolean.class || obj.getClass() == Integer.class || obj.getClass() == Double.class || obj.getClass() == String.class;
    }

    public boolean isStopFlag() {
        return context.isStopFlag();
    }

    private boolean isVariable(Token token) {
        return token.type == Type.ID || token.type == Type.GID || token.type == Type.GID_TEMP || token.type == Type.ACCESS;
    }

    public boolean isWaitFlag() {
        return context.isWaitFlag();
    }

    private Token parseValue(Object var, Token origin) {
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
     * Get possible result produced by this interpreter execution.
     * <p>
     * For example, if the code is simply 4 + 5 * 6 without any assignment,
     * the result will be 34.
     *
     * @return the result (top of the stack); null if there is no result.
     */
    public Object result() throws InterpreterException {
        if (context.stackEmpty()) return null;

        Token token = context.popToken();
        if (isVariable(token)) {
            token = unwrapVariable(token);
        }

        return token.value;
    }

    public void setTaskSupervisor(TaskSupervisor taskSupervisor) {
        if (started) throw new RuntimeException("Cannot change the interpreter property after started.");

        globalContext.task = taskSupervisor;
    }

    /**
     * Start interpretation without changing any context information.
     *
     * @throws InterpreterException
     */
    public void start() throws InterpreterException {
        for (int i = 0; i < root.getChildren().size(); i++)
            start(root.getChildren().get(i));
    }

    //Check if stopFlag is on before pop Token from stack.
    private void start(Node node) throws InterpreterException {
        if (context.isStopFlag()) return;

        //IF children -- [0] : condition , [1] : true body , [2] : false body(may not exist)
        if ("ELSEIF".equals(node.getToken().value) || "IF".equals(node.getToken().value)) {
            start(node.getChildren().get(0));//[0] condition
            if (context.isStopFlag()) return;

            Token resultToken = context.popToken();

            if (isVariable(resultToken)) {
                resultToken = unwrapVariable(resultToken);
            }

            if (resultToken.type == Type.NULLVALUE) { // null check failed
                if (node.getChildren().size() > 2) {
                    start(node.getChildren().get(2));
                }
            } else { // normal IF statement
                if (resultToken.isBoolean()) {
                    boolean result = (boolean) resultToken.value;
                    if (result) {
                        start(node.getChildren().get(1));//[1] true body
                    } else if (node.getChildren().size() > 2) {
                        start(node.getChildren().get(2));//[2] false body
                    }
                } else if (resultToken.isInteger()) {
                    int value = resultToken.toInteger();
                    if (value != 0) {
                        start(node.getChildren().get(1));
                    } else if (node.getChildren().size() > 2) {
                        start(node.getChildren().get(2));
                    }
                } else if (resultToken.isDecimal()) {
                    double value = resultToken.toDecimal();
                    if (value != 0.0) {
                        start(node.getChildren().get(1));
                    } else if (node.getChildren().size() > 2) {
                        start(node.getChildren().get(2));
                    }
                } else if (resultToken.value != null) {//always true if not null
                    start(node.getChildren().get(1));
                } else {
                    throw new InterpreterException("Unexpected token for IF statement! -- " + resultToken);
                }
            }
        } else if ("TRY".equals(node.getToken().value)) {
            if (node.getChildren().size() == 2 || node.getChildren().size() == 3) {
                try {
                    start(node.getChildren().get(0));
                } catch (Throwable e) {
                    if (node.getChildren().get(1).getToken().type == Type.CATCHBODY) {
                        Node catchBody = node.getChildren().get(1);

                        start(catchBody.getChildren().get(0));

                        Throwable throwable = e;

                        Token idToken = context.popToken();
                        Token valueToken = new Token(Type.OBJECT, throwable);

                        while ((throwable = throwable.getCause()) != null) {
                            valueToken = new Token(Type.OBJECT, throwable);
                        }

                        assignValue(idToken, valueToken);

                        start(catchBody.getChildren().get(1));
                    } else {
                        throw e;
                    }
                } finally {
                    if ((node.getChildren().size() == 2 && node.getChildren()
                            .get(1)
                            .getToken().type == Type.FINALLYBODY)) {
                        start(node.getChildren().get(1));
                    } else if (node.getChildren().size() == 3 && node.getChildren()
                            .get(2)
                            .getToken().type == Type.FINALLYBODY) {
                        start(node.getChildren().get(2));
                    }
                }
            } else if (node.getChildren().size() == 1) {
                throw new InterpreterException("Expected CATCH or FINALLY statement! -- " + node.getToken());
            } else {
                throw new InterpreterException("Unexpected token for TRY statement! -- " + node.getToken());
            }
        } else if ("WHILE".equals(node.getToken().value)) {
            long start = System.currentTimeMillis();

            Token resultToken = null;
            do {
                start(node.getChildren().get(0));

                if (context.stackEmpty())
                    throw new InterpreterException("Could not find condition for WHILE statement!");

                resultToken = context.popToken();

                if (isVariable(resultToken)) {
                    resultToken = unwrapVariable(resultToken);
                }

                if (!(resultToken.value instanceof Boolean))
                    throw new InterpreterException("Unexpected token for WHILE statement! -- " + resultToken);

                if ((boolean) resultToken.value) {
                    start(node.getChildren().get(1));
                    if (context.isBreakFlag()) {
                        context.setBreakFlag(false);
                        break;
                    }

                    context.setBreakFlag(false);
                    context.setContinueFlag(false);
                } else {
                    break;
                }

                if (globalContext.task.isServerThread()) {
                    long timeTook = System.currentTimeMillis() - start;
                    if (timeTook > 3000L) throw new InterpreterException(
                            "WHILE loop took more than 3 seconds in Server Thread. This is usually " + "considered as 'too long' and can crash the server.");
                }
            } while (!context.isStopFlag());
        } else if ("FOR".equals(node.getToken().value)) {
            start(node.getChildren().get(0));


            if (context.isStopFlag()) return;
            Token idToken = context.popToken();

            if (idToken == null) throw new InterpreterException("Iteration variable for FOR statement not found!");

            if (node.getChildren().get(1).getToken().type != Type.ITERATOR)
                throw new InterpreterException("Expected <ITERATOR> but found " + node.getChildren().get(1).getToken());
            Node iterNode = node.getChildren().get(1);

            if (iterNode.getChildren().size() == 1) {
                start(iterNode.getChildren().get(0));

                if (context.isStopFlag()) return;
                Token valueToken = context.popToken();

                if (isVariable(valueToken)) {
                    valueToken = unwrapVariable(valueToken);
                }

                if (!valueToken.isIterable()) throw new InterpreterException(valueToken + " is not iterable!");

                if (valueToken.isArray()) {
                    for (int i = 0; i < Array.getLength(valueToken.value); i++) {
                        Object obj = Array.get(valueToken.value, i);
                        if (context.isStopFlag()) break;

                        assignValue(idToken, parseValue(obj, valueToken));
                        start(node.getChildren().get(2));
                        if (context.isBreakFlag()) {
                            context.setBreakFlag(false);
                            break;
                        }

                        context.setBreakFlag(false);
                        context.setContinueFlag(false);
                    }
                } else {
                    for (Object obj : (Iterable<?>) valueToken.value) {
                        if (context.isStopFlag()) break;

                        assignValue(idToken, parseValue(obj, valueToken));
                        start(node.getChildren().get(2));
                        if (context.isBreakFlag()) {
                            context.setBreakFlag(false);
                            break;
                        }

                        context.setBreakFlag(false);
                        context.setContinueFlag(false);
                    }
                }
            } else if (iterNode.getChildren().size() == 2) {
                Node initNode = iterNode.getChildren().get(0);
                start(initNode);

                if (context.isStopFlag()) return;
                Token initToken = context.popToken();
                if (isVariable(initToken)) {
                    initToken = unwrapVariable(initToken);
                }

                if (!initToken.isInteger())
                    throw new InterpreterException("Init value must be an Integer value! -- " + initToken);

                Node limitNode = iterNode.getChildren().get(1);
                start(limitNode);

                if (context.isStopFlag()) return;
                Token limitToken = context.popToken();
                if (isVariable(limitToken)) {
                    limitToken = unwrapVariable(limitToken);
                }

                if (!limitToken.isInteger())
                    throw new InterpreterException("Limit value must be an Integer value! -- " + limitToken);

                for (int i = initToken.toInteger(); !context.isStopFlag() && i < limitToken.toInteger(); i++) {
                    assignValue(idToken, new Token(Type.INTEGER, i, iterNode.getToken()));
                    start(node.getChildren().get(2));
                    if (context.isBreakFlag()) {
                        context.setBreakFlag(false);
                        break;
                    }

                    context.setBreakFlag(false);
                    context.setContinueFlag(false);
                }
            } else {
                throw new InterpreterException("Number of <ITERATOR> must be 1 or 2!");
            }

        } else if (node.getToken().getType() == Type.LAMBDA) {
            if (node.getChildren().size() != 2)
                throw new InterpreterException("The LAMBDA node has " + node.getChildren()
                        .size() + " children instead of 2. " + "Report this to us: " + node);

            Node parameters = node.getChildren().get(0);
            if (parameters.getToken().getType() != Type.PARAMETERS)
                throw new InterpreterException("Expected parameters but found " + node);

            Node lambdaBody = node.getChildren().get(1);
            if (lambdaBody.getToken().getType() != Type.LAMBDABODY)
                throw new InterpreterException("Expected lambda expression body but found " + node);

            LambdaParameter[] lambdaParameters = new LambdaParameter[parameters.getChildren().size()];
            for (int i = 0; i < lambdaParameters.length; i++) {
                Node idNode = parameters.getChildren().get(i);
                if (idNode.getToken().getType() != Type.ID)
                    throw new InterpreterException("Expected lambda parameter to be an id but found " + idNode);

                lambdaParameters[i] = new LambdaParameter(idNode);
            }

            context.pushToken(new Token(Type.EPS,
                                        new LambdaFunction(lambdaParameters, lambdaBody, context, globalContext),
                                        node.getToken()));
        } else if (node.getToken().getType() == Type.SYNC) {
            try {
                globalContext.task.submitSync(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        for (Node node : node.getChildren()) {
                            //ignore whatever returns as it's impossible
                            //to handle it from the caller
                            start(node);
                        }
                        return null;
                    }

                }).get();
                return;
            } catch (InterruptedException | ExecutionException ex) {
                throw new InterpreterException("Synchronous task error.", ex);
            }
        } else if (node.getToken().getType() == Type.ASYNC) {
            globalContext.task.submitAsync(() -> {
                Node rootCopy = new Node(new Token(Type.ROOT, "<ROOT>", -1, -1));
                rootCopy.getChildren().addAll(node.getChildren());

                Interpreter copy = new Interpreter(rootCopy, context.copyState("ASYNC"), globalContext);


                try {
                    copy.start();
                } catch (InterpreterException e) {
                    throw new RuntimeException(e);
                }
            });
            return;
        } else {
            for (int i = 0; i < node.getChildren().size(); i++) {
                //ignore rest of body and continue if continue flag is set
                if (context.isContinueFlag()) continue;
                //ignore rest of body and stop
                if (context.isBreakFlag()) break;

                Node child = node.getChildren().get(i);
                start(child);

                if (i == 0) {
                    if ("&&".equals(node.getToken().value)) {
                        Token leftBool = context.popToken();
                        if (isVariable(leftBool)) {
                            leftBool = unwrapVariable(leftBool);
                        }
                        context.pushToken(leftBool);

                        if (!leftBool.isBoolean())
                            throw new InterpreterException("Left of && operator should be Boolean but was " + leftBool);

                        boolean result = leftBool.toBoolean();

                        if (!result) { //false anyway
                            return;
                        }
                    } else if ("||".equals(node.getToken().value)) {
                        Token leftBool = context.popToken();
                        if (isVariable(leftBool)) {
                            leftBool = unwrapVariable(leftBool);
                        }
                        context.pushToken(leftBool);

                        if (!leftBool.isBoolean())
                            throw new InterpreterException("Left of || operator should be Boolean but was " + leftBool);

                        boolean result = leftBool.toBoolean();

                        if (result) { //true anyway
                            return;
                        }
                    }
                }
            }
        }

        Integer result = interpret(node);
        if (result != null) {
            switch (result) {
                case Executor.STOP:
                    context.setStopFlag(true);
                    return;
                case Executor.WAIT:
                    context.setWaitFlag(true);
                    synchronized (waitLock) {
                        while (context.isWaitFlag()) {
                            try {
                                waitLock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case Executor.BREAK:
                    context.setBreakFlag(true);
                    return;
                case Executor.CONTINUE:
                    context.setContinueFlag(true);
                    return;
                default:
                    throw new InterpreterException(result + " is not a valid return code!");
            }
        }
    }

    public void startWithContext(Object context) throws InterpreterException {
        started = true;
        startWithContextAndInterrupter(context, null, Timings.LIMBO);
    }

    /**
     * Start interpretation.
     *
     * @param context The context that can be used by Executors. This is usually Event object for Bukkit plugin.
     * @throws InterpreterException
     */
    public void startWithContext(Object context, Timings.Timing timing) throws InterpreterException {
        started = true;
        startWithContextAndInterrupter(context, null, timing);
    }

    /**
     * Start interpretation.
     *
     * @param triggerCause The triggerCause that can be used by Executors.
     *                     This is usually Event object for Bukkit plugin.
     * @param interrupter  gives the caller to interrupt the execution
     * @throws InterpreterException
     */
    public void startWithContextAndInterrupter(Object triggerCause,
                                               ProcessInterrupter interrupter,
                                               Timings.Timing timing) throws InterpreterException {
        this.context.setTriggerCause(triggerCause);
        this.globalContext.interrupter = interrupter;
        this.context.setTiming(timing);

        try (Timings.Timing t = this.context.getTiming()
                .getTiming("Code Interpretation")
                .begin(globalContext.task.isServerThread())) {
            for (int i = 0; i < root.getChildren().size(); i++)
                start(root.getChildren().get(i));
        }
    }

    private Token unwrapVariable(Token varToken) throws InterpreterException {
        if (varToken.type == Type.ID) {
            if (context.getImportMap().containsKey(varToken.value)) {
                Class<?> clazz = context.getImportMap().get(varToken.value);
                return new Token(Type.CLAZZ, clazz, varToken.row, varToken.col);
            }

            Object var = context.getVars().get(varToken.value);

            return parseValue(var, varToken);
        } else if (varToken.type == Type.GID) {
            return parseValue(globalContext.gvars.get(varToken.value), varToken);
        } else if (varToken.type == Type.GID_TEMP) {
            return parseValue(globalContext.gvars.get(new TemporaryGlobalVariableKey((String) varToken.value)),
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

    public static void main(String[] ar) throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String text = "x = null;" + "y = null;" + "x.y.hoho();";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {
            @Override
            public Integer execute(Timings.Timing timing,
                                   Map<String, Object> variables,
                                   Object e,
                                   Object... args) throws Exception {
                return null;
            }
        });

        Map<String, Placeholder> placeholderMap = new HashMap<>();
        HashMap<Object, Object> gvars = new HashMap<>();

        Interpreter interpreter = new Interpreter(root);
        interpreter.setPlaceholderMap(placeholderMap);
        interpreter.globalContext.gvars = gvars;

        interpreter.startWithContext(null);
    }
}
