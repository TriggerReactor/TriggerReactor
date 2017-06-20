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
package io.github.wysohn.triggerreactor.core.parser;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import io.github.wysohn.triggerreactor.core.Token;
import io.github.wysohn.triggerreactor.core.Token.Type;
import io.github.wysohn.triggerreactor.core.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.lexer.LexerException;

public class Parser {
    final Lexer lexer;

    private Token token;

    public Parser(Lexer lexer) throws IOException, LexerException {
        this.lexer = lexer;

        nextToken();
    }

    private void nextToken() throws IOException, LexerException{
        token = lexer.getToken();
    }

    private void skipEndLines() throws IOException, LexerException{
        while(token != null && token.type == Type.ENDL)
            nextToken();
    }

    public Node parse() throws IOException, LexerException, ParserException{
        Node root = new Node(new Token(Type.ROOT, "<ROOT>"));
        Node statement = null;
        while((statement = parseStatement()) != null)
            root.getChildren().add(statement);
        return root;
    }

    private Node parseStatement() throws ParserException, IOException, LexerException{
        skipEndLines();
        if(token != null){
            if("IF".equals(token.value)){
                Node ifNode = new Node(token);
                nextToken();

                //condition
                Node condition = parseLogic();
                if(condition == null)
                    throw new ParserException("Could not find condition for IF statement!", this);
                ifNode.getChildren().add(condition);

                //if body
                Node trueBody = new Node(new Token(Type.BODY, "<BODY>"));

                Node codes = null;
                while((codes = parseStatement()) != null
                        && !"ENDIF".equals(codes.getToken().value)
                        && !"ELSE".equals(codes.getToken().value)){
                    trueBody.getChildren().add(codes);
                }
                if(codes == null)
                    throw new ParserException("Could not find ENDIF statement!", this);
                ifNode.getChildren().add(trueBody);

                //else body
                if("ELSE".equals(codes.getToken().value)){
                    Node falseBody = new Node(new Token(Type.BODY, "<BODY>"));

                    while((codes = parseStatement()) != null
                            && !"ENDIF".equals(codes.getToken().value)){
                        falseBody.getChildren().add(codes);
                    }
                    if(codes == null)
                        throw new ParserException("Could not find ENDIF statement!", this);
                    ifNode.getChildren().add(falseBody);
                }

                //return
                return ifNode;
            } else if("ELSE".equals(token.value)) {
                Node elseNode = new Node(token);
                nextToken();
                return elseNode;
            } else if("ENDIF".equals(token.value)){
                Node endIfNode = new Node(token);
                nextToken();
                return endIfNode;
            }
            else if("WHILE".equals(token.value)){
                Node whileNode = new Node(token);
                nextToken();

                Node condition = parseComparison();
                if(condition == null)
                    throw new ParserException("Could not find condition for WHILE statement!", this);
                whileNode.getChildren().add(condition);

                Node body = new Node(new Token(Type.BODY, "<BODY>"));
                Node codes = null;
                while((codes = parseStatement()) != null && !"ENDWHILE".equals(codes.getToken().value)){
                    body.getChildren().add(codes);
                }
                if(codes == null)
                    throw new ParserException("Could not find ENDWHILE statement!", this);
                whileNode.getChildren().add(body);

                return whileNode;
            }
            else if("ENDWHILE".equals(token.value)){
                Node endWhileNode = new Node(token);
                nextToken();
                return endWhileNode;
            }
            else if("FOR".equals(token.value)){
                Node forNode = new Node(token);
                nextToken();

                Node varName = parseId();
                if(varName == null)
                    throw new ParserException("Could not find variable name for FOR statement!", this);
                forNode.getChildren().add(varName);

                if(!"=".equals(token.value))
                    throw new ParserException("Expected '=' but found "+token, this);
                nextToken();

                Node iteration = new Node(new Token(Type.ITERATOR, "<ITERATOR>"));
                forNode.getChildren().add(iteration);
                Node first = parseFactor();
                if(first == null)
                    throw new ParserException("Could not find initial value for FOR statement!", this);
                iteration.getChildren().add(first);

                if(":".equals(token.value)){
                    nextToken();
                    Node second = parseFactor();
                    if(second == null)
                        throw new ParserException("Could not find max limit for FOR statement!", this);
                    iteration.getChildren().add(second);
                }

                Node body = new Node(new Token(Type.BODY, "<BODY>"));
                Node codes = null;
                while((codes = parseStatement()) != null && !"ENDFOR".equals(codes.getToken().value)){
                    body.getChildren().add(codes);
                }
                if(codes == null)
                    throw new ParserException("Could not find ENDFOR statement!", this);

                forNode.getChildren().add(body);

                return forNode;
            }
            else if("ENDFOR".equals(token.value)){
                Node endForNode = new Node(token);
                nextToken();
                return endForNode;
            }
            else if(token.type == Type.ID){
                if(((String) token.value).charAt(0) == '#'){
                    String command = ((String) token.value).substring(1);
                    Node commandNode = new Node(new Token(Type.COMMAND, command));
                    nextToken();

                    List<Node> args = new ArrayList<>();
                    while(token != null && token.type != Type.ENDL){
                        Node node = parseComparison();
                        if(node != null)
                            args.add(node);
                    }
                    commandNode.getChildren().addAll(args);
                    nextToken();

                    return commandNode;
                }else{
                    Node left = parseFactor();
                    if(left == null)
                        throw new ParserException("Expected an Id but found nothing", this);

                    if(token == null || token.type == Type.ENDL)
                        return left;

                    if(!"=".equals(token.value))
                        throw new ParserException("Expected '=' after id ["+left.getToken()+"] but found "+token, this);
                    Node assign = new Node(new Token(Type.OPERATOR, "="));
                    nextToken();

                    Node right = parseLogic();
                    if(right == null)
                        throw new ParserException("Expected logic but found nothing", this);

                    assign.getChildren().add(left);
                    assign.getChildren().add(right);

                    if(token != null && token.type != Type.ENDL)
                        throw new ParserException("Expected end of line but found "+token, this);
                    nextToken();

                    return assign;
                }
            } else if (token.type == Type.OPERATOR && "{".equals(token.value)) {
                nextToken();

                Node left = new Node(new Token(Type.GID, "<GVAR>"));
                Node keyString = parseLogic();

                left.getChildren().add(keyString);

                if (token == null || token.type != Type.OPERATOR || !"}".equals(token.value)) {
                    throw new ParserException("Expected '}' but found " + token, this);
                }
                nextToken();

                if(!"=".equals(token.value))
                    throw new ParserException("Expected '=' after id ["+left.getToken().value+"] but found "+token, this);
                Node assign = new Node(new Token(Type.OPERATOR, "="));
                nextToken();

                Node right = parseLogic();
                if(right == null)
                    throw new ParserException("Expected logic but found nothing", this);

                assign.getChildren().add(left);
                assign.getChildren().add(right);

                if(token != null && token.type != Type.ENDL)
                    throw new ParserException("Expected end of line but found "+token, this);
                nextToken();

                return assign;
            }
            else {
                throw new ParserException("Unexpected token " + token, this);
            }
        }else{
            return null;
        }
    }
/*
    private Node parseAssignment() throws IOException, LexerException, ParserException{
        Node id = parseFactor();
        if(id == null)
            throw new ParserException("Expected Id but found nothing. Token: "+token);

        Node parent = parseAssignmentAndId(id);
        if(parent != null){
            return parent;
        } else {
            return id;
        }
    }

    private Node parseAssignmentAndId(Node left) throws IOException, LexerException, ParserException{
        if(token != null && "=".equals(token.value)){
            Node node = new Node(new Token(Type.ASSIGNMENT, token.value));
            nextToken();

            node.getChildren().add(left);

            Node logic = parseLogic();
            if(logic != null){
                node.getChildren().add(logic);
            }else{
                throw new ParserException("Expected a logic after ["+node.getToken().value+"] but found ["+token+"] !");
            }

            Node assignmentAndLogic = parseAssignmentAndId(node);
            if(assignmentAndLogic != null){
                return assignmentAndLogic;
            }else{
                return node;
            }
        }else{
            throw new ParserException("Unexpected token "+token);
        }
    }*/

    private Node parseLogic() throws IOException, LexerException, ParserException{
        Node comparison = parseComparison();

        Node parent = parseLogicAndComparison(comparison);
        if(parent != null){
            if(parent.getChildren().size() != 2)
                throw new ParserException("Operator "+parent.getToken()+" requires boolean on the left and right of it.", this);

            return parent;
        } else{
            return comparison;
        }
    }

    private Node parseLogicAndComparison(Node left) throws IOException, LexerException, ParserException{
        if(token != null && token.type == Type.OPERATOR_L
                &&("||".equals(token.value) || "&&".equals(token.value))){
            Node node = new Node(token);
            nextToken();

            //insert left expression(or term+expression)
            node.getChildren().add(left);

            Node comparison = parseComparison();
            if(comparison != null){
                //insert right comparison
                node.getChildren().add(comparison);
            }else{
                throw new ParserException("Expected a comparison after ["+node.getToken().value+"] but found ["+token+"] !", this);
            }

            Node logicAndComparison = parseLogicAndComparison(node);
            if(logicAndComparison != null){
                return logicAndComparison;
            }else{
                return node;
            }
        }else{
            return null;
        }
    }

    private Node parseComparison() throws IOException, LexerException, ParserException{
        Node expression = parseExpression();

        if(token != null && token.type == Type.OPERATOR_L
                && ("<".equals(token.value) || "<=".equals(token.value)
                        || ">".equals(token.value) || ">=".equals(token.value)
                        || "==".equals(token.value)  || "!=".equals(token.value) )){
            Node node = new Node(token);
            nextToken();

            node.getChildren().add(expression);

            Node right = parseExpression();
            if(right == null)
                throw new ParserException("Tried to parse expression after '"+token+"' but failed!", this);
            else{
                node.getChildren().add(right);
                if(node.getChildren().size() != 2)
                    throw new ParserException("Comparison "+node.getToken()+" requires number or variable on the left and right of it.", this);

                return node;
            }
        }else{
            return expression;
        }
    }

    private Node parseExpression() throws IOException, LexerException, ParserException{
        Node term = parseTerm();

        Node parent = parseTermAndExpression(term);
        if(parent != null){
            if(parent.getChildren().size() != 2)
                throw new ParserException("Operator "+parent.getToken()+" requires number or variable on the left and right of it.", this);

            return parent;
        } else {
            return term;
        }
    }

    private Node parseTermAndExpression(Node left) throws IOException, LexerException, ParserException{
        if(token != null && token.type == Type.OPERATOR_A
                &&("+".equals(token.value) || "-".equals(token.value))){
            Node node = new Node(token);
            nextToken();

            //insert left expression(or term+expression)
            node.getChildren().add(left);

            Node term = parseTerm();
            if(term != null){
                //insert right term
                node.getChildren().add(term);
            }else{
                throw new ParserException("Expected a term after ["+node.getToken().value+"] but found ["+token+"] !", this);
            }

            Node termAndexpression = parseTermAndExpression(node);
            if(termAndexpression != null){
                return termAndexpression;
            }else{
                return node;
            }
        }else{
            return null;
        }
    }

    private Node parseTerm() throws IOException, LexerException, ParserException{
        Node factor = parseFactor();

        Node parent = parseFactorAndTerm(factor);
        if(parent != null){
            if(parent.getChildren().size() != 2)
                throw new ParserException("Operator "+parent.getToken()+" requires number or variable on the left and right of it.", this);

            return parent;
        }else{
            return factor;
        }
    }

    private Node parseFactorAndTerm(Node left) throws IOException, LexerException, ParserException{
        if(token != null && token.type == Type.OPERATOR_A
                && ("*".equals(token.value) || "/".equals(token.value) || "%".equals(token.value))){
            Node node = new Node(token);
            nextToken();

            node.getChildren().add(left);

            Node factor = parseFactor();
            if(factor != null){
                node.getChildren().add(factor);
            }else{
                throw new ParserException("Expected a factor after ["+node.getToken().value+"] but found ["+token+"] !", this);
            }

            Node factorAndTerm = parseFactorAndTerm(node);
            if(factorAndTerm != null){
                return factorAndTerm;
            }else{
                return node;
            }
        } else {
            return null;
        }
    }

    private Node parseFactor() throws IOException, LexerException, ParserException {
        if("true".equals(token.value) || "false".equals(token.value)){
            Node node = new Node(new Token(Type.BOOLEAN, token.value));
            nextToken();
            return node;
        }

        if("null".equals(token.value)){
            Node node = new Node(new Token(Type.NULLVALUE));
            nextToken();
            return node;
        }

        Node idNode = parseId();
        if(idNode != null){
            return idNode;
        }

        if(token == null)
            return null;

        if (token.type == Type.OPERATOR && "{".equals(token.value)) {
            nextToken();

            Node gvarNode = new Node(new Token(Type.GID, "<GVAR>"));
            Node keyString = parseLogic();

            gvarNode.getChildren().add(keyString);

            if (token == null || token.type != Type.OPERATOR || !"}".equals(token.value)) {
                throw new ParserException("Expected '}' but found " + token, this);
            }
            nextToken();

            return gvarNode;
        }

        if(token.type == Type.OPERATOR && "(".equals(token.value)){
            nextToken();

            Node expression = parseLogic();

            if(token == null || token.type != Type.OPERATOR || !")".equals(token.value)){
                throw new ParserException("Expected ')' but found "+token, this);
            }
            nextToken();

            return expression;
        }

        //do not process command as an Id
        if(token.type == Type.ID
                && ((String) token.value).charAt(0) == '#'){
            return null;
        }

        if (token.type.isLiteral()) {
            Node node = new Node(token);
            nextToken();
            return node;
        }

        //unary minus
        if(token.type == Type.OPERATOR_A && "-".equals(token.value)){
            nextToken();
            if(token.type != Type.INTEGER && token.type != Type.DECIMAL)
                throw new ParserException("Only Integer or Decimal are allowed for unary minus operation!", this);

            Node node = new Node(new Token(token.type, "-"+token.value));
            nextToken();
            return node;
        }

        //negation
        if(token.type == Type.OPERATOR_L && "!".equals(token.value)){
            Node node = new Node(token);
            nextToken();

            Node child = parseFactor();
            node.getChildren().add(child);

            return node;
        }

        throw new ParserException("Unexpected token "+ token, this);
    }

    private Node parseId() throws IOException, LexerException, ParserException {
        if (token.type == Type.ID) {
            Deque<Node> deque = new LinkedList<>();

            Token idToken;
            do{
                if(".".equals(token.value))
                    nextToken();

                idToken = token;
                nextToken();

                //id[i]
                if (token != null && token.type == Type.OPERATOR && token.value.equals("[")) { // array access                                                                                   // access
                    nextToken();

                    Node index = parseExpression();
                    Node arrAccess = new Node(new Token(Type.ARRAYACCESS, "<Array Access>"));

                    if (token == null || !"]".equals(token.value))
                        throw new ParserException("Expected ']' but found " + token, this);
                    nextToken();

                    arrAccess.getChildren().add(new Node(idToken));
                    arrAccess.getChildren().add(index);

                    deque.addLast(arrAccess);
                }
                //id(args)
                else if(token != null && "(".equals(token.value)){//fuction call
                    nextToken();
                    Node call = new Node(new Token(Type.CALL, idToken.value));

                    if(token != null && ")".equals(token.value)){
                        deque.addLast(call);
                        nextToken();
                    }else{
                        call.getChildren().add(parseLogic());
                        while(token != null && ",".equals(token.value)){
                            nextToken();
                            call.getChildren().add(parseLogic());
                        }

                        if(token == null || !")".equals(token.value))
                            throw new ParserException("Extected ')' but end of stream is reached.", this);
                        nextToken();

                        deque.addLast(call);
                    }

                    //id(args)[i]
                    if (token != null && token.type == Type.OPERATOR && token.value.equals("[")) { // array access                                                                                   // access
                        nextToken();

                        Node index = parseExpression();
                        Node arrAccess = new Node(new Token(Type.ARRAYACCESS, "<Array Access>"));

                        if (token == null || !"]".equals(token.value))
                            throw new ParserException("Expected ']' but found " + token, this);
                        nextToken();

                        arrAccess.getChildren().add(index);

                        deque.addLast(arrAccess);
                    }
                }
                //id
                else{
                    deque.addLast(new Node(idToken));
                }
            }while(token != null && ".".equals(token.value));

            if(deque.peekFirst().getToken().type != Type.THIS)
                deque.push(new Node(new Token(Type.THIS, "<This>")));

            return parseId(deque);
        }else{
            return null;
        }
    }

    private Node parseId(Deque<Node> deque){
        Stack<Node> stack = new Stack<>();
        stack.push(deque.pop());

        while(!deque.isEmpty()){
            Node node = new Node(new Token(Type.OPERATOR, "."));
            node.getChildren().add(stack.pop());
            node.getChildren().add(deque.pop());

            stack.push(node);
        }

        return stack.pop();
    }

    public static void main(String[] ar) throws IOException, LexerException, ParserException{
        Charset charset = Charset.forName("UTF-8");
/*        String text = ""
                + "X = 5\n"
                + "str = \"abc\"\n"
                + "WHILE 1 > 0\n"
                + "    str = str + X\n"
                + "    IF player.in.health > 2 && player.in.health > 0\n"
                + "        #MESSAGE 3*4\n"
                + "    ELSE\n"
                + "        #MESSAGE str\n"
                + "    ENDIF\n"
                + "    #MESSAGE player.getTest().in.getHealth()\n"
                + "    player.getTest().in.health = player.getTest().in.getHealth() + 1.2\n"
                + "    #MESSAGE player.in.hasPermission(\"t\")\n"
                + "    X = X - 1\n"
                + "    IF X < 0\n"
                + "        #STOP\n"
                + "    ENDIF\n"
                + "    #WAIT 1\n"
                + "ENDWHILE";*/
/*        String text = ""
                + "rand = common.random(3)\n"
                + "IF rand == 0\n"
                + "#MESSAGE 0\n"
                + "ENDIF\n"
                + "IF rand == 1\n"
                + "#MESSAGE 1\n"
                + "ENDIF\n"
                + "IF rand == 2\n"
                + "#MESSAGE 2\n"
                + "ENDIF";*/
        //String text = "#MESSAGE /mw goto ETC";
        //String text = "#MESSAGE args[0]";
        String text = ""
                + "FOR i = 0:10\n"
                + "    #MESSAGE \"test i=\"+i\n"
                + "ENDFOR\n";
        System.out.println("original: \n"+text);

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        System.out.println(root.toString());

        JFrame frame = new JFrame("Manual Nodes");
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root");
        setNode(rootNode, root);
        JTree tree = new JTree(rootNode);
        JScrollPane scrollPane = new JScrollPane(tree);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.setSize(300, 150);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void setNode(DefaultMutableTreeNode parent, Node node){
        if(node.getChildren().isEmpty()){
            parent.add(new DefaultMutableTreeNode(node.getToken().value));
        }else{
            DefaultMutableTreeNode holder = new DefaultMutableTreeNode(node.getToken().value);
            for(Node child : node.getChildren()){
                setNode(holder, child);
                parent.add(holder);
            }
        }
    }
}
