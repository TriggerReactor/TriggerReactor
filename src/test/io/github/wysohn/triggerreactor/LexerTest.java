package test.io.github.wysohn.triggerreactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.charset.Charset;

import org.junit.Test;

import io.github.wysohn.triggerreactor.core.Token;
import io.github.wysohn.triggerreactor.core.Token.Type;
import io.github.wysohn.triggerreactor.core.lexer.Lexer;

public class LexerTest {

    @Test
    public void testGetToken() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = "#MESSAGE (1+(4/2.0)/3*4-(2/(3*-4)) >= 0)\n"
                + "#MESSAGE \"text\" \"test\"\n";

        Lexer lexer = new Lexer(text, charset);

        assertEquals(new Token(Type.ID, "#MESSAGE"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR, "("), lexer.getToken());
        assertEquals(new Token(Type.INTEGER, "1"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR_A, "+"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR, "("), lexer.getToken());
        assertEquals(new Token(Type.INTEGER, "4"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR_A, "/"), lexer.getToken());
        assertEquals(new Token(Type.DECIMAL, "2.0"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR, ")"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR_A, "/"), lexer.getToken());
        assertEquals(new Token(Type.INTEGER, "3"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR_A, "*"), lexer.getToken());
        assertEquals(new Token(Type.INTEGER, "4"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR_A, "-"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR, "("), lexer.getToken());
        assertEquals(new Token(Type.INTEGER, "2"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR_A, "/"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR, "("), lexer.getToken());
        assertEquals(new Token(Type.INTEGER, "3"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR_A, "*"), lexer.getToken());
        assertEquals(new Token(Type.INTEGER, "-4"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR, ")"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR, ")"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR_L, ">="), lexer.getToken());
        assertEquals(new Token(Type.INTEGER, "0"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR, ")"), lexer.getToken());
        assertEquals(new Token(Type.ENDL, null), lexer.getToken());
        assertEquals(new Token(Type.ID, "#MESSAGE"), lexer.getToken());
        assertEquals(new Token(Type.STRING, "text"), lexer.getToken());
        assertEquals(new Token(Type.STRING, "test"), lexer.getToken());
        assertEquals(new Token(Type.ENDL, null), lexer.getToken());
        assertNull(lexer.getToken());
    }

}
