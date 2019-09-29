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

import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestLexer {

    private static void testToken(Lexer lexer, Type id, String s) throws IOException, LexerException {
        assertEquals(new Token(id, s), lexer.getToken());
    }

    private static void testEnd(Lexer lexer) throws IOException, LexerException {
        assertNull(lexer.getToken());
    }

    @Test
    public void testGetToken() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = "#MESSAGE (1+(4/2.0)/3*4-(2/(3*-4)) >= 0)\n"
                + "#MESSAGE \"text\" \"test\"\n";

        Lexer lexer = new Lexer(text, charset);

        testToken(lexer, Type.ID, "#MESSAGE");
        testToken(lexer, Type.OPERATOR, "(");
        testToken(lexer, Type.INTEGER, "1");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.OPERATOR, "(");
        testToken(lexer, Type.INTEGER, "4");
        testToken(lexer, Type.OPERATOR_A, "/");
        testToken(lexer, Type.DECIMAL, "2.0");
        testToken(lexer, Type.OPERATOR, ")");
        testToken(lexer, Type.OPERATOR_A, "/");
        testToken(lexer, Type.INTEGER, "3");
        testToken(lexer, Type.OPERATOR_A, "*");
        testToken(lexer, Type.INTEGER, "4");
        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.OPERATOR, "(");
        testToken(lexer, Type.INTEGER, "2");
        testToken(lexer, Type.OPERATOR_A, "/");
        testToken(lexer, Type.OPERATOR, "(");
        testToken(lexer, Type.INTEGER, "3");
        testToken(lexer, Type.OPERATOR_A, "*");
        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.INTEGER, "4");
        testToken(lexer, Type.OPERATOR, ")");
        testToken(lexer, Type.OPERATOR, ")");
        testToken(lexer, Type.OPERATOR_L, ">=");
        testToken(lexer, Type.INTEGER, "0");
        testToken(lexer, Type.OPERATOR, ")");
        testToken(lexer, Type.ENDL, null);
        testToken(lexer, Type.ID, "#MESSAGE");
        testToken(lexer, Type.STRING, "text");
        testToken(lexer, Type.STRING, "test");
        testToken(lexer, Type.ENDL, null);
        testEnd(lexer);
    }

    @Test
    public void testNegation() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = "#MESSAGE !true\n";

        Lexer lexer = new Lexer(text, charset);

        testToken(lexer, Type.ID, "#MESSAGE");
        testToken(lexer, Type.OPERATOR_L, "!");
        testToken(lexer, Type.ID, "true");
        testToken(lexer, Type.ENDL, null);
        testEnd(lexer);
    }

    @Test
    public void testSemicolon() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = "#MESSAGE !true;#MESSAGE \"next\"";

        Lexer lexer = new Lexer(text, charset);

        testToken(lexer, Type.ID, "#MESSAGE");
        testToken(lexer, Type.OPERATOR_L, "!");
        testToken(lexer, Type.ID, "true");
        testToken(lexer, Type.ENDL, null);
        testToken(lexer, Type.ID, "#MESSAGE");
        testToken(lexer, Type.STRING, "next");
        testEnd(lexer);
    }

    @Test
    public void testEscapeCharacter() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text = "#MESSAGE \"HI \\\"X\\\"! \\\\\"";

        Lexer lexer = new Lexer(text, charset);

        testToken(lexer, Type.ID, "#MESSAGE");
        testToken(lexer, Type.STRING, "HI \"X\"! \\");
        testEnd(lexer);
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
        testToken(lexer, Type.IMPORT, "some.class.to.import.Something");
        testEnd(lexer);
    }

    @Test
    public void testImporWithNum() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text;
        Lexer lexer;

        text = "IMPORT some.class.with2num.import2.So2met2hing2";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.IMPORT, "some.class.with2num.import2.So2met2hing2");
        testEnd(lexer);
    }

    @Test
    public void testComment() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text;
        Lexer lexer;

        text = "/5";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.OPERATOR_A, "/");
        testToken(lexer, Type.INTEGER, "5");
        testEnd(lexer);

        text = "1//hey";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.INTEGER, "1");
        testEnd(lexer);

        text = "2/*hey*/+3";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.INTEGER, "2");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.INTEGER, "3");
        testEnd(lexer);
    }

    @Test
    public void testNumber() throws Exception {

    }

    @Test
    public void testString() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text;
        Lexer lexer;

        text = "\"hey $playername !\"";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "hey ");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.OPERATOR, "$");
        testToken(lexer, Type.ID, "playername");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.STRING, " !");
        testEnd(lexer);

        text = "\"my name is $playername\"";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "my name is ");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.OPERATOR, "$");
        testToken(lexer, Type.ID, "playername");
        testEnd(lexer);

        text = "\"hey ${playername}!\"";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "hey ");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.OPERATOR, "$");
        testToken(lexer, Type.ID, "playername");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.STRING, "!");
        testEnd(lexer);

        text = "\"hey ${playername}\"";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "hey ");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.OPERATOR, "$");
        testToken(lexer, Type.ID, "playername");
        testEnd(lexer);

        text = "\"my name is $playername:5:\\\"2\\\":3 \"";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "my name is ");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.OPERATOR, "$");
        testToken(lexer, Type.ID, "playername");
        testToken(lexer, Type.OPERATOR, ":");
        testToken(lexer, Type.INTEGER, "5");
        testToken(lexer, Type.OPERATOR, ":");
        testToken(lexer, Type.STRING, "2");
        testToken(lexer, Type.OPERATOR, ":");
        testToken(lexer, Type.INTEGER, "3");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.STRING, " ");
        testEnd(lexer);

        text = "\"hey ${playername:7:8:\\\"9\\\"}\"";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "hey ");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.OPERATOR, "$");
        testToken(lexer, Type.ID, "playername");
        testToken(lexer, Type.OPERATOR, ":");
        testToken(lexer, Type.INTEGER, "7");
        testToken(lexer, Type.OPERATOR, ":");
        testToken(lexer, Type.INTEGER, "8");
        testToken(lexer, Type.OPERATOR, ":");
        testToken(lexer, Type.STRING, "9");
        testEnd(lexer);
    }

    @Test(expected = LexerException.class)
    public void testStringException() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text;
        Lexer lexer;

        text = "\"hey ${playername}";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "hey ");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.OPERATOR, "$");
        testToken(lexer, Type.ID, "playername");
        testEnd(lexer);
    }

    @Test(expected = LexerException.class)
    public void testStringException2() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text;
        Lexer lexer;

        text = "\"hey ${playername\"";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "hey ");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.OPERATOR, "$");
        testToken(lexer, Type.ID, "playername");
        testEnd(lexer);
    }

    @Test(expected = LexerException.class)
    public void testStringException3() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String text;
        Lexer lexer;

        text = "\"hey $playername";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "hey ");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.OPERATOR, "$");
        testToken(lexer, Type.ID, "playername");
        testEnd(lexer);
    }

    @Test
    public void testOperator() throws Exception {

    }

    @Test
    public void testId() throws Exception {

    }

    @Test
    public void testEndline() throws Exception {

    }
}
