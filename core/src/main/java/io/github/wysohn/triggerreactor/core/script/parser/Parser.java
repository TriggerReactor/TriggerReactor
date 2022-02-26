/*******************************************************************************
 *     Copyright (C) 2017, 2018 wysohn
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
package io.github.wysohn.triggerreactor.core.script.parser;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.Token.Type;
import io.github.wysohn.triggerreactor.core.script.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.warning.DeprecationWarning;
import io.github.wysohn.triggerreactor.core.script.warning.Warning;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

public class Parser {
    private static final List<DeprecationSupervisor> deprecationSupervisors = new ArrayList<>();

    public static void addDeprecationSupervisor(DeprecationSupervisor ds) {
        deprecationSupervisors.add(ds);
    }

    final Lexer lexer;

    private boolean showWarnings;
    private final List<Warning> warnings = new ArrayList<Warning>();

    private Token token;

    public Parser(Lexer lexer) throws IOException, LexerException, ParserException {
        this.lexer = lexer;

        nextToken();
    }

    private void nextToken() throws IOException, ParserException {
        try {
            token = lexer.getToken();

            if (showWarnings && token != null) {
                int row = lexer.getRow();

                Type type = token.type;
                String value = String.valueOf(token.value);

                if (type == null || value == null)
                    return;

                if (deprecationSupervisors.stream()
                        .anyMatch(deprecationSupervisor -> deprecationSupervisor.isDeprecated(type, value))) {
                    this.warnings.add(new DeprecationWarning(row, value, lexer.getScriptLines()[row - 1]));
                }
            }
        } catch (LexerException lex) {
            ParserException pex = new ParserException("Error occured while processing a token after " + token);
            pex.initCause(lex);
            throw pex;
        }
    }

    private void skipEndLines() throws IOException, LexerException, ParserException {
        while (token != null && token.type == Type.ENDL)
            nextToken();
    }

    public Node parse(boolean showWarnings) throws IOException, LexerException, ParserException {
        this.showWarnings = showWarnings;
        lexer.setWarnings(showWarnings);

        Node root = new Node(new Token(Type.ROOT, "<ROOT>", -1, -1));
        Node statement = null;
        while ((statement = parseStatement()) != null)
            root.getChildren().add(statement);

        List<Warning> lexWarnings = lexer.getWarnings();
        if (lexWarnings != null) {
            this.warnings.addAll(lexWarnings);
        }

        return root;
    }

    public Node parse() throws IOException, LexerException, ParserException {
        return parse(false);
    }

    private Node parseStatement() throws ParserException, IOException, LexerException {
        skipEndLines();
        if (token != null) {
            if (token.type == Type.IMPORT) {
                Token importToken = token;
                nextToken();
                return parseImport(importToken);
            } else if ("TRY".equals(token.value)) {
                Token tryToken = token;
                nextToken();
                return parseTry(tryToken);
            }  else if ("CATCH".equals(token.value)) {
                Node node = new Node(token);
                nextToken();
                return node;
            } else if ("FINALLY".equals(token.value)) {
                Node node = new Node(token);
                nextToken();
                return node;
            } else if ("ENDTRY".equals(token.value)) {
                Node node = new Node(token);
                nextToken();
                return node;
            } else if ("ENDLAMBDA".equals(token.value)) {
                Node node = new Node(token);
                nextToken();
                return node;
            }  else if ("IF".equals(token.value)) {
                Token ifToken = token;
                nextToken();
                return parseIf(ifToken);
            } else if ("ELSEIF".equals(token.value)) {
                Node node = new Node(token);
                nextToken();
                return node;
            } else if ("ELSE".equals(token.value)) {
                Node node = new Node(token);
                nextToken();
                return node;
            } else if ("ENDIF".equals(token.value)) {
                Node node = new Node(token);
                nextToken();
                return node;
            } else if ("WHILE".equals(token.value)) {
                Node whileNode = new Node(token);
                nextToken();

                Node condition = parseBitwise();
                if (condition == null)
                    throw new ParserException("Could not find condition for WHILE statement! " + whileNode.getToken());
                whileNode.getChildren().add(condition);

                Node body = new Node(new Token(Type.BODY, "<BODY>"));
                Node codes = null;
                while ((codes = parseStatement()) != null && !"ENDWHILE".equals(codes.getToken().value)) {
                    body.getChildren().add(codes);
                }
                if (codes == null)
                    throw new ParserException("Could not find ENDWHILE statement! " + whileNode.getToken());
                whileNode.getChildren().add(body);

                return whileNode;
            } else if ("ENDWHILE".equals(token.value)) {
                Node endWhileNode = new Node(token);
                nextToken();
                return endWhileNode;
            } else if ("FOR".equals(token.value)) {
                Node forNode = new Node(token);
                nextToken();

                Node varName = parseId();
                if (varName == null)
                    throw new ParserException("Could not find variable name for FOR statement! " + forNode.getToken());
                forNode.getChildren().add(varName);

                if (!"=".equals(token.value))
                    throw new ParserException("Expected '=' but found " + token);
                nextToken();

                Node iteration = new Node(new Token(Type.ITERATOR, "<ITERATOR>"));
                forNode.getChildren().add(iteration);
                Node first = parseBitShift();
                if (first == null)
                    throw new ParserException("Could not find initial value for FOR statement! " + forNode.getToken());
                iteration.getChildren().add(first);

                if (":".equals(token.value)) {
                    nextToken();
                    Node second = parseBitShift();
                    if (second == null)
                        throw new ParserException("Could not find max limit for FOR statement! " + forNode.getToken());
                    iteration.getChildren().add(second);
                }

                Node body = new Node(new Token(Type.BODY, "<BODY>"));
                Node codes = null;
                while ((codes = parseStatement()) != null && !"ENDFOR".equals(codes.getToken().value)) {
                    body.getChildren().add(codes);
                }
                if (codes == null)
                    throw new ParserException("Could not find ENDFOR statement! " + forNode.getToken());

                forNode.getChildren().add(body);

                return forNode;
            } else if ("ENDFOR".equals(token.value)) {
                Node endForNode = new Node(token);
                nextToken();
                return endForNode;
            } else if ("SYNC".equals(token.value)) {
                Node node = new Node(new Token(Type.SYNC, "<SYNC>", token));
                nextToken();

                Node codes = null;
                while ((codes = parseStatement()) != null && !"ENDSYNC".equals(codes.getToken().value)) {
                    node.getChildren().add(codes);
                }

                if (codes == null)
                    throw new ParserException("Could not find ENDSYNC. Did you forget to put one?");

                return node;
            } else if ("ENDSYNC".equals(token.value)) {
                Node node = new Node(token);
                nextToken();
                return node;
            } else if ("ASYNC".equals(token.value)) {
                Node node = new Node(new Token(Type.ASYNC, "<ASYNC>", token));
                nextToken();

                Node codes = null;
                while ((codes = parseStatement()) != null && !"ENDASYNC".equals(codes.getToken().value)) {
                    node.getChildren().add(codes);
                }

                if (codes == null)
                    throw new ParserException("Could not find ENDASYNC. Did you forget to put one?");

                return node;
            } else if ("ENDASYNC".equals(token.value)) {
                Node node = new Node(token);
                nextToken();
                return node;
            } else if (token.type == Type.ID) {
                if (((String) token.value).charAt(0) == '#') {
                    int row = token.row;
                    int col = token.col;

                    String command = ((String) token.value).substring(1);
                    StringBuilder builder = new StringBuilder(command);
                    nextToken();

                    while (token != null && ":".equals(token.value)) {
                        nextToken();

                        builder.append(":" + token.value);
                        nextToken();
                    }

                    Node commandNode = new Node(new Token(Type.EXECUTOR, builder.toString(), row, col));

                    List<Node> args = new ArrayList<>();
                    if (token != null && token.type != Type.ENDL) {
                        do {
                            Node node = parseLogic();
                            if (node != null)
                                args.add(node);

                            if (token != null && ",".equals(token.value))
                                nextToken();
                        } while ((token != null && token.type != Type.ENDL));
                    }
                    commandNode.getChildren().addAll(args);
                    nextToken();

                    return commandNode;
                } else {
                    return parseAssignment();
                }
            } else if (token.type == Type.OPERATOR && "{".equals(token.value)) {
                Token temp = token;
                nextToken();

                Node left;
                if (token.type == Type.OPERATOR && "?".equals(token.value)) {
                    nextToken();
                    left = new Node(new Token(Type.GID_TEMP, "<GVAR_TEMP>", temp));
                } else {
                    left = new Node(new Token(Type.GID, "<GVAR>", temp));
                }
                Node keyString = parseLogic();

                left.getChildren().add(keyString);

                if (token == null || token.type != Type.OPERATOR || !"}".equals(token.value)) {
                    throw new ParserException("Expected '}' but found " + token);
                }
                nextToken();
                ///////////////////////////////////////////////////////////////

                return parseAssignmentAndLogic(left);
            } else {
                return parseLogic();
            }
        } else {
            return null;
        }
    }

    private Node parseIf(Token ifToken) throws IOException, LexerException, ParserException {
        Node ifNode = new Node(ifToken);

        //condition
        Node condition = parseLogic();
        if (condition == null)
            throw new ParserException("Could not find condition for IF statement! " + ifNode.getToken());
        ifNode.getChildren().add(condition);

        //if body
        Node trueBody = new Node(new Token(Type.BODY, "<BODY>"));

        Node codes = null;
        while (token != null
                && (codes = parseStatement()) != null
                && !"ENDIF".equals(codes.getToken().value)
                && !"ELSE".equals(codes.getToken().value)
                && !"ELSEIF".equals(codes.getToken().value)) {
            trueBody.getChildren().add(codes);
        }
        ifNode.getChildren().add(trueBody);

        if (codes == null) {
            throw new ParserException("Could not find ENDIF statement! " + ifNode.getToken());
        } else if ("ELSEIF".equals(codes.getToken().value)) {//elseif body
            Node falseBody = new Node(new Token(Type.BODY, "<BODY>"));
            falseBody.getChildren().add(parseIf(codes.getToken()));
            ifNode.getChildren().add(falseBody);
        } else if ("ELSE".equals(codes.getToken().value)) { //else body
            Node falseBody = new Node(new Token(Type.BODY, "<BODY>"));
            nextToken();

            while (token != null
                    && (codes = parseStatement()) != null
                    && !"ENDIF".equals(codes.getToken().value)) {
                falseBody.getChildren().add(codes);
            }

            if (!"ENDIF".equals(codes.getToken().value))
                throw new ParserException("Could not find ENDIF statement! " + ifNode.getToken());
            nextToken(); // consume ENDLINE

            ifNode.getChildren().add(falseBody);
        } else {
            if (!"ENDIF".equals(codes.getToken().value))
                throw new ParserException("Could not find ENDIF statement! " + ifNode.getToken());
            nextToken(); //consume ENDLINE
        }

        //return
        return ifNode;
    }

    private Node parseTry(Token tryToken) throws IOException, LexerException, ParserException {
        Node tryNode = new Node(tryToken);

        Node tryBody = new Node(new Token(Type.BODY, "<TRYBODY>"));
        Node codes = null;
        while (token != null
                && (codes = parseStatement()) != null
                && !"CATCH".equals(codes.getToken().value)
                && !"FINALLY".equals(codes.getToken().value)
                && !"ENDTRY".equals(codes.getToken().value)) {
            tryBody.getChildren().add(codes);
        }
        tryNode.getChildren().add(tryBody);

        if (codes == null) {
            throw new ParserException("Could not find ENDTRY statement! " + tryNode.getToken());
        }
        if ("CATCH".equals(codes.getToken().value)) {
            Node catchBody = new Node(new Token(Type.CATCHBODY, "<CATCHBODY>"));

            Node varName = parseId();
            if (varName == null)
                throw new ParserException("Could not find variable name for CATCH statement! " + catchBody.getToken());
            catchBody.getChildren().add(varName);

            nextToken();

            Node catchCodeBody = new Node(new Token(Type.BODY, "<BODY>"));
            while (token != null
                    && (codes = parseStatement()) != null
                    && !"FINALLY".equals(codes.getToken().value)
                    && !"ENDTRY".equals(codes.getToken().value)) {
                catchCodeBody.getChildren().add(codes);
            }

            catchBody.getChildren().add(catchCodeBody);
            tryNode.getChildren().add(catchBody);
        }
        if ("FINALLY".equals(codes.getToken().value)) {
            Node finallyBody = new Node(new Token(Type.FINALLYBODY, "<FINALLYBODY>"));
            nextToken();

            while (token != null
                    && (codes = parseStatement()) != null
                    && !"ENDTRY".equals(codes.getToken().value)) {
                finallyBody.getChildren().add(codes);
            }

            tryNode.getChildren().add(finallyBody);
        }

        if (!"CATCH".equals(codes.getToken().value) && !"FINALLY".equals(codes.getToken().value)) {
            if (!"ENDTRY".equals(codes.getToken().value))
                throw new ParserException("Could not find ENDTRY statement! " + tryNode.getToken());
            nextToken();
        }

        return tryNode;
    }

    private Node parseAssignment() throws IOException, LexerException, ParserException{
        Node id = parseLogic();
        if(id == null)
            throw new ParserException("Expected Id but found nothing. Token: "+token);

        Node parent = parseAssignmentAndLogic(id);
        if(parent != null){
            return parent;
        } else {
            return id;
        }
    }

    private Node parseAssignmentAndLogic(Node leftNode) throws IOException, LexerException, ParserException {
        if (token != null
                && ("+=".equals(token.value) || "-=".equals(token.value) || "*=".equals(token.value)
                || "/=".equals(token.value) || "%=".equals(token.value) || "=".equals(token.value)
                || "<<=".equals(token.value) || ">>=".equals(token.value) || ">>>=".equals(token.value)
                || "&=".equals(token.value) || "^=".equals(token.value) || "|=".equals(token.value))) {
            Node assign = new Node(new Token(Type.OPERATOR, "=", token.row, token.col));
            Token assignToken = token;
            String assignTokenValue = (String) assignToken.value;
            nextToken();

            Node right = parseLogic();
            if (right == null)
                throw new ParserException("Expected an assignable value on the right of " + token + ", but found nothing.");

            assign.getChildren().add(leftNode);
            if ("=".equals(assignTokenValue)) {
                assign.getChildren().add(right);
            } else {
                String op = assignTokenValue.substring(0, assignTokenValue.length() - 1);
                Node operate = new Node(new Token(Type.OPERATOR_A, op, assignToken.row, assignToken.col));
                operate.getChildren().add(leftNode);
                operate.getChildren().add(right);
                assign.getChildren().add(operate);
            }

            if (token != null && token.type != Type.ENDL)
                throw new ParserException("Expected end of line but found " + token);
            nextToken();

            return assign;
        } else {
            return parseLogic();
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
                throw new ParserException("Expected a logic after ["+node.getToken().value+"] but found ["+token+"] ! "+token);
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

    private Node parseLogic() throws IOException, LexerException, ParserException {
        Node bitwise = parseBitwise();

        Node parent = parseLogicAndBitwise(bitwise);
        if (parent != null) {
            if (parent.getChildren().size() != 2)
                throw new ParserException("Operator " + parent.getToken() + " requires boolean on the left and right of it. " + token);

            return parent;
        } else {
            return bitwise;
        }
    }

    private Node parseLogicAndBitwise(Node left) throws IOException, LexerException, ParserException {
        if (token != null && token.type == Type.OPERATOR_L
                && ("||".equals(token.value) || "&&".equals(token.value))) {
            Node node = new Node(token);
            nextToken();

            //insert left expression(or term+expression)
            node.getChildren().add(left);

            Node bitwise = parseBitwise();
            if (bitwise != null) {
                //insert right comparison
                node.getChildren().add(bitwise);
            } else {
                throw new ParserException("Expected a comparison after [" + node.getToken().value + "] but found [" + token + "] ! " + token);
            }

            Node logicAndBitwise = parseLogicAndBitwise(node);
            if (logicAndBitwise != null) {
                return logicAndBitwise;
            } else {
                return node;
            }
        } else {
            return null;
        }
    }

    private Node parseBitwise() throws IOException, LexerException, ParserException {
        return parseBitwise(3);
    }

    private Node parseBitwise(int level) throws IOException, LexerException, ParserException {
        int curLevel = 0;
        Node comparison = parseComparison();
        Node parsedNode = comparison;

        if (level == curLevel)
            return parsedNode;
        curLevel++;

        Node bitwiseAnd = parseBitwiseAnd(parsedNode);
        if (bitwiseAnd != null) {
            if (bitwiseAnd.getChildren().size() != 2)
                throw new ParserException("Operator " + bitwiseAnd.getToken() + " requires number or variable on the left and right of it. " + token);
            parsedNode = bitwiseAnd;
        }

        if (level == curLevel)
            return parsedNode;
        curLevel++;

        Node bitwiseXor = parseBitwiseXor(parsedNode);
        if (bitwiseXor != null) {
            if (bitwiseXor.getChildren().size() != 2)
                throw new ParserException("Operator " + bitwiseXor.getToken() + " requires number or variable on the left and right of it. " + token);
            parsedNode = bitwiseXor;
        }

        if (level == curLevel)
            return parsedNode;

        Node bitwiseOr = parseBitwiseOr(parsedNode);
        if (bitwiseOr != null) {
            if (bitwiseOr.getChildren().size() != 2)
                throw new ParserException("Operator " + bitwiseOr.getToken() + " requires number or variable on the left and right of it. " + token);
            parsedNode = bitwiseOr;
        }

        return parsedNode;
    }

    private Node parseBitwiseAnd(Node left) throws IOException, LexerException, ParserException {
        if (token != null && token.type == Type.OPERATOR_A && ("&".equals(token.value))) {
            Node node = new Node(token);
            nextToken();

            node.getChildren().add(left);

            Node comparison = parseBitwise(0);
            if (comparison != null) {
                node.getChildren().add(comparison);
            } else {
                throw new ParserException("Expected a comparison after [" + node.getToken().value + "] but found [" + token + "] ! " + token);
            }

            Node bitwiseAnd = parseBitwiseAnd(node);
            if (bitwiseAnd != null) {
                return bitwiseAnd;
            } else {
                return node;
            }
        } else {
            return null;
        }
    }

    private Node parseBitwiseXor(Node left) throws IOException, LexerException, ParserException {
        if (token != null && token.type == Type.OPERATOR_A && ("^".equals(token.value))) {
            Node node = new Node(token);
            nextToken();

            node.getChildren().add(left);

            Node bitwiseAnd = parseBitwise(1);
            if (bitwiseAnd != null) {
                node.getChildren().add(bitwiseAnd);
            } else {
                throw new ParserException("Expected a comparison after [" + node.getToken().value + "] but found [" + token + "] ! " + token);
            }

            Node bitwiseXor = parseBitwiseXor(node);
            if (bitwiseXor != null) {
                return bitwiseXor;
            } else {
                return node;
            }
        } else {
            return null;
        }
    }

    private Node parseBitwiseOr(Node left) throws IOException, LexerException, ParserException {
        if (token != null && token.type == Type.OPERATOR_A && ("|".equals(token.value))) {
            Node node = new Node(token);
            nextToken();

            node.getChildren().add(left);

            Node bitwiseXor = parseBitwise(2);
            if (bitwiseXor != null) {
                node.getChildren().add(bitwiseXor);
            } else {
                throw new ParserException("Expected a comparison after [" + node.getToken().value + "] but found [" + token + "] ! " + token);
            }

            Node bitwiseOr = parseBitwiseOr(node);
            if (bitwiseOr != null) {
                return bitwiseOr;
            } else {
                return node;
            }
        } else {
            return null;
        }
    }

    private Node parseComparison() throws IOException, LexerException, ParserException {
        Node bitshift = parseBitShift();

        if (token != null
                && (
                (token.type == Type.OPERATOR_L && ("<".equals(token.value) || "<=".equals(token.value)
                        || ">".equals(token.value) || ">=".equals(token.value)
                        || "==".equals(token.value) || "!=".equals(token.value))
                ) || "IS".equals(token.value))
        ) {
            Node node = new Node(token);
            nextToken();

            node.getChildren().add(bitshift);

            Node right = parseBitShift();
            if (right == null)
                throw new ParserException("Tried to parse expression after '" + token + "' but failed! " + token);
            else {
                node.getChildren().add(right);
                if (node.getChildren().size() != 2)
                    throw new ParserException("Comparison " + node.getToken() + " requires number or variable on the left and right of it. " + token);

                return node;
            }
        } else {
            return bitshift;
        }
    }

    private Node parseBitShift() throws IOException, LexerException, ParserException {
        Node expression = parseExpression();

        Node parent = parseBitShiftOper(expression);
        if (parent != null) {
            if (parent.getChildren().size() != 2)
                throw new ParserException("Operator " + parent.getToken() + " requires number or variable on the left and right of it. " + token);

            return parent;
        } else {
            return expression;
        }
    }

    private Node parseBitShiftOper(Node left) throws IOException, LexerException, ParserException {
        if (token != null && token.type == Type.OPERATOR_A
                && ("<<".equals(token.value) || ">>".equals(token.value) || ">>>".equals(token.value))) {
            Node node = new Node(token);
            nextToken();

            node.getChildren().add(left);

            Node expression = parseExpression();
            if (expression != null) {
                node.getChildren().add(expression);
            } else {
                throw new ParserException("Operator " + node.getToken() + " requires number or variable on the left and right of it. " + token);
            }

            Node bitShift = parseBitShiftOper(node);
            if (bitShift != null) {
                return bitShift;
            } else {
                return node;
            }
        } else {
            return null;
        }
    }

    private Node parseExpression() throws IOException, LexerException, ParserException {
        Node term = parseTerm();

        Node parent = parseTermAndExpression(term);
        if (parent != null) {
            if (parent.getChildren().size() != 2)
                throw new ParserException("Operator " + parent.getToken() + " requires number or variable on the left and right of it. " + token);

            return parent;
        } else {
            return term;
        }
    }

    private Node parseTermAndExpression(Node left) throws IOException, LexerException, ParserException {
        if (token != null && token.type == Type.OPERATOR_A
                && ("+".equals(token.value) || "-".equals(token.value))) {
            if ("-".equals(token.value) && left != null //for negative sign only
                    && (!"*".equals(left.getToken().value)
                    && !"/".equals(left.getToken().value)
                    && !"%".equals(left.getToken().value)
                    && !"+".equals(left.getToken().value)
                    && !"-".equals(left.getToken().value)
                    && !".".equals(left.getToken().value)
                    && !"<<".equals(left.getToken().value)
                    && !">>".equals(left.getToken().value)
                    && !">>>".equals(left.getToken().value)
                    && !"&".equals(left.getToken().value)
                    && !"^".equals(left.getToken().value)
                    && !"|".equals(left.getToken().value)
                    && !"~".equals(left.getToken().value)

                    && left.getToken().type != Type.PLACEHOLDER
                    && left.getToken().type != Type.ID
                    && left.getToken().type != Type.GID
                    && left.getToken().type != Type.GID_TEMP
                    && left.getToken().type != Type.INTEGER
                    && left.getToken().type != Type.DECIMAL

                    && left.getToken().type != Type.OPERATOR_UNARY
            )
            ) {
                //if left node is NOT possible candidate of expression, it can be an unary minus. Just skip to factor
                return parseFactor();
            }

            Node node = new Node(token);
            nextToken();

            //insert left expression(or term+expression)
            node.getChildren().add(left);

            Node term = parseTerm();
            if (term != null) {
                //insert right term
                node.getChildren().add(term);
            } else {
                throw new ParserException("Expected a term after [" + node.getToken().value + "] but found [" + token + "] ! " + token);
            }

            Node termAndexpression = parseTermAndExpression(node);
            if (termAndexpression != null) {
                return termAndexpression;
            } else {
                return node;
            }
        } else {
            return null;
        }
    }

    private Node parseTerm() throws IOException, LexerException, ParserException {
        Node factor = parseFactor();

        Node parent = parseFactorAndTerm(factor);
        if (parent != null) {
            if (parent.getChildren().size() != 2)
                throw new ParserException("Operator " + parent.getToken() + " requires number or variable on the left and right of it. " + token);

            return parent;
        } else {
            return factor;
        }
    }

    private Node parseFactorAndTerm(Node left) throws IOException, LexerException, ParserException {
        if (token != null && token.type == Type.OPERATOR_A
                && ("*".equals(token.value) || "/".equals(token.value) || "%".equals(token.value))) {
            Node node = new Node(token);
            nextToken();

            node.getChildren().add(left);

            Node factor = parseFactor();
            if (factor != null) {
                node.getChildren().add(factor);
            } else {
                throw new ParserException("Expected a factor after [" + node.getToken().value + "] but found [" + token + "] ! " + token);
            }

            Node factorAndTerm = parseFactorAndTerm(node);
            if (factorAndTerm != null) {
                return factorAndTerm;
            } else {
                return node;
            }
        } else {
            return null;
        }
    }

    private Node parseFactor() throws IOException, LexerException, ParserException {
        if (token == null)
            return null;

        if("LAMBDA".equals(token.value)){
            Node lambda = new Node(new Token(Type.LAMBDA, "<LAMBDA>", token));
            nextToken();

            Node parameters = new Node(new Token(Type.PARAMETERS, "<PARAMETERS>", token));
            lambda.getChildren().add(parameters);

            if(token != null && token.getType() == Type.ID) {
                parameters.getChildren().add(new Node(token));
                nextToken();
            }

            if(parameters.getChildren().size() > 0){
                while(token != null && ",".equals(token.value)){
                    nextToken();
                    if(token == null || token.getType() != Type.ID)
                        throw new ParserException("Expected a parameter after a comma but found "+token);

                    parameters.getChildren().add(new Node(token));
                    nextToken();
                }
            }

            if(token == null || !"=>".equals(token.value))
                throw new ParserException("Expected an arrow operator, =>, but found "+token);
            Node body = new Node(new Token(Type.LAMBDABODY, "<LAMBDABODY>", token));
            lambda.getChildren().add(body);
            nextToken();

            Node statement = null;
            while ((statement = parseStatement()) != null) {
                if("ENDLAMBDA".equals(statement.getToken().getValue()))
                    break;
                body.getChildren().add(statement);
            }

            if(body.getChildren().size() < 1)
                throw new ParserException("LAMBDA body should have at least one statement: "+body);

            return lambda;
        }

        if (token.type == Type.ID && ("true".equals(token.value) || "false".equals(token.value))) {
            Node node = new Node(new Token(Type.BOOLEAN, token.value, token.row, token.col));
            nextToken();
            return node;
        }

        if ("null".equals(token.value)) {
            Node node = new Node(new Token(Type.NULLVALUE, null, token.row, token.col));
            nextToken();
            return node;
        }

        if ("$".equals(token.value)) {
            nextToken();
            if (token.type != Type.ID)
                throw new ParserException("Expected to find name of placeholder after $, but found " + token);

            int row = token.row;
            int col = token.col;

            String name = (String) token.value;
            StringBuilder builder = new StringBuilder(name);
            nextToken();

            while (token != null && "@".equals(token.value)) {
                nextToken();

                builder.append("@" + token.value);
                nextToken();
            }

            Node node = new Node(new Token(Type.PLACEHOLDER, builder.toString(), row, col));

            while (token != null && ":".equals(token.value)) {
                nextToken();
                node.getChildren().add(parseFactor());
            }

            return node;
        }

        Node idNode = parsePostUnary();
        if (idNode != null) {
            return idNode;
        }

        if (token.type == Type.OPERATOR && "{".equals(token.value)) {
            nextToken();

            Node gvarNode = null;
            if (token.type == Type.OPERATOR && "?".equals(token.value)) {
                nextToken();
                gvarNode = new Node(new Token(Type.GID_TEMP, "<GVAR_TEMP>"));
            } else {
                gvarNode = new Node(new Token(Type.GID, "<GVAR>"));
            }
            Node keyString = parseLogic();

            gvarNode.getChildren().add(keyString);

            if (token == null || token.type != Type.OPERATOR || !"}".equals(token.value)) {
                throw new ParserException("Expected '}' but found " + token);
            }
            nextToken();

            return gvarNode;
        }

        if (token.type == Type.OPERATOR && "(".equals(token.value)) {
            nextToken();

            Node expression = parseLogic();

            if (token == null || token.type != Type.OPERATOR || !")".equals(token.value)) {
                throw new ParserException("Expected ')' but found " + token);
            }
            nextToken();

            return expression;
        }

        //do not process command as an Id
        if (token.type == Type.ID
                && ((String) token.value).charAt(0) == '#') {
            return null;
        }

        if (token.type.isLiteral()) {
            Node node = new Node(token);
            nextToken();
            return node;
        }

        //unary minus
        if (token.type == Type.OPERATOR_A && "-".equals(token.value)) {
            nextToken();
            if (token.type != Type.INTEGER && token.type != Type.DECIMAL //number
                    && token.type != Type.ID //variable
                    && !"{".equals(token.value) //gvar
                    && !"$".equals(token.value) //placeholder
                    && !"(".equals(token.value) //factor
                    && !"~".equals(token.value) //bitwise complement
            )
                throw new ParserException("Only Number, Variable, or Placeholder are allowed for unary minus operation! " + token);

            Node node = new Node(new Token(Type.OPERATOR_UNARY, "-", token.row, token.col));
            node.getChildren().add(parseFactor());

            return node;
        }

        //pre-unary increment/decrement
        if (token.type == Type.OPERATOR_UNARY) {
            Object tokenValue = token.value;
            nextToken();
            if (token.type != Type.ID //variable
                    && !"{".equals(token.value) //gvar
            )
                throw new ParserException("Only Variable is allowed for unary increment/decrement operation! " + token);

            Node node = new Node(new Token(Type.OPERATOR_UNARY, tokenValue + "expr", token.row, token.col));
            node.getChildren().add(parseFactor());

            return node;
        }

        //negation
        if (token.type == Type.OPERATOR_L && "!".equals(token.value)) {
            Node node = new Node(token);
            nextToken();

            Node child = parseFactor();
            node.getChildren().add(child);

            return node;
        }

        if (token.type == Type.OPERATOR_A && "~".equals(token.value)) {
            Node node = new Node(token);
            nextToken();

            Node child = parseFactor();
            node.getChildren().add(child);

            return node;
        }

        if(token.getType() == Type.ENDL){
            return null;
        }

        throw new ParserException("Unexpected token " + token);
    }

    private Node parsePostUnary() throws IOException, LexerException, ParserException {
        Node left = parseId();

        if(left != null && token != null && token.type == Type.OPERATOR_UNARY) {
            Node node = new Node(new Token(Type.OPERATOR_UNARY, "expr" + token.value, token.row, token.col));
            node.getChildren().add(left);
            nextToken();
            return node;
        }

        return left;
    }

    private Node parseId() throws IOException, LexerException, ParserException {
        if (token.type == Type.ID) {
            Deque<Node> deque = new LinkedList<>();

            Token idToken;
            do {
                if (".".equals(token.value))
                    nextToken();

                idToken = token;
                nextToken();

                //id[i]
                if (token != null && token.type == Type.OPERATOR && token.value.equals("[")) { // array access                                                                                   // access
                    nextToken();

                    Node index = parseBitShift();
                    Node arrAccess = new Node(new Token(Type.ARRAYACCESS, "<Array Access>", idToken));

                    if (token == null || !"]".equals(token.value))
                        throw new ParserException("Expected ']' but found " + token);
                    nextToken();

                    arrAccess.getChildren().add(new Node(idToken));
                    arrAccess.getChildren().add(index);

                    deque.addLast(arrAccess);
                }
                //id(args)
                else if (token != null && "(".equals(token.value)) {//fuction call
                    nextToken();

                    Node call = new Node(new Token(Type.CALL, idToken.value, idToken));

                    if (token != null && ")".equals(token.value)) {
                        deque.addLast(call);
                        nextToken();
                    } else {
                        call.getChildren().add(parseLogic());
                        while (token != null && ",".equals(token.value)) {
                            nextToken();
                            call.getChildren().add(parseLogic());
                        }

                        if (token == null || !")".equals(token.value))
                            throw new ParserException("Extected ')' but end of stream is reached. " + token);
                        nextToken();

                        deque.addLast(call);
                    }

                    //id(args)[i]
                    if (token != null && token.type == Type.OPERATOR && token.value.equals("[")) { // array access                                                                                   // access
                        nextToken();

                        Node index = parseBitShift();
                        Node arrAccess = new Node(new Token(Type.ARRAYACCESS, "<Array Access>", idToken));

                        if (token == null || !"]".equals(token.value))
                            throw new ParserException("Expected ']' but found " + token);
                        nextToken();

                        arrAccess.getChildren().add(index);

                        deque.addLast(arrAccess);
                    }
                }
                //id
                else {
                    if (idToken.type != Type.ID)
                        throw new ParserException("Expected an ID but found " + idToken);
                    deque.addLast(new Node(idToken));
                }
            } while (token != null && ".".equals(token.value));

            if (deque.peekFirst().getToken().type != Type.THIS) {
                deque.push(new Node(new Token(Type.THIS, "<This>", deque.peekFirst().getToken())));
            }

            return parseId(deque);
        } else {
            return null;
        }
    }

    private Node parseId(Deque<Node> deque) {
        Stack<Node> stack = new Stack<>();
        stack.push(deque.pop());

        while (!deque.isEmpty()) {
            Node left = stack.pop();
            Node right = deque.pop();
            Node node = new Node(new Token(Type.OPERATOR, ".", left.getToken()));
            node.getChildren().add(left);
            node.getChildren().add(right);

            stack.push(node);
        }

        return stack.pop();
    }

    private Node parseImport(Token importToken) throws IOException, ParserException {
        Node node = new Node(importToken);

        if (token.type == Type.ENDL) {
            return node;
        }

        String attribute = (String) token.value;
        if ("AS".equalsIgnoreCase(attribute)) {
            nextToken();

            if (token == null || token.type != Type.ID || ((String) token.value).charAt(0) == '#') {
                throw new ParserException("Expected an alphabetic characters or _ after import.as but found " + token);
            }

            node.getChildren().add(new Node(token));
            nextToken();
        } else {
            throw new ParserException("IMPORT expected end of line or ; at the end but found " + token);
        }

        return node;
    }

    public List<Warning> getWarnings() {
        return warnings;
    }

    public static void main(String[] ar) throws IOException, LexerException, ParserException {
        Charset charset = StandardCharsets.UTF_8;
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
/*        String text = ""
                + "FOR i = 0:10\n"
                + "    #TEST:MESSAGE \"test i=\"+i..i\n"
                + "ENDFOR\n";*/
/*        String text = "x = 4.0;"
                + "#TEST1 -1;"
                + "#TEST2 -2.0;"
                + "#TEST3 -$test3;"
                + "#TEST4 -x;";*/
/*        String text = ""
                + "IF args.length == 1 && $haspermission: \"lenz.perms\"\n" +
                "    IF args[0] == \"option\"\n" +
                "        IF {$playername+\".kit\"} != true\n" +
                "            #MESSAGE \"&f[ &c! &f] &ctrue message!\"\n" +
                "            #STOP\n" +
                "        ELSEIF {$playername+\".kit\"}\n" +
                "            {$playername+\".kit\"} = null\n" +
                "            #MESSAGE \"&f[ &c! &f] :: null message.\"\n" +
                "        ELSEIF $haspermission: \"lenz.perms\" == false\n" +
                "            #MESSAGE \"&f[ &c! &f] :: &cfalse message.\"\n" +
                "            #STOP\n" +
                "        ENDIF\n" +
                "    ENDIF\n" +
                "ENDIF";*/
//        String text = "a = 2\n" +
//                "a = ++a * --a - a++ / a--\n" +
//                "a = -(--a) -(++a) -(a++) -(a--)\n" +
//                "a = -(--a) - -(++a) - -(a++) - -(a--)\n";
        String text = "" +
                "abc = LAMBDA =>\n" +
                "    3\n" +
                "ENDLAMBDA\n" +
                "cdf = 55";
        System.out.println("original: \n" + text);

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

    private static void setNode(DefaultMutableTreeNode parent, Node node) {
        if (node.getChildren().isEmpty()) {
            parent.add(new DefaultMutableTreeNode(node.toString()));
        } else {
            DefaultMutableTreeNode holder = new DefaultMutableTreeNode(node.toString());
            for (Node child : node.getChildren()) {
                setNode(holder, child);
                parent.add(holder);
            }
        }
    }
}
