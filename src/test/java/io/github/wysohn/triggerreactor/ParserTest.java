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
package io.github.wysohn.triggerreactor;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

import io.github.wysohn.triggerreactor.core.Token;
import io.github.wysohn.triggerreactor.core.Token.Type;
import io.github.wysohn.triggerreactor.core.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.Node;
import io.github.wysohn.triggerreactor.core.parser.Parser;
import io.github.wysohn.triggerreactor.core.parser.ParserException;

public class ParserTest {

    @Test
    public void testParse() throws IOException, LexerException, ParserException {
        Charset charset = Charset.forName("UTF-8");
        String text = "#MESSAGE (1+(4/2.0)/3*4-(2/(3*-4)) >= 0)\n"
                + "#MESSAGE \"text\"\n";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Queue<Node> queue = new LinkedList<Node>();

        serializeNode(queue, root);

        assertEquals(new Node(new Token(Type.INTEGER, "1")), queue.poll());
        assertEquals(new Node(new Token(Type.INTEGER, "4")), queue.poll());
        assertEquals(new Node(new Token(Type.DECIMAL, "2.0")), queue.poll());
        assertEquals(new Node(new Token(Type.OPERATOR_A, "/")), queue.poll());
        assertEquals(new Node(new Token(Type.INTEGER, "3")), queue.poll());
        assertEquals(new Node(new Token(Type.OPERATOR_A, "/")), queue.poll());
        assertEquals(new Node(new Token(Type.INTEGER, "4")), queue.poll());
        assertEquals(new Node(new Token(Type.OPERATOR_A, "*")), queue.poll());
        assertEquals(new Node(new Token(Type.OPERATOR_A, "+")), queue.poll());
        assertEquals(new Node(new Token(Type.INTEGER, "2")), queue.poll());
        assertEquals(new Node(new Token(Type.INTEGER, "3")), queue.poll());
        assertEquals(new Node(new Token(Type.INTEGER, "-4")), queue.poll());
        assertEquals(new Node(new Token(Type.OPERATOR_A, "*")), queue.poll());
        assertEquals(new Node(new Token(Type.OPERATOR_A, "/")), queue.poll());
        assertEquals(new Node(new Token(Type.OPERATOR_A, "-")), queue.poll());
        assertEquals(new Node(new Token(Type.INTEGER, "0")), queue.poll());
        assertEquals(new Node(new Token(Type.OPERATOR_L, ">=")), queue.poll());
        assertEquals(new Node(new Token(Type.COMMAND, "MESSAGE")), queue.poll());
        assertEquals(new Node(new Token(Type.STRING, "text")), queue.poll());
        assertEquals(new Node(new Token(Type.COMMAND, "MESSAGE")), queue.poll());
        assertEquals(new Node(new Token(Type.ROOT, "<ROOT>")), queue.poll());
        assertEquals(0, queue.size());
    }

    @Test
    public void testParam() throws IOException, LexerException, ParserException {
        Charset charset = Charset.forName("UTF-8");
        String text = "#SOUND player.getLocation() LEVEL_UP 1.0 1.0";

        Lexer lexer = new Lexer(text, charset);
        Parser parser = new Parser(lexer);

        Node root = parser.parse();
        Queue<Node> queue = new LinkedList<Node>();

        serializeNode(queue, root);

        assertEquals(new Node(new Token(Type.ID, "player")), queue.poll());
        assertEquals(new Node(new Token(Type.CALL, "getLocation")), queue.poll());
        assertEquals(new Node(new Token(Type.OPERATOR, ".")), queue.poll());
        assertEquals(new Node(new Token(Type.ID, "LEVEL_UP")), queue.poll());
        assertEquals(new Node(new Token(Type.DECIMAL, "1.0")), queue.poll());
        assertEquals(new Node(new Token(Type.DECIMAL, "1.0")), queue.poll());
        assertEquals(new Node(new Token(Type.COMMAND, "SOUND")), queue.poll());
        assertEquals(new Node(new Token(Type.ROOT, "<ROOT>")), queue.poll());
        assertEquals(0, queue.size());
    }

    private void serializeNode(Queue<Node> queue, Node node){
        for(Node child : node.getChildren()){
            serializeNode(queue, child);
        }

        queue.add(node);
    }

}
