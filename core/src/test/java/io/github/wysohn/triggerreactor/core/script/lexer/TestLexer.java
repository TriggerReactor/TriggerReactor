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
package io.github.wysohn.triggerreactor.core.script.lexer;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.Token.Type;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestLexer {

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
        assertEquals(new Token(Type.OPERATOR_A, "-"), lexer.getToken());
        assertEquals(new Token(Type.INTEGER, "4"), lexer.getToken());
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

    @Test
    public void testNegation() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = "#MESSAGE !true\n";

        Lexer lexer = new Lexer(text, charset);

        assertEquals(new Token(Type.ID, "#MESSAGE"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR_L, "!"), lexer.getToken());
        assertEquals(new Token(Type.ID, "true"), lexer.getToken());
        assertEquals(new Token(Type.ENDL, null), lexer.getToken());
        assertNull(lexer.getToken());
    }

    @Test
    public void testSemicolon() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = "#MESSAGE !true;#MESSAGE \"next\"";

        Lexer lexer = new Lexer(text, charset);

        assertEquals(new Token(Type.ID, "#MESSAGE"), lexer.getToken());
        assertEquals(new Token(Type.OPERATOR_L, "!"), lexer.getToken());
        assertEquals(new Token(Type.ID, "true"), lexer.getToken());
        assertEquals(new Token(Type.ENDL, null), lexer.getToken());
        assertEquals(new Token(Type.ID, "#MESSAGE"), lexer.getToken());
        assertEquals(new Token(Type.STRING, "next"), lexer.getToken());
        assertNull(lexer.getToken());
    }

    @Test
    public void testEscapeCharacter() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = "#MESSAGE \"HI \\\"X\\\"! \\\\\"";

        Lexer lexer = new Lexer(text, charset);

        assertEquals(new Token(Type.ID, "#MESSAGE"), lexer.getToken());
        assertEquals(new Token(Type.STRING, "HI \"X\"! \\"), lexer.getToken());
        assertNull(lexer.getToken());
    }

    @Test
    public void testEscapeCharacter2() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = "#MESSAGE \"The cost is \\$100\"";

        Lexer lexer = new Lexer(text, charset);

        assertEquals(new Token(Type.ID, "#MESSAGE"), lexer.getToken());
        assertEquals(new Token(Type.STRING, "The cost is $100"), lexer.getToken());
        assertNull(lexer.getToken());
    }

    @Test
    public void testImport() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text;
        Lexer lexer;

        text = "IMPORT some.class.to.import.Something";
        lexer = new Lexer(text, charset);
        assertEquals(new Token(Type.IMPORT, "some.class.to.import.Something"), lexer.getToken());
    }

    @Test
    public void testImporWithNum() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text;
        Lexer lexer;

        text = "IMPORT some.class.with2num.import2.So2met2hing2";
        lexer = new Lexer(text, charset);
        assertEquals(new Token(Type.IMPORT, "some.class.with2num.import2.So2met2hing2"), lexer.getToken());
    }
}
