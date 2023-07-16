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
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestLexer {
    
    private static final Charset charset = StandardCharsets.UTF_8;

    private static void testToken(Lexer lexer, Type id, String s) throws IOException, LexerException {
        assertEquals(new Token(id, s), lexer.getToken());
    }

    private static void testEnd(Lexer lexer) throws IOException, LexerException {
        assertNull(lexer.getToken());
    }

    @Test
    public void testGetToken() throws Exception {
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
    public void testBitwiseAndBitshift() throws Exception {
        String text;
        Lexer lexer;

        text = "#MESSAGE 1 | 2 ^ 3 & ~4\n";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.ID, "#MESSAGE");
        testToken(lexer, Type.INTEGER, "1");
        testToken(lexer, Type.OPERATOR_A, "|");
        testToken(lexer, Type.INTEGER, "2");
        testToken(lexer, Type.OPERATOR_A, "^");
        testToken(lexer, Type.INTEGER, "3");
        testToken(lexer, Type.OPERATOR_A, "&");
        testToken(lexer, Type.OPERATOR_A, "~");
        testToken(lexer, Type.INTEGER, "4");
        testToken(lexer, Type.ENDL, null);
        testEnd(lexer);

        text = "#MESSAGE 1 << 2 >> 3 >>> 4\n";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.ID, "#MESSAGE");
        testToken(lexer, Type.INTEGER, "1");
        testToken(lexer, Type.OPERATOR_A, "<<");
        testToken(lexer, Type.INTEGER, "2");
        testToken(lexer, Type.OPERATOR_A, ">>");
        testToken(lexer, Type.INTEGER, "3");
        testToken(lexer, Type.OPERATOR_A, ">>>");
        testToken(lexer, Type.INTEGER, "4");
        testToken(lexer, Type.ENDL, null);
        testEnd(lexer);
    }

    @Test
    public void testAssignment() throws Exception {
        String text;
        Lexer lexer;

        text = "a = 1\n" +
                "a += 2\n" +
                "a -= 3\n" +
                "a *= 4\n" +
                "a /= 5\n" +
                "a %= 6\n";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, "=");
        testToken(lexer, Type.INTEGER, "1");
        testToken(lexer, Type.ENDL, null);
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, "+=");
        testToken(lexer, Type.INTEGER, "2");
        testToken(lexer, Type.ENDL, null);
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, "-=");
        testToken(lexer, Type.INTEGER, "3");
        testToken(lexer, Type.ENDL, null);
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, "*=");
        testToken(lexer, Type.INTEGER, "4");
        testToken(lexer, Type.ENDL, null);
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, "/=");
        testToken(lexer, Type.INTEGER, "5");
        testToken(lexer, Type.ENDL, null);
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, "%=");
        testToken(lexer, Type.INTEGER, "6");
        testToken(lexer, Type.ENDL, null);
        testEnd(lexer);

        text = "a &= 1\n" +
                "a ^= 2\n" +
                "a |= 3\n" +
                "a <<= 4\n" +
                "a >>= 5\n" +
                "a >>>= 6\n";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, "&=");
        testToken(lexer, Type.INTEGER, "1");
        testToken(lexer, Type.ENDL, null);
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, "^=");
        testToken(lexer, Type.INTEGER, "2");
        testToken(lexer, Type.ENDL, null);
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, "|=");
        testToken(lexer, Type.INTEGER, "3");
        testToken(lexer, Type.ENDL, null);
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, "<<=");
        testToken(lexer, Type.INTEGER, "4");
        testToken(lexer, Type.ENDL, null);
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, ">>=");
        testToken(lexer, Type.INTEGER, "5");
        testToken(lexer, Type.ENDL, null);
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, ">>>=");
        testToken(lexer, Type.INTEGER, "6");
        testToken(lexer, Type.ENDL, null);
        testEnd(lexer);
    }

    @Test
    public void testIncrementAndDecrement() throws Exception {
        String text;
        Lexer lexer;

        text = "a = 2\n" +
                "a = ++a * --a - a++ / a-- -(--a) -(++a) -(a++) -(a--) - -(--a) - -(++a) - -(a++) - -(a--)\n";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, "=");
        testToken(lexer, Type.INTEGER, "2");
        testToken(lexer, Type.ENDL, null);
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, "=");
        testToken(lexer, Type.OPERATOR_UNARY, "++");
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR_A, "*");
        testToken(lexer, Type.OPERATOR_UNARY, "--");
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR_UNARY, "++");
        testToken(lexer, Type.OPERATOR_A, "/");
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR_UNARY, "--");

        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.OPERATOR, "(");
        testToken(lexer, Type.OPERATOR_UNARY, "--");
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, ")");

        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.OPERATOR, "(");
        testToken(lexer, Type.OPERATOR_UNARY, "++");
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, ")");

        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.OPERATOR, "(");
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR_UNARY, "++");
        testToken(lexer, Type.OPERATOR, ")");

        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.OPERATOR, "(");
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR_UNARY, "--");
        testToken(lexer, Type.OPERATOR, ")");

        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.OPERATOR, "(");
        testToken(lexer, Type.OPERATOR_UNARY, "--");
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, ")");

        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.OPERATOR, "(");
        testToken(lexer, Type.OPERATOR_UNARY, "++");
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR, ")");

        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.OPERATOR, "(");
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR_UNARY, "++");
        testToken(lexer, Type.OPERATOR, ")");

        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.OPERATOR_A, "-");
        testToken(lexer, Type.OPERATOR, "(");
        testToken(lexer, Type.ID, "a");
        testToken(lexer, Type.OPERATOR_UNARY, "--");
        testToken(lexer, Type.OPERATOR, ")");

        testToken(lexer, Type.ENDL, null);
        testEnd(lexer);
    }

    @Test
    public void testNegation() throws Exception {
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
        String text = "#MESSAGE \"HI \\\"X\\\"! \\\\\"";

        Lexer lexer = new Lexer(text, charset);

        testToken(lexer, Type.ID, "#MESSAGE");
        testToken(lexer, Type.STRING, "HI \"X\"! \\");
        testEnd(lexer);
    }

    @Test
    public void testEscapeCharacter2() throws Exception {
        String text = "#MESSAGE \"The cost is \\$100\"";

        Lexer lexer = new Lexer(text, charset);

        assertEquals(new Token(Type.ID, "#MESSAGE"), lexer.getToken());
        assertEquals(new Token(Type.STRING, "The cost is $100"), lexer.getToken());
        assertNull(lexer.getToken());
    }

    @Test
    public void testImport() throws Exception {
        String text;
        Lexer lexer;

        text = "IMPORT some.class.to.import.Something";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.IMPORT, "some.class.to.import.Something");
        testEnd(lexer);
    }

    @Test
    public void testImportWithNum() throws Exception {
        String text;
        Lexer lexer;

        text = "IMPORT some.class.with2num.import2.So2met2hing2";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.IMPORT, "some.class.with2num.import2.So2met2hing2");
        testEnd(lexer);
    }

    @Test
    public void testImportWithInComment() throws Exception {
        String text;
        Lexer lexer;

        text = "IMPORT /* asdf */some.class.with2num.import2.So2met2hing2";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.IMPORT, "some.class.with2num.import2.So2met2hing2");
        testEnd(lexer);
    }

    @Test
    public void testComment() throws Exception {
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

        text = "4/**\n"
            + " * heya" +
            " */+5";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.INTEGER, "4");
        testToken(lexer, Type.OPERATOR_A, "+");
        testToken(lexer, Type.INTEGER, "5");
        testEnd(lexer);
    }

    @Test
    public void testNumber_NumericSeparators() throws Exception {
        String text;
        Lexer lexer;

        {
            text = "1_000_000_000";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "1000000000");
            testEnd(lexer);
        }
        {
            text = "0.000_000_1";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.DECIMAL, "0.0000001");
            testEnd(lexer);
        }
    }

    @Test
    public void testNumber_Base() throws Exception {
        String text;
        Lexer lexer;

        {
            text = "0010"; // Not a base prefix
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "10");
            testEnd(lexer);
        }
        {
            text = "0b0000_0010_0000";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "32");
            testEnd(lexer);
        }
        {
            text = "0B0000_0010_0000";  // Check for Uppercase base
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "32");
            testEnd(lexer);
        }
        {
            text = "0o100";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "64");
            testEnd(lexer);
        }
        {
            text = "0O100";  // Check for Uppercase base
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "64");
            testEnd(lexer);
        }
        {
            text = "0xC0FFEE";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "12648430");
            testEnd(lexer);
        }
        {
            text = "0XC0FFEE";  // Check for Uppercase base
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "12648430");
            testEnd(lexer);
        }
        {
            text = "0xabcdef";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "11259375");
            testEnd(lexer);
        }
        {
            text = "0xABCDEF";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "11259375");
            testEnd(lexer);
        }
    }

    @Test
    public void testNumber_ENotation() throws Exception {
        String text;
        Lexer lexer;

        {  // Test for E notation in integer literals
            text = "3e2";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "300");
            testEnd(lexer);
        }
        {  // Test for E notation in decimal literals, which is lower-cased (Case 1)
            text = "1.23e2";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "123");
            testEnd(lexer);
        }
        {  // Test for E notation in decimal literals, which is lower-cased (Case 2)
            text = "1.23e4";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "12300");
            testEnd(lexer);
        }
        {  // Test for E notation in decimal literals, which is upper-cased
            text = "1.23E2";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "123");
            testEnd(lexer);
        }
        {  // Test for E notation in decimal literals with sign(+) operator (optional)
            text = "1.23e+2";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "123");
            testEnd(lexer);
        }
        {  // Test for E notation in decimal literals with minus(-) operator
            text = "1.23e-2";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.DECIMAL, "0.0123");
            testEnd(lexer);
        }
        {  // Test for complex literals with numeric separators(_) and also rest tests too
            text = "1.77_244_325e8";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "177244325");
            testEnd(lexer);
        }
        {
            text = "1.77_244_325e4";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.DECIMAL, "17724.4325");
            testEnd(lexer);
        }
        {
            text = "177.244_325e-2";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.DECIMAL, "1.77244325");
            testEnd(lexer);
        }
        {
            text = "177e-8";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.DECIMAL, "0.00000177");
            testEnd(lexer);
        }
    }

    @Test(expected = LexerException.class)
    public void testNumber_InvalidTokenException() throws IOException, LexerException {
        String text;
        Lexer lexer;
        {
            text = "1_000_000_000.";
            lexer = new Lexer(text, charset);

            testToken(lexer, Type.INTEGER, "");
            testEnd(lexer);
        }
        {
            text = "0._";
            lexer = new Lexer(text, charset);

            testToken(lexer, Type.DECIMAL, "");
            testEnd(lexer);
        }
    }

    @Test(expected = LexerException.class)
    public void testNumber_InvalidBaseException() throws IOException, LexerException {
        String text;
        Lexer lexer;
        {
            text = "0b0000_0010_0000.";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "");
            testEnd(lexer);
        }
        {
            text = "0o100.";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "");
            testEnd(lexer);
        }
        {
            text = "0xC0FFEE.";
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "");
            testEnd(lexer);
        }
        {
            text = "0xABCDEG"; // "G" is not a part of hexadecimal
            lexer = new Lexer(text, charset);
            testToken(lexer, Type.INTEGER, "");
            testEnd(lexer);
        }
    }

    @Test
    public void testString() throws Exception {
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

    @Test
    public void testMultilineString() throws IOException, LexerException {
        Lexer lexer;
        String text;

        text = "`\nmerp\nderp\n`";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "\nmerp\nderp\n");
        testEnd(lexer);

        text = "`\\\"$hi`";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "\\\"$hi");
        testEnd(lexer);

        text = "`\r`";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "");
        testEnd(lexer);

        text = "``";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "");
        testEnd(lexer);
    }

    @Test(expected = LexerException.class)
    public void testMultilineStringException() throws IOException, LexerException {
        Lexer lexer;
        String text;

        text = "`\nmerp";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "\nmerp");
        testEnd(lexer);
    }

    @Test(expected = LexerException.class)
    public void testStringException() throws Exception {
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
    public void testPlaceholderSubFolder() throws Exception {
        String text;
        Lexer lexer;

        text = "$Refactoring@GAMEMODE:\"TestPlayer\":1";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.OPERATOR, "$");
        testToken(lexer, Type.ID, "Refactoring");
        testToken(lexer, Type.OPERATOR, "@");
        testToken(lexer, Type.ID, "GAMEMODE");
        testToken(lexer, Type.OPERATOR, ":");
        testToken(lexer, Type.STRING, "TestPlayer");
        testToken(lexer, Type.OPERATOR, ":");
        testToken(lexer, Type.INTEGER, "1");
        testEnd(lexer);
    }

    @Test
    public void testPlaceholderEscape() throws Exception {
        String text;
        Lexer lexer;

        text = "\"hey \\$playername\"";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "hey $playername");
        testEnd(lexer);
    }

    @Test
    public void testPlaceholderEscape2() throws Exception {
        String text;
        Lexer lexer;

        text = "\"hey \\${playername}\"";
        lexer = new Lexer(text, charset);
        testToken(lexer, Type.STRING, "hey ${playername}");
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

    @Test
    public void testImportWithUnderscore() throws Exception{
        String text;
        Lexer lexer;

        text = "IMPORT net.md_5.bungee.api.chat.ComponentBuilder";
        lexer = new Lexer(text, charset);
        assertEquals(new Token(Type.IMPORT, "net.md_5.bungee.api.chat.ComponentBuilder"), lexer.getToken());
    }
}
