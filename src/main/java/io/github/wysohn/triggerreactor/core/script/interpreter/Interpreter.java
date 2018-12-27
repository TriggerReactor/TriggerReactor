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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.Token.Type;
import io.github.wysohn.triggerreactor.core.script.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.script.parser.Node;
import io.github.wysohn.triggerreactor.core.script.parser.Parser;
import io.github.wysohn.triggerreactor.core.script.wrapper.Accessor;
import io.github.wysohn.triggerreactor.core.script.wrapper.IScriptObject;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

public class Interpreter {
    private Node root;
    private final Map<String, Executor> executorMap = new HashMap<>();
    private final Map<String, Placeholder> placeholderMap = new HashMap<>();
    private final Map<Object, Object> gvars;
    private final Map<String, Object> vars;
    private final SelfReference selfReference;

    private final Map<String, Class<?>> importMap = new HashMap<>();

    private Stack<Token> stack = new Stack<>();

    private Object context = null;
    private ProcessInterrupter interrupter = null;
    private boolean sync = false;

    private boolean stopFlag = false;
    private boolean waitFlag = false;
    private boolean breakFlag = false;
    private boolean continueFlag = false;
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
    public Interpreter(Node root, Map<String, Executor> executorMap, Map<String, Placeholder> placeholderMap, Map<Object, Object> gvars, Map<String, Object> localVars,
            SelfReference selfReference) {
        this.root = root;
        for(Entry<String, Executor> entry : executorMap.entrySet())
            this.executorMap.put(entry.getKey(), entry.getValue());
        for(Entry<String, Placeholder> entry : placeholderMap.entrySet())
            this.placeholderMap.put(entry.getKey(), entry.getValue());
        this.gvars = gvars;
        this.vars = localVars;
        this.selfReference = selfReference;

        initDefaultExecutors();
        initDefaultPlaceholders();
    }

    private void initDefaultExecutors() {
        executorMap.put("STOP", EXECUTOR_STOP);
        executorMap.put("WAIT", EXECUTOR_WAIT);
        executorMap.put("COOLDOWN", EXECUTOR_COOLDOWN);
        executorMap.put("BREAK", EXECUTOR_BREAK);
        executorMap.put("CONTINUE", EXECUTOR_CONTINUE);
    }

    private void initDefaultPlaceholders(){
        placeholderMap.put("cooldown", PLACEHOLDER_COOLDOWN);
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
        startWithContextAndInterrupter(context, null);
    }

    /**
     * Start interpretation.
     * @param context The context that can be used by Executors. This is usually Event object for Bukkit plugin.
     * @param interrupter gives the caller to interrupt the execution
     * @throws InterpreterException
     */
    public void startWithContextAndInterrupter(Object context, ProcessInterrupter interrupter) throws InterpreterException{
        this.context = context;
        this.interrupter = interrupter;

        for(int i = 0; i < root.getChildren().size(); i++)
            start(root.getChildren().get(i));
    }

    //Check if stopFlag is on before pop Token from stack.
    private void start(Node node) throws InterpreterException{
        if(stopFlag)
            return;

        //IF children -- [0] : condition , [1] : true body , [2] : false body(may not exist)
        if ("ELSEIF".equals(node.getToken().value) || "IF".equals(node.getToken().value)) {
            start(node.getChildren().get(0));//[0] condition
            if(stopFlag)
                return;

            Token resultToken = stack.pop();

            if(isVariable(resultToken)){
                resultToken = unwrapVariable(resultToken);
            }

            if(resultToken.type == Type.NULLVALUE){ // null check failed
                if(node.getChildren().size() > 2){
                    start(node.getChildren().get(2));
                }
            }else{ // normal IF statement
                if(resultToken.isBoolean()){
                    boolean result = (boolean) resultToken.value;
                    if (result) {
                        start(node.getChildren().get(1));//[1] true body
                    } else if (node.getChildren().size() > 2) {
                        start(node.getChildren().get(2));//[2] false body
                    }
                }else if(resultToken.isInteger()){
                    int value = resultToken.toInteger();
                    if(value != 0){
                        start(node.getChildren().get(1));
                    } else if (node.getChildren().size() > 2) {
                        start(node.getChildren().get(2));
                    }
                }else if(resultToken.isDecimal()){
                    double value = resultToken.toDecimal();
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
            long start = System.currentTimeMillis();

            Token resultToken = null;
            do {
                start(node.getChildren().get(0));

                if(stack.isEmpty())
                    throw new InterpreterException("Could not find condition for WHILE statement!");

                resultToken = stack.pop();

                if (isVariable(resultToken)) {
                    resultToken = unwrapVariable(resultToken);
                }

                if (!(resultToken.value instanceof Boolean))
                    throw new InterpreterException("Unexpected token for WHILE statement! -- " + resultToken);

                if ((boolean) resultToken.value) {
                    start(node.getChildren().get(1));
                    if(breakFlag){
                        breakFlag = false;
                        break;
                    }

                    breakFlag = false;
                    continueFlag = false;
                } else {
                    break;
                }

                if(sync) {
                    long timeTook = System.currentTimeMillis() - start;
                    if(timeTook > 3000L)
                        throw new InterpreterException("WHILE loop took more than 3 seconds in Server Thread. This is usually "
                                + "considered as 'too long' and can crash the server.");
                }
            } while (!stopFlag);
        } else if("FOR".equals(node.getToken().value)){
            start(node.getChildren().get(0));

            if(stopFlag)
                return;
            Token idToken = stack.pop();

            if(idToken == null)
                throw new InterpreterException("Iteration variable for FOR statement not found!");

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

                        assignValue(idToken, parseValue(obj, valueToken));
                        start(node.getChildren().get(2));
                        if(breakFlag){
                            breakFlag = false;
                            break;
                        }

                        breakFlag = false;
                        continueFlag = false;
                    }
                } else {
                    for (Object obj : (Iterable<?>) valueToken.value) {
                        if (stopFlag)
                            break;

                        assignValue(idToken, parseValue(obj, valueToken));
                        start(node.getChildren().get(2));
                        if(breakFlag){
                            breakFlag = false;
                            break;
                        }

                        breakFlag = false;
                        continueFlag = false;
                    }
                }
            }else if(iterNode.getChildren().size() == 2){
                Node initNode = iterNode.getChildren().get(0);
                start(initNode);

                if(stopFlag)
                    return;
                Token initToken = stack.pop();
                if(isVariable(initToken)) {
                    initToken = unwrapVariable(initToken);
                }

                if(initToken.type != Type.INTEGER)
                    throw new InterpreterException("Init value must be an Integer value! -- "+initToken);

                Node limitNode = iterNode.getChildren().get(1);
                start(limitNode);

                if(stopFlag)
                    return;
                Token limitToken = stack.pop();
                if(isVariable(limitToken)){
                    limitToken = unwrapVariable(limitToken);
                }

                if(limitToken.type != Type.INTEGER)
                    throw new InterpreterException("Limit value must be an Integer value! -- "+limitToken);

                for(int i = initToken.toInteger(); !stopFlag && i < limitToken.toInteger(); i++){
                    assignValue(idToken, new Token(Type.INTEGER, i, iterNode.getToken()));
                    start(node.getChildren().get(2));
                    if(breakFlag){
                        breakFlag = false;
                        break;
                    }

                    breakFlag = false;
                    continueFlag = false;
                }
            }else{
                throw new InterpreterException("Number of <ITERATOR> must be 1 or 2!");
            }

        } else {
            for(int i = 0; i < node.getChildren().size(); i++){
                //ignore rest of body if continue flag is set
                if(continueFlag)
                    continue;

                Node child = node.getChildren().get(i);
                start(child);

                if(i == 0) {
                    if("&&".equals(node.getToken().value)){
                        Token leftBool = stack.pop();
                        if(isVariable(leftBool)){
                            leftBool = unwrapVariable(leftBool);
                        }
                        stack.push(leftBool);

                        if(!leftBool.isBoolean())
                            throw new InterpreterException("Left of && operator should be Boolean but was "+leftBool);

                        boolean result = leftBool.toBoolean();

                        if(!result) { //false anyway
                            return;
                        }
                    }else if("||".equals(node.getToken().value)) {
                        Token leftBool = stack.pop();
                        if(isVariable(leftBool)){
                            leftBool = unwrapVariable(leftBool);
                        }
                        stack.push(leftBool);

                        if(!leftBool.isBoolean())
                            throw new InterpreterException("Left of || operator should be Boolean but was "+leftBool);

                        boolean result = leftBool.toBoolean();

                        if(result) { //true anyway
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
                    stopFlag = true;
                    return;
                case Executor.WAIT:
                    waitFlag = true;
                    synchronized (this) {
                        while (waitFlag) {
                            try {
                                this.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case Executor.BREAK:
                    breakFlag = true;
                    return;
                case Executor.CONTINUE:
                    continueFlag = true;
                    return;
                default:
                    throw new InterpreterException(result + " is not a valid return code!");
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
                    || "ELSEIF".equals(node.getToken().value)
                    || "WHILE".equals(node.getToken().value)) {
                return null;
            } else if("IS".equals(node.getToken().value)){
                Token right = stack.pop();
                Token left = stack.pop();

                if(isVariable(right)){
                    right = unwrapVariable(right);
                }

                if(!(right.value instanceof Class))
                    throw new RuntimeException(right+" is not a Class!");

                if(isVariable(left)){
                    left = unwrapVariable(left);
                }

                Class clazz = (Class) right.value;
                stack.push(new Token(Type.BOOLEAN, clazz.isInstance(left.value), node.getToken()));
            } else if (node.getToken().type == Type.EXECUTOR) {
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

                    return executorMap.get(command).execute(sync, vars, context, args);
                }
            } else if(node.getToken().type == Type.PLACEHOLDER) {
                String placeholderName = (String) node.getToken().value;

                Object[] args = new Object[node.getChildren().size()];
                for(int i = args.length - 1; i >= 0; i--) {
                    Token argument = stack.pop();

                    if(isVariable(argument)) {
                        argument = unwrapVariable(argument);
                    }

                    args[i] = argument.value;
                }

                if (!placeholderMap.containsKey(placeholderName))
                    throw new InterpreterException("No placeholder named $" + placeholderName + " found!");

                Object replaced = placeholderMap.get(placeholderName).parse(context, vars, args);
                if(replaced == null) {
                    replaced = "$"+placeholderName;
                }

                if (replaced instanceof Number) {
                    double d = ((Number) replaced).doubleValue();
                    if (d % 1 == 0) {
                        // whole number
                        stack.push(new Token(Type.INTEGER, (int) d, node.getToken()));
                    } else {
                        stack.push(new Token(Type.DECIMAL, d, node.getToken()));
                    }
                } else {
                    stack.push(new Token(Type.EPS, replaced, node.getToken()));
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

                if (((String) node.getToken().value).equals("+")
                        && (left.type == Type.STRING || right.type == Type.STRING)) {
                    stack.push(new Token(Type.STRING, String.valueOf(left.value) + String.valueOf(right.value), node.getToken()));
                } else {
                    if(!left.isNumeric())
                        throw new InterpreterException("Cannot execute arithmetic operation on non-numeric value ["+left+"]!");

                    if(!right.isNumeric())
                        throw new InterpreterException("Cannot execute arithmetic operation on non-numeric value ["+right+"]!");

                    boolean integer = true;
                    if(left.isDecimal() || right.isDecimal()) {
                        integer = false;
                    }

                    Number result;
                    switch ((String) node.getToken().value) {
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
                        throw new InterpreterException("Cannot interpret the unknown operator "+node.getToken().value);
                    }

                    if(integer) {
                        stack.push(new Token(Type.INTEGER, result.intValue(), node.getToken().row, node.getToken().col));
                    }else {
                        stack.push(new Token(Type.DECIMAL, result.doubleValue(), node.getToken().row, node.getToken().col));
                    }
                }
            }else if(node.getToken().type == Type.UNARYMINUS) {
                Token value = stack.pop();

                if(isVariable(value)){
                    value = unwrapVariable(value);
                }

                if(!value.isNumeric())
                    throw new InterpreterException("Cannot do unary minus operation for non-numeric value "+value);

                stack.push(value.isInteger() ? new Token(Type.INTEGER, -value.toInteger(), value.row, value.col)
                        : new Token(Type.DECIMAL, -value.toDecimal(), value.row, value.col));
            }else if(node.getToken().type == Type.OPERATOR_L){
                if("!".equals(node.getToken().value)){
                    Token boolval = stack.pop();

                    if(isVariable(boolval)){
                        boolval = unwrapVariable(boolval);
                    }

                    if (boolval.type == Type.NULLVALUE) {// treat null as false
                        stack.push(new Token(Type.BOOLEAN, true, node.getToken()));
                    } else if (boolval.isBoolean()) {
                        stack.push(new Token(Type.BOOLEAN, !boolval.toBoolean(), node.getToken()));
                    } else if(boolval.isDecimal()){
                        stack.push(new Token(Type.BOOLEAN, boolval.toDecimal() == 0.0, node.getToken()));
                    } else if(boolval.isInteger()){
                        stack.push(new Token(Type.BOOLEAN, boolval.toInteger() == 0, node.getToken()));
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
                        if(!left.isNumeric() || !right.isNumeric())
                            throw new InterpreterException("Only numeric values can be compared!");

                        stack.push(new Token(Type.BOOLEAN, (left.isInteger() ? left.toInteger() : left.toDecimal()) < (right.isInteger()
                                ? right.toInteger() : right.toDecimal()), node.getToken()));
                        break;
                    case ">":
                        if(!left.isNumeric() || !right.isNumeric())
                            throw new InterpreterException("Only numeric values can be compared!");

                        stack.push(new Token(Type.BOOLEAN, (left.isInteger() ? left.toInteger() : left.toDecimal()) > (right.isInteger()
                                ? right.toInteger() : right.toDecimal()), node.getToken()));
                        break;
                    case "<=":
                        if(!left.isNumeric() || !right.isNumeric())
                            throw new InterpreterException("Only numeric values can be compared!");

                        stack.push(new Token(Type.BOOLEAN, (left.isInteger() ? left.toInteger() : left.toDecimal()) <= (right.isInteger()
                                ? right.toInteger() : right.toDecimal()), node.getToken()));
                        break;
                    case ">=":
                        if(!left.isNumeric() || !right.isNumeric())
                            throw new InterpreterException("Only numeric values can be compared!");

                        stack.push(new Token(Type.BOOLEAN, (left.isInteger() ? left.toInteger() : left.toDecimal()) >= (right.isInteger()
                                ? right.toInteger() : right.toDecimal()), node.getToken()));
                        break;
                    case "==":
                        if (left.type == Type.NULLVALUE || right.type == Type.NULLVALUE) {
                            if (left.value == null && right.value == null) {
                                stack.push(new Token(Type.BOOLEAN, true, node.getToken()));
                            } else if (left.value == null) {
                                stack.push(new Token(Type.BOOLEAN, right.value == null, node.getToken()));
                            } else {
                                stack.push(new Token(Type.BOOLEAN, left.value == null, node.getToken()));
                            }
                        } else {
                            stack.push(new Token(Type.BOOLEAN, left.value.equals(right.value), node.getToken()));
                        }

                        break;
                    case "!=":
                        if (left.type == Type.NULLVALUE || right.type == Type.NULLVALUE) {
                            if (left.value == null && right.value == null) {
                                stack.push(new Token(Type.BOOLEAN, false, node.getToken()));
                            } else if (left.value == null) {
                                stack.push(new Token(Type.BOOLEAN, right.value != null, node.getToken()));
                            } else {
                                stack.push(new Token(Type.BOOLEAN, left.value != null, node.getToken()));
                            }
                        } else {
                            stack.push(new Token(Type.BOOLEAN, !left.value.equals(right.value), node.getToken()));
                        }
                        break;
                    case "&&":
                        stack.push(new Token(Type.BOOLEAN, left.toBoolean() && right.toBoolean(), node.getToken()));
                        break;
                    case "||":
                        stack.push(new Token(Type.BOOLEAN, left.toBoolean() || right.toBoolean(), node.getToken()));
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
                            callFunction(new Token(Type.OBJECT, right.value, node.getToken()),
                                    new Token(Type.OBJECT, selfReference, node.getToken()), args);
                        }else{
                            Token temp = left;

                            if(isVariable(left)){
                                left = unwrapVariable(left);
                            }

                            if(left.getType() == Type.NULLVALUE){
                                throw new InterpreterException("Cannot access "+right+"! "+temp.value+" is null.");
                            }

                            if(left.isObject()){
                                callFunction(right, left, args);
                            } else{
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
                                throw new InterpreterException("Cannot access "+right+"! "+temp.value+" is null.");
                            }

                            if(left.isObject() || left.isArray()){
                                stack.push(new Token(Type.ACCESS, new Accessor(left.value, (String) right.value), node.getToken()));
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

                                stack.push(new Token(Type.ACCESS, new Accessor(var, (String) right.value), node.getToken()));
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

                if(!right.isInteger())
                    throw new InterpreterException(right+" is not a valid index for array!");

                stack.push(new Token(Type.ACCESS, new Accessor(left.value, right.toInteger()), node.getToken()));
            }else if(node.getToken().type == Type.THIS){
                stack.push(node.getToken());
            }else if(node.getToken().type == Type.ID){
                stack.push(node.getToken());
            }else if(node.getToken().type == Type.GID || node.getToken().type == Type.GID_TEMP){
                Token keyToken = stack.pop();

                if(isVariable(keyToken)){
                    keyToken = unwrapVariable(keyToken);
                }

                if(keyToken.getType() != Type.STRING){
                    throw new InterpreterException(keyToken+" is not a valid global variable id.");
                }

                stack.push(new Token(node.getToken().type, keyToken.value, node.getToken()));
            }else if(node.getToken().type == Type.CALL){
                stack.push(node.getToken());
                callArgsSize = node.getChildren().size();
            }else if(node.getToken().type == Type.STRING){
                stack.push(new Token(node.getToken().type, node.getToken().value, node.getToken()));
            }else if(node.getToken().type == Type.INTEGER){
                stack.push(new Token(node.getToken().type, Integer.parseInt((String) node.getToken().value), node.getToken()));
            }else if(node.getToken().type == Type.DECIMAL){
                stack.push(new Token(node.getToken().type, Double.parseDouble((String) node.getToken().value), node.getToken()));
            }else if(node.getToken().type == Type.BOOLEAN){
                stack.push(new Token(node.getToken().type, Boolean.parseBoolean((String) node.getToken().value), node.getToken()));
            }else if(node.getToken().type == Type.EPS){
                stack.push(new Token(node.getToken().type, node.getToken().value, node.getToken()));
            }else if(node.getToken().type == Type.NULLVALUE){
                stack.push(new Token(node.getToken().type, null, node.getToken()));
            }else if(node.getToken().type == Type.IMPORT) {
                Class<?> clazz = Class.forName((String) node.getToken().getValue());
                importMap.put(clazz.getSimpleName(), clazz);
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
        } else if(id.type == Type.GID || id.type == Type.GID_TEMP){
            if(value.type == Type.NULLVALUE) {
                gvars.remove(id.type == Type.GID ? id.value.toString() : new TemporaryGlobalVariableKey(id.value.toString()));
            }else {
                if(isVariable(value)){
                    value = unwrapVariable(value);
                }

                gvars.put(id.type == Type.GID ? id.value.toString() : new TemporaryGlobalVariableKey(id.value.toString()), value.value);
            }
        }else if(id.type == Type.ID){
            if(value.type == Type.NULLVALUE) {
                vars.remove(id.value.toString());
            }else {
                if(isVariable(value)){
                    value = unwrapVariable(value);
                }

                vars.put(id.value.toString(), value.value);
            }
        }else{
            throw new InterpreterException("Cannot assign value to "+id.value.getClass().getSimpleName());
        }
    }

    private void callFunction(Token right, Token left, Object[] args) throws InterpreterException {
        Object result;

        if(importMap.containsKey(right.value)) {
            Class<?> clazz = importMap.get(right.value);

            try {
                result = ReflectionUtil.constructNew(clazz, args);
            } catch (NoSuchMethodException | InstantiationException | IllegalArgumentException | IllegalAccessException e) {
                throw new InterpreterException("Cannot create new instance with "+right+" of "+clazz.getSimpleName(), e);
            }
        } else if(left.type == Type.CLAZZ) {
            Class<?> clazz = (Class<?>) left.value;

            try {
                result = ReflectionUtil.invokeMethod(clazz, (Object) null, (String) right.value, args);
            } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                throw new InterpreterException("Cannot invoke static method "+right+" of "+clazz.getSimpleName()+"!", e);
            }
        } else {
            try {
                result = ReflectionUtil.invokeMethod(left.value, (String) right.value, args);
            } catch (NoSuchMethodException e) {
                throw new InterpreterException("Function "+right+" does not exist or parameter types not match.");
            } catch (Exception e) {
                throw new InterpreterException("Error executing fuction "+right+"!", e);
            }
        }

        if(result != null){
            if(isPrimitive(result)){
                stack.push(new Token(Type.EPS, result, right));
            }else{
                stack.push(new Token(Type.OBJECT, result, right));
            }
        } else {
            stack.push(new Token(Type.NULLVALUE, null, right));
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
                || token.type == Type.GID_TEMP
                || token.type == Type.ACCESS;
    }

    private Token unwrapVariable(Token varToken) throws InterpreterException {
        if(varToken.type == Type.ID){
            if(importMap.containsKey(varToken.value)) {
                Class<?> clazz = importMap.get(varToken.value);
                return new Token(Type.CLAZZ, clazz, varToken.row, varToken.col);
            }

            Object var = vars.get(varToken.value);

            return parseValue(var, varToken);
        }else if(varToken.type == Type.GID){
            return parseValue(gvars.get(varToken.value), varToken);
        }else if(varToken.type == Type.GID_TEMP){
            return parseValue(gvars.get(new TemporaryGlobalVariableKey((String) varToken.value)), varToken);
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

            return parseValue(var, varToken);
        } else{
            throw new InterpreterException("Unresolved id "+varToken);
        }
    }

    private Token parseValue(Object var, Token origin) {
        if(var == null){
            return new Token(Type.NULLVALUE, null, origin);
        }else if (var.getClass() == Integer.class) {
            return new Token(Type.INTEGER, var, origin);
        } else if (var.getClass() == Double.class) {
            return new Token(Type.DECIMAL, var, origin);
        } else if (var.getClass() == String.class) {
            return new Token(Type.STRING, var, origin);
        } else if (var.getClass() == Boolean.class) {
            return new Token(Type.BOOLEAN, var, origin);
        } else if(var instanceof IScriptObject){
            return new Token(Type.OBJECT, ((IScriptObject) var).get(), origin);
        } else {
            return new Token(Type.OBJECT, var, origin);
        }
    }

    private final Executor EXECUTOR_STOP = new Executor() {
        @Override
        public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
            return STOP;
        }
    };
    private final Executor EXECUTOR_BREAK = new Executor() {
        @Override
        public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
            return BREAK;
        }
    };
    private final Executor EXECUTOR_CONTINUE = new Executor() {
        @Override
        public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
            return CONTINUE;
        }
    };
    private final Executor EXECUTOR_WAIT = new Executor() {
        @Override
        public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
            if(sync){
                throw new RuntimeException("WAIT is illegal in sync mode!");
            }

            if(args.length < 1)
                throw new RuntimeException("Missing arguments [Decimal].");

            if(!(args[0] instanceof Number))
                throw new RuntimeException(args[0]+" is not a number!");

            double secs = ((Number) args[0]).doubleValue();
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
        public Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) {
            if(!(args[0] instanceof Number))
                throw new RuntimeException(args[0]+" is not a number!");

            long mills = (long)(((Number) args[0]).doubleValue() * 1000L);
            Interpreter.this.cooldownEnd = System.currentTimeMillis() + mills;
            return null;
        }
    };

    private final Placeholder PLACEHOLDER_COOLDOWN = new Placeholder() {
        @Override
        public Object parse(Object context, Map<String, Object> vars, Object... args) throws Exception {
            return Math.max(0L, Interpreter.this.cooldownEnd - System.currentTimeMillis()) / 1000.0;
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
        * @param context
        * @param args
        * @boolean true if consumed it; false to let interpreter continue working on it.
        */
        boolean onCommand(Object context, String command, Object[] args);
    }

    public static void main(String[] ar) throws Exception{
        Charset charset = Charset.forName("UTF-8");
        String text = "x = null;" +
                "y = null;" +
                "x.y.hoho();";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("TEST", new Executor() {
            @Override
            protected Integer execute(boolean sync, Map<String, Object> vars, Object context, Object... args) throws Exception {
                return null;
            }
        });

        Map<String, Placeholder> placeholderMap = new HashMap<>();
        HashMap<Object, Object> gvars = new HashMap<>();

        Interpreter interpreter = new Interpreter(root, executorMap, placeholderMap, gvars, new HashMap<>(), new CommonFunctions(null));

        interpreter.startWithContext(null);
    }
}
