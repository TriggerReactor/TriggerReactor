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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import io.github.wysohn.triggerreactor.core.Token;
import io.github.wysohn.triggerreactor.core.Token.Type;
import io.github.wysohn.triggerreactor.core.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.Node;
import io.github.wysohn.triggerreactor.core.parser.Parser;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.core.wrapper.ObjectReference;

public class Interpreter {
    private Node root;
    private final Map<String, Executor> executorMap;
    private final Map<String, Object> gvars;
    private final Map<String, Object> vars = new HashMap<>();
    private final InterpretCondition condition;

    private Stack<Token> stack = new Stack<>();

    private Object context = null;

    private boolean stopFlag = false;
    private boolean waitFlag = false;
    private boolean cooldownFlag = false;

    private boolean callReturn = false;
    public Interpreter(Node root, Map<String, Executor> executorMap, Map<String, Object> gvars, InterpretCondition condition) {
        this.root = root;
        this.executorMap = executorMap;
        this.gvars = gvars;
        this.condition = condition;

        initDefaultExecutors();
    }

    private void initDefaultExecutors() {
        executorMap.put("STOP", EXECUTOR_STOP);
        executorMap.put("WAIT", EXECUTOR_WAIT);
    }

    public boolean isStopFlag() {
        return stopFlag;
    }

    public boolean isWaitFlag() {
        return waitFlag;
    }

    public boolean isCooldown() {
        return cooldownFlag;
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
            case Executor.COOLDOWN:
                cooldownFlag = true;
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
        if (node.getToken().type == Type.BODY
                || "IF".equals(node.getToken().value)
                || "WHILE".equals(node.getToken().value)) {
            return null;
        } else if (node.getToken().type == Type.COMMAND) {
            String command = (String) node.getToken().value;
            if (!executorMap.containsKey(command))
                throw new InterpreterException("No executor named #" + command + " found!");

            Object[] args = new Object[node.getChildren().size()];
            for (int i = args.length - 1; i >= 0 ; i--) {
                Token argument = stack.pop();

                if (isVariable(argument)) {
                    argument = unwrapVariable(argument);
                }

                if(argument.value instanceof ObjectReference){
                    Token fieldTarget = stack.pop();
                    argument = new Token(Type.OBJECT, ((ObjectReference) argument.value).getFieldValue(fieldTarget.value.toString(), null));
                }

                args[i] = argument.value;
            }

            return executorMap.get(command).execute(context, args);
        } else if (node.getToken().type == Type.OPERATOR_A) {
            Token right = stack.pop();
            Token left = stack.pop();

            if(isVariable(right)){
                right = unwrapVariable(right);
            }

            if(isVariable(left)){
                left = unwrapVariable(left);
            }

            if(left.value instanceof ObjectReference){
                Token fieldTarget = stack.pop();
                left = new Token(Type.OBJECT, ((ObjectReference) left.value).getFieldValue(fieldTarget.value.toString(), null));
            }

            if(right.value instanceof ObjectReference){
                Token fieldTarget = stack.pop();
                right = new Token(Type.OBJECT, ((ObjectReference) right.value).getFieldValue(fieldTarget.value.toString(), null));
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

            if(left.value instanceof ObjectReference){
                Token fieldTarget = stack.pop();
                left = new Token(Type.OBJECT, ((ObjectReference) left.value).getFieldValue(fieldTarget.value.toString(), null));
            }

            if(right.value instanceof ObjectReference){
                Token fieldTarget = stack.pop();
                right = new Token(Type.OBJECT, ((ObjectReference) right.value).getFieldValue(fieldTarget.value.toString(), null));
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
                stack.push(new Token(Type.BOOLEAN, (left.isInt() ? left.toInt() : left.toDouble()) == (right.isInt()
                        ? right.toInt() : right.toDouble())));
                break;
            case "!=":
                stack.push(new Token(Type.BOOLEAN, (left.isInt() ? left.toInt() : left.toDouble()) != (right.isInt()
                        ? right.toInt() : right.toDouble())));
                break;
            case "&&":
                stack.push(new Token(Type.BOOLEAN, left.toBoolean() && right.toBoolean()));
                break;
            case "||":
                stack.push(new Token(Type.BOOLEAN, left.toBoolean() || right.toBoolean()));
                break;
            }
        }else if(node.getToken().type == Type.OPERATOR){
            switch ((String) node.getToken().value) {
            case "=":
                Token right = stack.pop(); //can be access name
                Token left = stack.pop(); //can be reference

                if(left.value instanceof ObjectReference){
                    Token accessName = stack.pop();

                    ObjectReference reference = (ObjectReference) left.value;
                    reference.setFieldValue(accessName.value.toString(), right.value);
                }else{
                    if(left.type == Type.GID){
                        gvars.put(left.value.toString(), right.value);
                    }else if(left.type == Type.ID){
                        vars.put(left.value.toString(), right.value);
                    }else{
                        throw new InterpreterException("Cannot assign value to "+left.value.getClass().getSimpleName());
                    }
                }
                break;
            case ".":
                if(callReturn){
                    callReturn = false;
                    return null;
                }

                Token right1 = stack.pop();
                Token left1 = stack.pop();

                if(isVariable(left1)){
                    left1 = unwrapVariable(left1);
                }
                if(!(left1.value instanceof ObjectReference))
                    throw new InterpreterException("Expected ObjectReference at left of "+right1+" but found "+left1);

                Object obj = ((ObjectReference) left1.value).getFieldValue(right1.value.toString(), null);
                if(obj == null)
                    throw new InterpreterException("No such field "+right1.value);

                if(isPrimitive(obj)){
                    stack.push(new Token(Type.ID, right1.value.toString()));
                    stack.push(left1);
                }else{
                    stack.push(new Token(Type.OBJECT, new ObjectReference(obj, right1.value.toString())));
                }

                break;
            }
        }else if(node.getToken().type == Type.CALL){
            Object[] args = new Object[node.getChildren().size()];
            for(int i = args.length - 1; i >= 0; i--){
                args[i] = stack.pop().value;
            }

            Token parent = stack.pop();
            if(isVariable(parent)){
                parent = unwrapVariable(parent);
            }

            if(!(parent.value instanceof ObjectReference))
                throw new InterpreterException("Expected an ObjectReference at left of "+node.getToken()+" but found "+parent);

            ObjectReference ref = (ObjectReference) parent.value;
            Object result = ref.invokeMethod((String) node.getToken().value, args);

            if(isPrimitive(result))
                stack.push(new Token(Type.OBJECT, result));
            else
                stack.push(new Token(Type.OBJECT, new ObjectReference(result, (String) node.getToken().value)));
            callReturn = true;
        }else if(node.getToken().type == Type.ID || node.getToken().type == Type.GID){
            stack.push(node.getToken());
        }else if(node.getToken().type == Type.STRING){
            stack.push(new Token(node.getToken().type, node.getToken().value));
        }else if(node.getToken().type == Type.INTEGER){
            stack.push(new Token(node.getToken().type, Integer.parseInt((String) node.getToken().value)));
        }else if(node.getToken().type == Type.DECIMAL){
            stack.push(new Token(node.getToken().type, Double.parseDouble((String) node.getToken().value)));
        }else if(node.getToken().type == Type.BOOLEAN){
            stack.push(new Token(node.getToken().type, Boolean.parseBoolean((String) node.getToken().value)));
        }else if(node.getToken().type == Type.OBJECT){
            stack.push(node.getToken());
        }else{
            throw new InterpreterException("Cannot interpret the unknown node "+node.getToken().type.name());
        }

        return null;
    }

    private boolean isPrimitive(Object obj){
        return obj.getClass() == Boolean.class
                || obj.getClass() == Integer.class
                || obj.getClass() == Double.class
                || obj.getClass() == String.class;
    }

    private boolean isVariable(Token token) {
        return token.type == Type.ID || token.type == Type.GID;
    }

    private Token unwrapVariable(Token idToken) throws InterpreterException {
        if(idToken.type == Type.ID){
            Object var = vars.get(idToken.value);
            if(var == null)
                throw new InterpreterException("Unresolved id "+idToken);

            if (var.getClass() == Integer.class) {
                return new Token(Type.INTEGER, var);
            } else if (var.getClass() == Double.class) {
                return new Token(Type.DECIMAL, var);
            } else if (var.getClass() == String.class) {
                return new Token(Type.STRING, var);
            } else if (var.getClass() == Boolean.class) {
                return new Token(Type.BOOLEAN, var);
            } else if (var instanceof ObjectReference){
                return new Token(Type.OBJECT, var);
            } else {
                throw new InterpreterException("Unresolved id "+idToken);
            }
        }else if(idToken.type == Type.GID){
            return convertValue(gvars, idToken);
        }else{
            throw new InterpreterException("Unresolved id "+idToken);
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

    public static void main(String[] ar) throws IOException, LexerException, ParserException, InterpreterException{
        Charset charset = Charset.forName("UTF-8");
        String text = ""
                + "X = 5\n"
                + "str = \"abc\"\n"
                + "WHILE 1 > 0\n"
                + "    str = str + X\n"
                + "    IF player.in.health > 2 && player.in.health > 0\n"
                + "        #MESSAGE 3*4\n"
                + "    ELSE\n"
                + "        #MESSAGE str\n"
                + "    ENDIF\n"
                + "    #MESSAGE text\n"
                + "    player = player.getTest().in.getHealth() + 1.2\n"
                + "    #MESSAGE player.in.hasPermission(\"t\")\n"
                + "    X = X - 1\n"
                + "    IF X < 0\n"
                + "        #STOP\n"
                + "    ENDIF\n"
                + "    #WAIT 1\n"
                + "ENDWHILE";
        //String text = "#MESSAGE \"beh\"+{player.health}";
        System.out.println("original: \n"+text);

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put("MESSAGE", new Executor(){
            @Override
            protected Integer execute(Object context, Object... args) {
                System.out.println(args[0]);
                return null;
            }
        });
        Test reference = new Test();
        Interpreter interpreter = new Interpreter(root, executorMap, new HashMap<String, Object>() {
            {

            }
        }, null);
        interpreter.getVars().put("player", new ObjectReference(reference, "player"));
        interpreter.getVars().put("text", "hello");

        System.out.println();
        System.out.println("result: ");
        interpreter.startWithContext(null);

        System.out.println();
        System.out.println("In player.getTest().in.getHealth(): "+reference.getTest().in.getHealth());
    }

    private static class Test{
        public InTest in = new InTest();
        public InTest getTest(){
            return in;
        }
    }

    private static class InTest{
        public InTest2 in = new InTest2();
        public double health = 0.82;
        public boolean hasPermission(String tt){
            return tt.equals("tt");
        }
        public InTest2 getTest(){
            return in;
        }
    }

    private static class InTest2{
        public double health = 5.23;
        public double getHealth(){
            return health;
        }
    }
}
