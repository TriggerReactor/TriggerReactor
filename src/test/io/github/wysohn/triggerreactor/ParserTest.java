package test.io.github.wysohn.triggerreactor;

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

    private void serializeNode(Queue<Node> queue, Node node){
        for(Node child : node.getChildren()){
            serializeNode(queue, child);
        }

        queue.add(node);
    }

}
