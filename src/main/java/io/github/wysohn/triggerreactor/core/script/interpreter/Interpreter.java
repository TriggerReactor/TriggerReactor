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
package io.github.wysohn.triggerreactor.core.script.interpreter;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.Token.Type;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.wrapper.Accessor;
import io.github.wysohn.triggerreactor.core.script.wrapper.IScriptObject;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

public class Interpreter {
    private Node root;
    private final Map<String, Executor> executorMap = new HashMap<>();
    private final Map<String, Object> gvars;
    private final Map<String, Object> vars;
    private final SelfReference selfReference;

    private Stack<Token> stack = new Stack<>();

    private Object context = null;
    private ProcessInterrupter interrupter = null;
    private boolean sync = false;

    private boolean stopFlag = false;
    private boolean waitFlag = false;
    private long cooldownEnd = -1;

    private int callArgsSize = 0;
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
    public Interpreter(Node root, Map<String, Executor> executorMap, Map<String, Object> gvars, Map<String, Object> localVars,
            SelfReference selfReference) {
        this.root = root;
        for(Entry<String, Executor> entry : executorMap.entrySet())
            this.executorMap.put(entry.getKey(), entry.getValue());
        this.gvars = gvars;
        this.vars = localVars;
        this.selfReference = selfReference;

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

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
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
            start(child);
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
            start(child);
    }

    //Check if stopFlag is on before pop Token from stack.
    @SuppressWarnings("unchecked")
    private void start(Node node) throws InterpreterException{
        if(stopFlag)
            return;

        if ("IF".equals(node.getToken().value)) {
            start(node.getChildren().get(0));
            if(stopFlag)
                return;

            Token resultToken = stack.pop();

            if(isVariable(resultToken)){
                resultToken = unwrapVariable(resultToken);
            }

            if(resultToken.type == Type.NULLVALUE){
                if(node.getChildren().size() > 2){
                    start(node.getChildren().get(2));
                }
            }else{
                if(resultToken.isBoolean()){
                    boolean result = (boolean) resultToken.value;
                    if (result) {
                        start(node.getChildren().get(1));
                    } else if (node.getChildren().size() > 2) {
                        start(node.getChildren().get(2));
                    }
                }else if(resultToken.isInt()){
                    int value = resultToken.toInt();
                    if(value != 0){
                        start(node.getChildren().get(1));
                    } else if (node.getChildren().size() > 2) {
                        start(node.getChildren().get(2));
                    }
                }else if(resultToken.isDouble()){
                    double value = resultToken.toDouble();
                    if(value != 0.0){
                        start(node.getChildren().get(1));
                    } else if (node.getChildren().size() > 2) {
                        start(node.getChildren().get(2));
                    }
                }else if(resultToken.value != null){//always true if not null
                    start(node.getChildren().get(1));
                }else{
                    throw new InterpreterException("Unexpected token for IF statement! -- " + resultToken);
                }
            }
        } else if ("WHILE".equals(node.getToken().value)) {
            start(node.getChildren().get(0));

            Token resultToken = stack.pop();
            if(!(resultToken.value instanceof Boolean))
                throw new InterpreterException("Unexpected token for WHILE statement! -- " + resultToken);

            while(!stopFlag && (boolean) resultToken.value){
                start(node.getChildren().get(1));
                start(node.getChildren().get(0));
            }
        } else if("FOR".equals(node.getToken().value)){
            start(node.getChildren().get(0));

            if(stopFlag)
                return;
            Token idToken = stack.pop();

            if (node.getChildren().get(1).getToken().type != Type.ITERATOR)
                throw new InterpreterException("Expected <ITERATOR> but found " + node.getChildren().get(1).getToken());
            Node iterNode = node.getChildren().get(1);

            if(iterNode.getChildren().size() == 1){
                start(iterNode.getChildren().get(0));

                if(stopFlag)
                    return;
                Token valueToken = stack.pop();

                if(isVariable(valueToken)){
                    valueToken = unwrapVariable(valueToken);
                }

                if(!valueToken.isIterable())
                    throw new InterpreterException(valueToken+" is not iterable!");

                if (valueToken.isArray()) {
                    for (int i = 0; i < Array.getLength(valueToken.value); i++) {
                        Object obj = Array.get(valueToken.value, i);
                        if (stopFlag)
                            break;

                        assignValue(idToken, parseValue(obj));
                        start(node.getChildren().get(2));
                    }
                } else {
                    for (Object obj : (Iterable<?>) valueToken.value) {
                        if (stopFlag)
                            break;

                        assignValue(idToken, parseValue(obj));
                        start(node.getChildren().get(2));
                    }
                }
            }else if(iterNode.getChildren().size() == 2){
                Node initNode = iterNode.getChildren().get(0);
                if(initNode.getToken().type != Type.INTEGER)
                    throw new InterpreterException("Init value must be an Integer value!");
                start(initNode);

                if(stopFlag)
                    return;
                Token initToken = stack.pop();

                Node limitNode = iterNode.getChildren().get(1);
                start(limitNode);

                if(stopFlag)
                    return;
                Token limitToken = stack.pop();

                if(isVariable(limitToken)){
                    limitToken = unwrapVariable(limitToken);
                }

                if(limitToken.type != Type.INTEGER)
                    throw new InterpreterException("Limit value must be an Integer value!");

                for(int i = initToken.toInt(); !stopFlag && i < limitToken.toInt(); i++){
                    assignValue(idToken, new Token(Type.INTEGER, i));
                    start(node.getChildren().get(2));
                }
            }else{
                throw new InterpreterException("Number of <ITERATOR> must be 1 or 2!");
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
        try{
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

                    return executorMap.get(command).execute(sync, context, args);
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

                switch ((String) node.getToken().value) {
                case "+":
                    if(left.type == Type.STRING || right.type == Type.STRING){
                        stack.push(new Token(Type.STRING, String.valueOf(left.value) + String.valueOf(right.value)));
                    } else{
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
                if("!".equals(node.getToken().value)){
                    Token boolval = stack.pop();

                    if(isVariable(boolval)){
                        boolval = unwrapVariable(boolval);
                    }

                    if (boolval.type == Type.NULLVALUE) {// treat null as false
                        stack.push(new Token(Type.BOOLEAN, true));
                    } else if (boolval.isBoolean()) {
                        stack.push(new Token(Type.BOOLEAN, !boolval.toBoolean()));
                    } else if(boolval.isDouble()){
                        stack.push(new Token(Type.BOOLEAN, boolval.toDouble() == 0.0));
                    } else if(boolval.isInt()){
                        stack.push(new Token(Type.BOOLEAN, boolval.toInt() == 0));
                    } else {
                        throw new InterpreterException("Cannot negate non-boolean value " + boolval);
                    }
                } else {
                    Token right = stack.pop();
                    Token left = stack.pop();

                    if(isVariable(right)){
                        right = unwrapVariable(right);
                    }

                    if(isVariable(left)){
                        left = unwrapVariable(left);
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
                }
            }else if(node.getToken().type == Type.OPERATOR){
                Token right, left;
                switch ((String) node.getToken().value) {
                case "=":
                    right = stack.pop();
                    left = stack.pop();

                    assignValue(left, right);
                    break;
                case ".":
                    right = stack.pop();
                    //function call
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

                        if(left.type == Type.THIS){
                            callFunction(new Token(Type.OBJECT, right.value), new Token(Type.OBJECT, selfReference), args);
                        }else{
                            Token temp = left;

                            if(isVariable(left)){
                                left = unwrapVariable(left);
                            }

                            if(left.getType() == Type.NULLVALUE){
                                throw new InterpreterException("Could not access "+temp+" because it doesn't exist!");
                            }

                            if(left.isObject()){
                                callFunction(right, left, args);
                            }else{
                                Accessor accessor = (Accessor) left.value;

                                Object var;
                                try {
                                    var = accessor.evaluateTarget();
                                } catch (NoSuchFieldException e) {
                                    throw new InterpreterException("Unknown field " + accessor, e);
                                } catch (Exception e) {
                                    throw new InterpreterException("Unknown error " + e.getMessage(), e);
                                }

                                callFunction(right, new Token(Type.EPS, var), args);
                            }
                        }
                    }
                    //field access
                    else{
                        left = stack.pop();

                        if(left.type == Type.THIS){
                            stack.push(right);
                        }else{
                            Token temp = left;

                            if(isVariable(left)){
                                left = unwrapVariable(left);
                            }

                            if(left.getType() == Type.NULLVALUE){
                                throw new InterpreterException("Could not access "+temp+" because it doesn't exist!");
                            }

                            if(left.isObject() || left.isArray()){
                                stack.push(new Token(Type.ACCESS, new Accessor(left.value, (String) right.value)));
                            }else{
                                Accessor accessor = (Accessor) left.value;

                                Object var;
                                try {
                                    var = accessor.evaluateTarget();
                                } catch (NoSuchFieldException e) {
                                    throw new InterpreterException("Unknown field " + accessor, e);
                                } catch (Exception e) {
                                    throw new InterpreterException("Unknown error " + e.getMessage(), e);
                                }

                                stack.push(new Token(Type.ACCESS, new Accessor(var, (String) right.value)));
                            }
                        }
                    }
                    break;
                }
            }else if(node.getToken().type == Type.ARRAYACCESS){
                Token right = stack.pop();
                Token left = stack.pop();

                if(isVariable(left)){
                    left = unwrapVariable(left);
                }

                if(isVariable(right)){
                    right = unwrapVariable(right);
                }

                if(!left.isArray())
                    throw new InterpreterException(left+" is not an array!");

                if(!right.isInt())
                    throw new InterpreterException(right+" is not a valid index for array!");

                stack.push(new Token(Type.ACCESS, new Accessor(left.value, (Integer) right.value)));
            }else if(node.getToken().type == Type.THIS){
                stack.push(node.getToken());
            }else if(node.getToken().type == Type.ID){
                stack.push(node.getToken());
            }else if(node.getToken().type == Type.GID){
                Token keyToken = stack.pop();

                if(isVariable(keyToken)){
                    keyToken = unwrapVariable(keyToken);
                }

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
                stack.push(new Token(node.getToken().type, node.getToken().value));
            }else if(node.getToken().type == Type.NULLVALUE){
                stack.push(new Token(node.getToken().type, "null"));
            }else{
                throw new InterpreterException("Cannot interpret the unknown node "+node.getToken().type.name());
            }
        }catch(Exception e){
            throw new InterpreterException("Error occured while processing Node "+node, e);
        }

        return null;
    }

    private void assignValue(Token id, Token value) throws InterpreterException {
        if(id.type == Type.ACCESS){
            Accessor accessor = (Accessor) id.value;
            try {
                if(value.type == Type.NULLVALUE){
                    accessor.setTargetValue(null);
                }else{
                    accessor.setTargetValue(value.value);
                }
            } catch (NoSuchFieldException e) {
                throw new InterpreterException("Unknown field "+id.value+"."+value.value);
            } catch (Exception e) {
                throw new InterpreterException("Unknown error ", e);
            }
        } else if(id.type == Type.GID){
            if(isVariable(value)){
                value = unwrapVariable(value);
            }

            gvars.put(id.value.toString(), value.value);
        }else if(id.type == Type.ID){
            if(isVariable(value)){
                value = unwrapVariable(value);
            }

            vars.put(id.value.toString(), value.value);
        }else{
            throw new InterpreterException("Cannot assign value to "+id.value.getClass().getSimpleName());
        }
    }

    private void callFunction(Token right, Token left, Object[] args) throws InterpreterException {
        Object result;
        try {
            result = ReflectionUtil.invokeMethod(left.value, (String) right.value, args);
        } catch (NoSuchMethodException e) {
            throw new InterpreterException("Function "+left.value+"."+right.value+" does not exist or parameter types not match.", e);
        } catch (Exception e) {
            throw new InterpreterException("Error executing fuction "+left.value+"."+right.value+"!", e);
        }

        if(result != null){
            if(isPrimitive(result)){
                stack.push(new Token(Type.EPS, result));
            }else{
                stack.push(new Token(Type.OBJECT, result));
            }
        } else {
            stack.push(new Token(Type.NULLVALUE, result));
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

            return parseValue(var);
        }else if(varToken.type == Type.GID){
            return convertValue(gvars, varToken);
        }else if(varToken.type == Type.ACCESS){
            Accessor accessor = (Accessor) varToken.value;
            Object var;
            try {
                var = accessor.evaluateTarget();
            } catch (NoSuchFieldException e) {
                throw new InterpreterException("Unknown field " + accessor, e);
            } catch (Exception e) {
                throw new InterpreterException("Unknown error " + e.getMessage(), e);
            }

            return parseValue(var);
        } else{
            throw new InterpreterException("Unresolved id "+varToken);
        }
    }

    private Token parseValue(Object var) {
        if(var == null){
            return new Token(Type.NULLVALUE, null);
        }else if (var.getClass() == Integer.class) {
            return new Token(Type.INTEGER, var);
        } else if (var.getClass() == Double.class) {
            return new Token(Type.DECIMAL, var);
        } else if (var.getClass() == String.class) {
            return new Token(Type.STRING, var);
        } else if (var.getClass() == Boolean.class) {
            return new Token(Type.BOOLEAN, var);
        } else if(var instanceof IScriptObject){
            return new Token(Type.OBJECT, ((IScriptObject) var).get());
        } else {
            return new Token(Type.OBJECT, var);
        }
    }

    private Token convertValue(Map<String, Object> varMap, Token idToken) throws InterpreterException {
        Object value = varMap.get(idToken.value);

        return parseValue(value);
    }

    private final Executor EXECUTOR_STOP = new Executor() {
        @Override
        public Integer execute(boolean sync, Object context, Object... args) {
            return STOP;
        }
    };
    private final Executor EXECUTOR_WAIT = new Executor() {
        @Override
        public Integer execute(boolean sync, Object context, Object... args) {
            if(sync){
                throw new RuntimeException("WAIT is illegal in sync mode!");
            }

            double secs = args[0] instanceof Double ? (double) args[0] : (int) args[0];
            long later = (long) (secs * 1000);
            Executor.runTaskLater(new Runnable(){
                @Override
                public void run() {
                    synchronized(Interpreter.this){
                        Interpreter.this.waitFlag = false;
                        Interpreter.this.notify();
                    }
                }
            }, later);
            return WAIT;
        }
    };
    private final Executor EXECUTOR_COOLDOWN = new Executor(){
        @Override
        public Integer execute(boolean sync, Object context, Object... args) {
            long mills = Long.parseLong(String.valueOf(args[0])) * 1000L;
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
