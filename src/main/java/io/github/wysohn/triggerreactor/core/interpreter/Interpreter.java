/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
package io.github.wysohn.triggerreactor.core.interpreter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import io.github.wysohn.triggerreactor.core.Token;
import io.github.wysohn.triggerreactor.core.Token.Type;
import io.github.wysohn.triggerreactor.core.parser.Node;
import io.github.wysohn.triggerreactor.core.wrapper.Accessor;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

public class Interpreter {
    private Node root;
    private final Map<String, Executor> executorMap = new HashMap<>();
    private final Map<String, Object> gvars;
    private final Map<String, Object> vars = new HashMap<>();
    private final InterpretCondition condition;

    private Stack<Token> stack = new Stack<>();

    private Object context = null;
    private ProcessInterrupter interrupter = null;

    private boolean stopFlag = false;
    private boolean waitFlag = false;
    private long cooldownEnd = -1;

    private int callArgsSize = 0;
    public Interpreter(Node root, Map<String, Executor> executorMap, Map<String, Object> gvars, InterpretCondition condition) {
        this.root = root;
        for(Entry<String, Executor> entry : executorMap.entrySet())
            this.executorMap.put(entry.getKey(), entry.getValue());
        this.gvars = gvars;
        this.condition = condition;

        initDefaultExecutors();
    }

    private void initDefaultExecutors() {
        executorMap.put("STOP", EXECUTOR_STOP);
        executorMap.put("WAIT", EXECUTOR_WAIT);
        executorMap.put("COOLDOWN", EXECUTOR_COOLDOWN);
    }

    public boolean isStopFlag() {
        return stopFlag;
    }

    public boolean isWaitFlag() {
        return waitFlag;
    }

    public boolean isCooldown() {
        return cooldownEnd != -1;
    }

    public long getCooldownEnd() {
        return cooldownEnd;
    }

    /**
     * Map of local variables. Fill this map with necessary pairs depends on the context.
     * @return
     */
    public Map<String, Object> getVars() {
        return vars;
    }

    /**
     * Start interpretation.
     * @param context The context that can be used by Executors. This is usually Event object for Bukkit plugin.
     * @throws InterpreterException
     */
    public void startWithContext(Object context) throws InterpreterException{
        this.context = context;
        for(Node child : root.getChildren())
            try {
                start(child);
            } catch (InterpreterException e) {
                throw e;
            }
    }

    /**
     * Start interpretation.
     * @param context The context that can be used by Executors. This is usually Event object for Bukkit plugin.
     * @param interupter gives the caller to interrupt the execution
     * @throws InterpreterException
     */
    public void startWithContextAndInterrupter(Object context, ProcessInterrupter interrupter) throws InterpreterException{
        this.context = context;
        this.interrupter = interrupter;

        for(Node child : root.getChildren())
            try {
                start(child);
            } catch (InterpreterException e) {
                throw e;
            }
    }

    private void start(Node node) throws InterpreterException{
        if(stopFlag)
            return;

        if ("IF".equals(node.getToken().value)) {
            start(node.getChildren().get(0));
            boolean result = (boolean) stack.pop().value;
            if (result) {
                start(node.getChildren().get(1));
            } else if (node.getChildren().size() > 2) {
                start(node.getChildren().get(2));
            }
        } else if ("WHILE".equals(node.getToken().value)) {
            start(node.getChildren().get(0));
            while(!stopFlag && (boolean) stack.pop().value){
                start(node.getChildren().get(1));
                start(node.getChildren().get(0));
            }
        } else {
            for(Node child : node.getChildren()){
                start(child);
            }
        }

        Integer result = interpret(node);
        if(result != null){
            switch(result){
            case Executor.STOP:
                stopFlag = true;
                return;
            case Executor.WAIT:
                waitFlag = true;
                synchronized(this){
                    while(waitFlag){
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            default:
                throw new InterpreterException(result +" is not a valid return code!");
            }
        }
    }

    /**
     *
     * @param node
     * @return return codes in Executor. null if execution continues.
     * @throws InterpreterException
     */
    private Integer interpret(Node node) throws InterpreterException {
        if(interrupter != null && interrupter.onNodeProcess(node)){
            return Executor.STOP;
        }

        if (node.getToken().type == Type.BODY
                || "IF".equals(node.getToken().value)
                || "WHILE".equals(node.getToken().value)) {
            return null;
        } else if (node.getToken().type == Type.COMMAND) {
            String command = (String) node.getToken().value;

            Object[] args = new Object[node.getChildren().size()];
            for (int i = args.length - 1; i >= 0 ; i--) {
                Token argument = stack.pop();

                if (isVariable(argument)) {
                    argument = unwrapVariable(argument);
                }

                args[i] = argument.value;
            }

            if (interrupter != null && interrupter.onCommand(context, command, args)) {
                return null;
            } else {
                if (!executorMap.containsKey(command))
                    throw new InterpreterException("No executor named #" + command + " found!");

                return executorMap.get(command).execute(context, args);
            }
        } else if (node.getToken().type == Type.OPERATOR_A) {
            Token right = stack.pop();
            Token left = stack.pop();

            if(isVariable(right)){
                right = unwrapVariable(right);
            }

            if(isVariable(left)){
                left = unwrapVariable(left);
            }

            if(left.getType() == Type.UNKNOWNID || right.getType() == Type.UNKNOWNID){
                throw new InterpreterException("Operation "+left+" "+node.getToken().value+" "+right+" is not valid");
            }

            switch ((String) node.getToken().value) {
            case "+":
                if(left.type == Type.STRING){
                    stack.push(new Token(Type.STRING, ((String) left.value) + String.valueOf(right.value)));
                }else{
                    if(left.isInt() && right.isInt()){
                        int leftVal = left.toInt(), rightVal = right.toInt();
                        stack.push(new Token(left.isInt() && right.isInt() ? Type.INTEGER : Type.DECIMAL, leftVal + rightVal));
                    }else{
                        double leftVal = left.isInt() ? left.toInt() : left.toDouble();
                        double rightVal = right.isInt() ? right.toInt() : right.toDouble();
                        stack.push(new Token(left.isInt() && right.isInt() ? Type.INTEGER : Type.DECIMAL, leftVal + rightVal));
                    }
                }
                break;
            case "-":
                if(left.isInt() && right.isInt()){
                    int leftVal = left.toInt(), rightVal = right.toInt();
                    stack.push(new Token(left.isInt() && right.isInt() ? Type.INTEGER : Type.DECIMAL, leftVal - rightVal));
                }else{
                    double leftVal = left.isInt() ? left.toInt() : left.toDouble();
                    double rightVal = right.isInt() ? right.toInt() : right.toDouble();
                    stack.push(new Token(left.isInt() && right.isInt() ? Type.INTEGER : Type.DECIMAL, leftVal - rightVal));
                }
                break;
            case "*":
                if(left.isInt() && right.isInt()){
                    int leftVal = left.toInt(), rightVal = right.toInt();
                    stack.push(new Token(left.isInt() && right.isInt() ? Type.INTEGER : Type.DECIMAL, leftVal * rightVal));
                }else{
                    double leftVal = left.isInt() ? left.toInt() : left.toDouble();
                    double rightVal = right.isInt() ? right.toInt() : right.toDouble();
                    stack.push(new Token(left.isInt() && right.isInt() ? Type.INTEGER : Type.DECIMAL, leftVal * rightVal));
                }
                break;
            case "/":
                if(left.isInt() && right.isInt()){
                    int leftVal = left.toInt(), rightVal = right.toInt();
                    stack.push(new Token(left.isInt() && right.isInt() ? Type.INTEGER : Type.DECIMAL, leftVal / rightVal));
                }else{
                    double leftVal = left.isInt() ? left.toInt() : left.toDouble();
                    double rightVal = right.isInt() ? right.toInt() : right.toDouble();
                    stack.push(new Token(left.isInt() && right.isInt() ? Type.INTEGER : Type.DECIMAL, leftVal / rightVal));
                }
                break;
            case "%":
                if(left.isInt() && right.isInt()){
                    int leftVal = left.toInt(), rightVal = right.toInt();
                    stack.push(new Token(left.isInt() && right.isInt() ? Type.INTEGER : Type.DECIMAL, leftVal % rightVal));
                }else{
                    double leftVal = left.isInt() ? left.toInt() : left.toDouble();
                    double rightVal = right.isInt() ? right.toInt() : right.toDouble();
                    stack.push(new Token(left.isInt() && right.isInt() ? Type.INTEGER : Type.DECIMAL, leftVal % rightVal));
                }
                break;
            default:
                throw new InterpreterException("Cannot interpret the unknown operator "+node.getToken().value);
            }
        }else if(node.getToken().type == Type.OPERATOR_L){
            Token right = stack.pop();
            Token left = stack.pop();

            if(isVariable(right)){
                right = unwrapVariable(right);
            }

            if(isVariable(left)){
                left = unwrapVariable(left);
            }

            if(left.getType() == Type.UNKNOWNID || right.getType() == Type.UNKNOWNID){
                throw new InterpreterException("Operation "+left+" "+node.getToken().value+" "+right+" is not valid");
            }

            switch ((String) node.getToken().value) {
            case "<":
                stack.push(new Token(Type.BOOLEAN, (left.isInt() ? left.toInt() : left.toDouble()) < (right.isInt()
                        ? right.toInt() : right.toDouble())));
                break;
            case ">":
                stack.push(new Token(Type.BOOLEAN, (left.isInt() ? left.toInt() : left.toDouble()) > (right.isInt()
                        ? right.toInt() : right.toDouble())));
                break;
            case "<=":
                stack.push(new Token(Type.BOOLEAN, (left.isInt() ? left.toInt() : left.toDouble()) <= (right.isInt()
                        ? right.toInt() : right.toDouble())));
                break;
            case ">=":
                stack.push(new Token(Type.BOOLEAN, (left.isInt() ? left.toInt() : left.toDouble()) >= (right.isInt()
                        ? right.toInt() : right.toDouble())));
                break;
            case "==":
                stack.push(new Token(Type.BOOLEAN, left.value.equals(right.value)));
                break;
            case "!=":
                stack.push(new Token(Type.BOOLEAN, !left.value.equals(right.value)));
                break;
            case "&&":
                stack.push(new Token(Type.BOOLEAN, left.toBoolean() && right.toBoolean()));
                break;
            case "||":
                stack.push(new Token(Type.BOOLEAN, left.toBoolean() || right.toBoolean()));
                break;
            }
        }else if(node.getToken().type == Type.OPERATOR){
            Token right, left;
            switch ((String) node.getToken().value) {
            case "=":
                right = stack.pop();
                left = stack.pop();

                if(left.type == Type.ACCESS){
                    Accessor accessor = (Accessor) left.value;
                    try {
                        accessor.setTargetValue(right.value);
                    } catch (NoSuchFieldException e) {
                        throw new InterpreterException("Unknown field "+left.value+"."+right.value);
                    } catch (IllegalArgumentException e) {
                        throw new InterpreterException("Unknown error "+e.getMessage());
                    }
                }else if(left.type == Type.GID){

                    gvars.put(left.value.toString(), right.value);
                }else if(left.type == Type.ID){
                    vars.put(left.value.toString(), right.value);
                }else{
                    throw new InterpreterException("Cannot assign value to "+left.value.getClass().getSimpleName());
                }
                break;
            case ".":
                right = stack.pop();
                if(right.type == Type.CALL){
                    Object[] args = new Object[callArgsSize];
                    for(int i = callArgsSize - 1; i >= 0; i--){
                        Token argument = stack.pop();

                        if (isVariable(argument)) {
                            argument = unwrapVariable(argument);
                        }

                        args[i] = argument.value;
                    }
                    callArgsSize = 0;

                    left = stack.pop();

                    if(isVariable(left)){
                        left = unwrapVariable(left);
                    }
                    if(left.getType() == Type.UNKNOWNID || right.getType() == Type.UNKNOWNID){
                        throw new InterpreterException("Operation "+left+" "+node.getToken().value+" "+right+" is not valid");
                    }

                    if(left.isObject()){
                        callFunction(node, right, left, args);
                    }else{
                        Accessor accessor = (Accessor) left.value;

                        Object var;
                        try {
                            var = accessor.evaluateTarget();
                        } catch (NoSuchFieldException e) {
                            throw new InterpreterException("Unknown field " + accessor);
                        } catch (IllegalArgumentException e) {
                            throw new InterpreterException("Unknown error " + e.getMessage());
                        }

                        callFunction(node, right, new Token(Type.EPS, var), args);
                    }
                }else{
                    left = stack.pop();

                    if(isVariable(left)){
                        left = unwrapVariable(left);
                    }
                    if(left.getType() == Type.UNKNOWNID || right.getType() == Type.UNKNOWNID){
                        throw new InterpreterException("Operation "+left+" "+node.getToken().value+" "+right+" is not valid");
                    }

                    if(left.isObject()){
                        stack.push(new Token(Type.ACCESS, new Accessor(left.value, (String) right.value)));
                    }else{
                        Accessor accessor = (Accessor) left.value;

                        Object var;
                        try {
                            var = accessor.evaluateTarget();
                        } catch (NoSuchFieldException e) {
                            throw new InterpreterException("Unknown field " + accessor);
                        } catch (IllegalArgumentException e) {
                            throw new InterpreterException("Unknown error " + e.getMessage());
                        }

                        stack.push(new Token(Type.ACCESS, new Accessor(var, (String) right.value)));
                    }
                }
                break;
            }
        }else if(node.getToken().type == Type.ID){
            stack.push(node.getToken());
        }else if(node.getToken().type == Type.GID){
            Token keyToken = stack.pop();

            if(keyToken.getType() != Type.STRING){
                throw new InterpreterException(keyToken+" is not a valid global variable id.");
            }

            stack.push(new Token(Type.GID, keyToken.value));
        }else if(node.getToken().type == Type.CALL){
            stack.push(node.getToken());
            callArgsSize = node.getChildren().size();
        }else if(node.getToken().type == Type.STRING){
            stack.push(new Token(node.getToken().type, node.getToken().value));
        }else if(node.getToken().type == Type.INTEGER){
            stack.push(new Token(node.getToken().type, Integer.parseInt((String) node.getToken().value)));
        }else if(node.getToken().type == Type.DECIMAL){
            stack.push(new Token(node.getToken().type, Double.parseDouble((String) node.getToken().value)));
        }else if(node.getToken().type == Type.BOOLEAN){
            stack.push(new Token(node.getToken().type, Boolean.parseBoolean((String) node.getToken().value)));
        }else if(node.getToken().type == Type.EPS){
            stack.push(node.getToken());
        }else{
            throw new InterpreterException("Cannot interpret the unknown node "+node.getToken().type.name());
        }

        return null;
    }

    private void callFunction(Node node, Token right, Token left, Object[] args) throws InterpreterException {
        Object result;
        try {
            result = ReflectionUtil.invokeMethod(left.value, (String) right.value, args);
        } catch (NoSuchMethodException e) {
            throw new InterpreterException("Function "+left.value+"."+right.value+" does not exist.");
        } catch (IllegalArgumentException | InvocationTargetException e) {
            throw new InterpreterException("Error executing fuction "+left.value+"."+right.value+"! -- "+e.getMessage());
        }

        if(result != null){
            if(isPrimitive(result)){
                stack.push(new Token(Type.EPS, result));
            }else{
                stack.push(new Token(Type.OBJECT, result));
            }
        }
    }

    private boolean isPrimitive(Object obj){
        return obj.getClass() == Boolean.class
                || obj.getClass() == Integer.class
                || obj.getClass() == Double.class
                || obj.getClass() == String.class;
    }

    private boolean isVariable(Token token) {
        return token.type == Type.ID
                || token.type == Type.GID
                || token.type == Type.ACCESS;
    }

    private Token unwrapVariable(Token varToken) throws InterpreterException {
        if(varToken.type == Type.ID){
            Object var = vars.get(varToken.value);

            if (var == null) {
                return new Token(Type.UNKNOWNID, varToken.value);
            } else if (var.getClass() == Integer.class) {
                return new Token(Type.INTEGER, var);
            } else if (var.getClass() == Double.class) {
                return new Token(Type.DECIMAL, var);
            } else if (var.getClass() == String.class) {
                return new Token(Type.STRING, var);
            } else if (var.getClass() == Boolean.class) {
                return new Token(Type.BOOLEAN, var);
            } else {
                return new Token(Type.OBJECT, var);
            }
        }else if(varToken.type == Type.GID){
            return convertValue(gvars, varToken);
        }else if(varToken.type == Type.ACCESS){
            Accessor accessor = (Accessor) varToken.value;
            Object var;
            try {
                var = accessor.evaluateTarget();
            } catch (NoSuchFieldException e) {
                throw new InterpreterException("Unknown field " + accessor);
            } catch (IllegalArgumentException e) {
                throw new InterpreterException("Unknown error " + e.getMessage());
            }

            if (var == null) {
                return new Token(Type.UNKNOWNID, varToken.value);
            } else if (var.getClass() == Integer.class) {
                return new Token(Type.INTEGER, var);
            } else if (var.getClass() == Double.class) {
                return new Token(Type.DECIMAL, var);
            } else if (var.getClass() == String.class) {
                return new Token(Type.STRING, var);
            } else if (var.getClass() == Boolean.class) {
                return new Token(Type.BOOLEAN, var);
            } else {
                return new Token(Type.OBJECT, var);
            }
        }else{
            throw new InterpreterException("Unresolved id "+varToken);
        }
    }

    private Token convertValue(Map<String, Object> varMap, Token idToken) throws InterpreterException {
        Object value = varMap.get(idToken.value);
        if (value == null)
            throw new InterpreterException("Cannot find variable " + idToken.value);

        if (value.getClass() == Integer.class) {
            return new Token(Type.INTEGER, value);
        } else if (value.getClass() == Double.class) {
            return new Token(Type.DECIMAL, value);
        } else if (value.getClass() == String.class) {
            return new Token(Type.STRING, value);
        } else if (value.getClass() == Boolean.class) {
            return new Token(Type.BOOLEAN, value);
        } else {
            return new Token(Type.OBJECT, value);
        }
    }

    private final Executor EXECUTOR_STOP = new Executor() {
        @Override
        public Integer execute(Object context, Object... args) {
            return STOP;
        }
    };
    private final Executor EXECUTOR_WAIT = new Executor() {
        @Override
        public Integer execute(Object context, Object... args) {
            double secs = args[0] instanceof Double ? (double) args[0] : (int) args[0];
            Executor.runTaskLater(new Runnable(){
                @Override
                public void run() {
                    synchronized(Interpreter.this){
                        Interpreter.this.waitFlag = false;
                        Interpreter.this.notifyAll();
                    }
                }
            }, (long) (secs * 1000L));
            return WAIT;
        }
    };
    private final Executor EXECUTOR_COOLDOWN = new Executor(){
        @Override
        public Integer execute(Object context, Object... args) {
            long mills = Integer.parseInt((String) args[0]) * 1000L;
            Interpreter.this.cooldownEnd = System.currentTimeMillis() + mills;
            return null;
        }
    };

    public interface ProcessInterrupter{
        /**
         * This will be called every time when a node is processing.
         * @param node the current node
         * @return return true will terminate execution
         */
        boolean onNodeProcess(Node node);

        /**
        *
        * @param commandNode
        * @param args
        * @boolean true if consumed it; false to let interpreter continue working on it.
        */
        boolean onCommand(Object context, String command, Object[] args);
    }
}
