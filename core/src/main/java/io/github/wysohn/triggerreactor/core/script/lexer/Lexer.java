/**
 * Copyright (c) 2023 TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.wysohn.triggerreactor.core.script.lexer;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.Token.Type;
import io.github.wysohn.triggerreactor.core.script.warning.StringInterpolationWarning;
import io.github.wysohn.triggerreactor.core.script.warning.Warning;
import io.github.wysohn.triggerreactor.tools.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.*;

public class Lexer {
    private static final char[] OPERATORS;

    static {
        OPERATORS = new char[]{'+', '-', '*', '/', '%', '=', '!', '?', '<', '>', '&', '|', '^', '~', '(', ')', '{', '}', ',', '.', '[', ']', ':', '\\', '$', '@'};
        Arrays.sort(OPERATORS);
    }

    private InputStream stream;
    private PushbackReader reader;
    private final String[] scriptLines;

    private boolean eos = false;
    private char c = 0;
    private boolean showWarnings = false;

    private List<Warning> warnings = new ArrayList<Warning>();

    private int row = 1;
    private int col = 1;

    public Lexer(String str, Charset charset) throws IOException {
        this.stream = new ByteArrayInputStream(str.getBytes(charset));
        this.scriptLines = str.split("\n");
        initInputStream();
    }

    public Lexer(String str, String charset) throws IOException {
        this.stream = new ByteArrayInputStream(str.getBytes(charset));
        this.scriptLines = str.split("\n");
        initInputStream();
    }

    private void initInputStream() throws IOException {
        InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
        reader = new PushbackReader(new BufferedReader(isr), 256);
        read();//position to first element
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public List<Warning> getWarnings() {
        return warnings;
    }

    public boolean getShowWarnings() {
        return showWarnings;
    }

    public String[] getScriptLines() {
        return scriptLines;
    }

    /**
     * @return false if end of stream is reached.
     * @throws IOException
     */
    private boolean read() throws IOException {
        int read = reader.read();
        if (read == -1) {
            c = 0;
            eos = true;
            return false;
        } else {
            if (c == '\n') {
                row++;
                col = 0;
            } else {
                col++;
            }
            c = (char) read;
            return true;
        }
    }

    private void unread() throws IOException {
        col--;
        reader.unread(c);
    }

    /**
     * @return null if end of stream is reached.
     * @throws IOException
     * @throws LexerException
     */
    public Token getToken() throws IOException, LexerException {
        skipWhiteSpaces();
        skipComment();

        if (Character.isDigit(c)) {
            return readNumber();
        }

        if (c == '"') {
            return readString();
        }

        if (c == '`') {
            return readMultilineString();
        }

        if (isOperator(c)) {
            return readOperator();
        }

        if (isIdCharacter(c)) {
            return readId();
        }

        if (c == '\n' || c == ';') {
            return readEndline();
        }

        //still something is in the stream, but it's not recognizable
        if (!eos) {
            throw new LexerException("Found an unrecognizable character", this);
        }

        return null;
    }

    private void skipWhiteSpaces() throws IOException {
        //skip white spaces
        while (c == ' ' || c == '\t' || c == '\r') {
            read();
        }
    }

    private void skipComment() throws LexerException, IOException {
        if (c == '/') {
            read();

            if (c == '/') {
                //skip until next line or end of line
                while (c != '\n' && read()) ;
            } else if (c == '*') {
                while (read()) {
                    while (c != '*' && read()) ;
                    read();

                    // Eat stream while cursor meet */, or throw exception if end of stream is reached.
                    if (c == '/') {
                        read();
                        break;
                    } else if (eos) {
                        throw new LexerException("Expected '/' but end of stream is reached", this);
                    }
                }
            } else {
                //was not comment
                unread();
                c = '/';
            }
        }
    }

    private Token readNumber() throws IOException, LexerException {
        final StringBuilder builder = new StringBuilder();

        final Token.Base base = getNumericLiteralBase();
        if (base == Token.Base.Hexadecimal) {
            eatHexadecimalDigits(builder);
        } else {
            eatDecimalDigits(builder);
        }

        if (c != '.') {
            eatNumericLiteralPostfix(builder, false);
        } else {
            builder.append('.');
            read();
            if (base != Token.Base.Decimal) {
                throw new LexerException("Float literals are unsupported base.", this);
            } else if (c == '_') {
                throw new LexerException("Numeric separators are not allowed at the start of floating points.", this);
            } else if (!Character.isDigit(c)) {
                throw new LexerException("Invalid number [" + builder.toString() + "]", this);
            }

            eatDecimalDigits(builder);
            eatNumericLiteralPostfix(builder, true);
        }

        if (builder.indexOf(".") != -1) {  // Treat as decimal now
            return new Token(Type.DECIMAL, builder.toString(), row, col);
        } else {
            return new Token(Type.INTEGER, String.valueOf(tryParseInt(builder.toString(), base.radix)), row, col);
        }
    }

    private Token readString() throws IOException, LexerException {
        StringBuilder builder = new StringBuilder();
        boolean warn = false;

        while (read() && c != '"') {
            if (c == '\\') {
                read();
                readEscapeChar(builder);
            } else {
                if (c == '$') {
                    StringBuilder placeholder_builder = new StringBuilder();
                    placeholder_builder.append("+$");

                    read();
                    if (c == '{') {
                        while (read() && c != '}') {
                            if (c == '\\') {
                                read();
                                readEscapeChar(placeholder_builder);
                            } else {
                                placeholder_builder.append(c);
                            }
                        }

                        if (eos)
                            throw new LexerException("End of stream is reached before finding } for placeholder $" +
                                    placeholder_builder.substring(2), this);
                        read();
                        if (eos)
                            throw new LexerException("End of stream is reached before finding '\"'", this);

                        if (c != '"') {
                            placeholder_builder.append("+\""); // prepare for concatenation
                            unread(); // push back the last character
                        }
                    } else {
                        while (c != '"' && c != ' ') {
                            if (c == '\\') {
                                read();
                                readEscapeChar(placeholder_builder);
                            } else {
                                placeholder_builder.append(c);
                            }

                            if (!read())
                                break;
                        }

                        // white space is still part of string so push it back
                        if (c == ' ')
                            unread();

                        if (eos)
                            throw new LexerException("End of stream is reached before finding end of placeholder $" +
                                    placeholder_builder.substring(2), this);

                        if (c != '"')
                            placeholder_builder.append("+\""); // prepare for concatenation
                        else
                            read(); // consume dangling " sign
                    }

                    //push placeholder back into stream
                    reader.unread(placeholder_builder.toString().toCharArray());
                    read();

                    return new Token(Type.STRING, builder.toString(), row, col);
                } else {
                    builder.append(c);
                }
            }
        }

        if (eos)
            throw new LexerException("End of stream is reached before finding '\"'", this);
        read();
        if (warn)
            warnings.add(new StringInterpolationWarning(row, scriptLines[row - 1]));

        return new Token(Type.STRING, builder.toString(), row, col);
    }

    private Token readMultilineString() throws IOException, LexerException {
        StringBuilder builder = new StringBuilder();

        while (read() && c != '`') {
            if (c != '\r') //skip carrige returns
                builder.append(c);
        }
        if (eos)
            throw new LexerException("End of stream reached before finding '`'", this);

        read();
        return new Token(Type.STRING, builder.toString(), row, col);
    }

    private void readEscapeChar(StringBuilder builder) throws LexerException {
        if (c == '\\' || c == '"') {
            builder.append(c);
        } else if (c == 'n') {
            builder.append('\n');
        } else if (c == 'r') {
            builder.append('\r');
        } else if (c == '$') {
            builder.append('$');
        } else {
            throw new LexerException("Expected an escaping character after \\ but found " + c + " instead", this);
        }
    }

    private Token readOperator() throws IOException, LexerException {
        if (c == '<' || c == '>' || c == '!') {
            String op = String.valueOf(c);
            read();

            if (c == '=') { // <=, >=, !=
                read();
                return new Token(Type.OPERATOR_L, op + "=", row, col);
            } else if (!op.equals("!") && op.equals(String.valueOf(c))) { // <<, >>, >>>, <<=, >>=, >>>=
                read();
                if (">".equals(op) && c == '>') { // >>>, >>>=
                    read();
                    if (c == '=') { // >>>=
                        read();
                        return new Token(Type.OPERATOR, ">>>=", row, col);
                    } else { // >>>
                        return new Token(Type.OPERATOR_A, ">>>", row, col);
                    }
                } else if (c == '=') { // <<=, >>=
                    read();
                    return new Token(Type.OPERATOR, op + op + "=", row, col);
                } else { // <<, >>
                    return new Token(Type.OPERATOR_A, op + op, row, col);
                }
            } else {
                return new Token(Type.OPERATOR_L, op, row, col);
            }
        } else if (c == '|') {
            String op = String.valueOf(c);
            read();

            if (c == '|') {
                read();
                return new Token(Type.OPERATOR_L, op + "|", row, col);
            } else if (c == '=') {
                read();
                return new Token(Type.OPERATOR, op + "=", row, col);
            } else {
                return new Token(Type.OPERATOR_A, op, row, col);
            }
        } else if (c == '&') {
            String op = String.valueOf(c);
            read();

            if (c == '&') {
                read();
                return new Token(Type.OPERATOR_L, op + "&", row, col);
            } else if (c == '=') {
                read();
                return new Token(Type.OPERATOR, op + "=", row, col);
            } else {
                return new Token(Type.OPERATOR_A, op, row, col);
            }
        } else if (c == '=') {
            String op = String.valueOf(c);
            read();

            if (c == '=') {
                read();
                return new Token(Type.OPERATOR_L, op + "=", row, col);
            } else if(c == '>'){
                read();
                return new Token(Type.OPERATOR, op + ">", row, col);
            } else {
                return new Token(Type.OPERATOR, op, row, col);
            }
        } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%'
                    || c == '^' || c == '~') {
            String op = String.valueOf(c);
            read();

            if (!"~".equals(op) && c == '=') {
                read();
                return new Token(Type.OPERATOR, op + "=", row, col);
            } else if (("+".equals(op) || "-".equals(op)) && op.equals(String.valueOf(c))) {
                read();
                return new Token(Type.OPERATOR_UNARY, op+op, row, col);
            } else if (("*".equals(op)) && c == '*') {
                read();
                return new Token(Type.OPERATOR_A, op+op, row, col);
            } else {
                return new Token(Type.OPERATOR_A, op, row, col);
            }
        } else {
            Token token = new Token(Type.OPERATOR, String.valueOf(c), row, col);
            read();
            return token;
        }
    }

    private Token readId() throws IOException, LexerException {
        StringBuilder builder = new StringBuilder();

        //first character cannot be digit, etc
        if (isIdCharacter(c)) {
            builder.append(c);
            read();
        } else {
            throw new LexerException("Cannot use " + c + " as a first character", this);
        }

        while (isIdCharacter(c) || Character.isDigit(c)) {
            builder.append(c);
            read();
        }

        String id = builder.toString();
        if (id.equalsIgnoreCase("IMPORT")) {
            skipWhiteSpaces();
            skipComment();

            if (c == '.') {
                throw new LexerException("IMPORT found a dangling .(dot)", this);
            }

            if (!isClassNameCharacter(c)) {
                throw new LexerException("IMPORT found an unexpected character [" + c + "]", this);
            }

            StringBuilder classNameBuilder = new StringBuilder();
            while (isClassNameCharacter(c)) {
                classNameBuilder.append(c);
                read();
            }

            if (classNameBuilder.charAt(classNameBuilder.length() - 1) == '.')
                classNameBuilder.deleteCharAt(classNameBuilder.length() - 1);

//            skipWhiteSpaces();
//            if (!eos && c != '\n' && c != ';') {
//                throw new LexerException("IMPORT expected end of line or ; at the end but found [" + c + "]", this);
//            }

            return new Token(Type.IMPORT, classNameBuilder.toString(), row, col);
        } else {
            return new Token(Type.ID, builder.toString(), row, col);
        }
    }

    private Token readEndline() throws IOException {
        read();
        return new Token(Type.ENDL, null, row, col);
    }

    private Token.Base getNumericLiteralBase() throws IOException {
        if (c == '0') {
            read();

            if (c == 'b' || c == 'B') {
                read();
                return Token.Base.Binary;
            } else if (c == 'o' || c == 'O') {
                read();
                return Token.Base.Octal;
            } else if (c == 'x' || c == 'X') {
                read();
                return Token.Base.Hexadecimal;
            } else {
                // Not a base prefix, push bask the last character
                unread();
                c = '0';
            }
        }

        return Token.Base.Decimal;
    }
    private static final Predicate<Character> PREDICATE_DECIMAL_DIGIT = c -> Character.isDigit(c) || c == '_';
    private static final Predicate<Character> PREDICATE_HEXADECIMAL_DIGIT = c -> PREDICATE_DECIMAL_DIGIT.test(c) || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F');
    private static final BiConsumer<Appendable, Character> DIGIT_CONSUMER = (appendable, c) -> {
        if (c != '_') {
            try {
                appendable.append(c);
            } catch (final IOException ignored) {
            }
        }
    };
    private static Predicate<Character> isDecimalDigit() {
        return PREDICATE_DECIMAL_DIGIT;
    }
    private static Predicate<Character> isHexadecimalDigit() {
        return PREDICATE_HEXADECIMAL_DIGIT;
    }

    private StringBuilder eatDecimalDigits() throws IOException {
        return eatDecimalDigits(new StringBuilder());
    }

    private StringBuilder eatDecimalDigits(final StringBuilder builder) throws IOException {
        return (StringBuilder) eatWhile(isDecimalDigit(), DIGIT_CONSUMER, () -> builder);
    }

    private StringBuilder eatHexadecimalDigits() throws IOException {
        return eatHexadecimalDigits(new StringBuilder());
    }

    private StringBuilder eatHexadecimalDigits(final StringBuilder builder) throws IOException {
        return (StringBuilder) eatWhile(isHexadecimalDigit(), DIGIT_CONSUMER, () -> builder);
    }

    private void eatNumericLiteralPostfix(final StringBuilder builder) throws IOException, LexerException {
        eatNumericLiteralPostfix(builder, builder.indexOf("."));
    }

    private void eatNumericLiteralPostfix(final StringBuilder builder, final boolean decSeen) throws IOException, LexerException {
        if (decSeen) {
            eatNumericLiteralPostfix(builder);
        } else {
            eatNumericLiteralPostfix(builder, -1);
        }
    }

    private void eatNumericLiteralPostfix(final StringBuilder builder, final int decIndex) throws IOException, LexerException {
        eatENotation(builder, decIndex);
    }

    private void eatENotation(final StringBuilder builder, final int decIndex) throws IOException, LexerException {
        // Look for 'e' or 'E' notation
        if (c == 'e' || c == 'E') {
            read();

            final boolean negative = c == '-';
            if (c == '+' || negative /* Calculated value (same as: c == '-') */)
                read();  // Advance sign

            final CharSequence maybeExponent = eatDecimalDigits();
            if (maybeExponent.length() == 0) {
                throw new LexerException("Exponent must be not empty.", this);
            }

            final int exponent = tryParseInt(maybeExponent.toString());
            if (exponent == 0) {
                throw new LexerException("Exponent must be numerical value.", this);
            }

            if (decIndex == -1) {  // Int
                if (!negative) builder.append(StringUtils.repeat("0", exponent));
                else {
                    final int length = exponent - builder.length();
                    if (length == 0) {
                        builder.insert(0, '.');
                        builder.insert(0, '0');
                    } else if (length > 0) {
                        for (int i = length; i >= 0; i--) {
                            builder.insert(0, '0');

                            if (i == 1) {
                                builder.insert(0, '.');
                            }
                        }
                    } else {
                        builder.insert(-length, '.');
                    }
                }
            } else {  // Float
                builder.deleteCharAt(decIndex);

                final int decimalLength = builder.length() - decIndex;
                if (!negative) {
                    // Skip if exponent == decimalLength
                    if (exponent < decimalLength) {
                        builder.insert(decIndex + exponent, '.');
                    } else if (exponent > decimalLength) {
                        builder.append(StringUtils.repeat("0", exponent - decimalLength));
                    }
                } else if (decIndex - exponent > 0) {
                    builder.insert(decIndex - exponent, '.');
                } else {
                    for (int i = exponent; i > 0; i--) {
                        builder.insert(0, '0');

                        if (i == 2) builder.insert(0, '.');
                    }
                }
            }
        }
    }

    /**
     * Eats symbols while predicate returns {@code true} or until the end of stream is reached.
     *
     * @param predicate the predicate to evaluate against symbols
     * @throws IOException if an I/O error occurs
     */
    private void eatWhile(final Predicate<Character> predicate) throws IOException {
        eatWhile(predicate, null, null);
    }

    /**
     * Eats symbols while predicate returns {@code true} or until the end of stream is reached,
     * and returns a result container that fold eaten characters.
     *
     * @param predicate the predicate to evaluate against symbols
     * @param fn a function that fold eaten symbols into a result container
     * @param sup a function that creates a new mutable result container to fold eaten symbols
     * @return consumed characters that performed the fn
     * @param <R> the type
     * @throws IOException if an I/O error occurs
     */
    private <R> R eatWhile(final Predicate<Character> predicate, final BiConsumer<R, Character> fn, final Supplier<R> sup) throws IOException {
        final R identity = sup != null ? sup.get() : null;
        while (predicate.test(c) && !eos) {
            if (fn != null) fn.accept(identity, c);
            read();
        }

        return identity;
    }

    private static int tryParseInt(final String s) {
        return tryParseInt(s, 10);
    }

    private static int tryParseInt(final String s, final int radix) {
        int i = 0, len = s.length(), result = 0;
        final int limit = -Integer.MAX_VALUE;
        final int multmin = limit / radix;

        if (len > 0) {
            while (i < len) {
                final int digit = Character.digit(s.charAt(i++), radix);
                if (digit < 0 || result < multmin) {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }
                result *= radix;
                if (result < limit + digit) {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }
                result -= digit;
            }
        }

        return -result;
    }

    private static boolean isClassNameCharacter(char c) {
        return Character.isDigit(c) || Character.isAlphabetic(c) || c == '.' || c == '$' || c == '_';
    }

    private static boolean isIdCharacter(char c) {
        return Character.isAlphabetic(c) || c == '_' || c == '#';
    }

    private static boolean isOperator(char c) {
        return Arrays.binarySearch(OPERATORS, c) >= 0;
    }

    public void setWarnings(boolean w) {
        showWarnings = w;
    }

    public static void main(String[] ar) throws IOException, LexerException {
        Charset charset = StandardCharsets.UTF_8;
        String text = "1.23e2";
        //String text = "#CMD \"w \"+name ";
        System.out.println("original: \n" + text);

        Lexer lexer = new Lexer(text, charset);
        System.out.println("result: \n");
        Token tok = null;
        while ((tok = lexer.getToken()) != null)
            System.out.println("[" + tok.type + "] " + tok.value);
    }
}
