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
package io.github.wysohn.triggerreactor.core.script.lexer;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.Token.Type;
import io.github.wysohn.triggerreactor.core.script.warnings.StringInterpolationWarning;
import io.github.wysohn.triggerreactor.core.script.warnings.Warning;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lexer {
    private static final char[] OPERATORS;

    static {
        OPERATORS = new char[]{'+', '-', '*', '/', '%', '=', '!', '?', '<', '>', '&', '|', '(', ')', '{', '}', ',', '.', '[', ']', ':', '\\', '$'};
        Arrays.sort(OPERATORS);
    }

    private InputStream stream;
    private BufferedReader br;
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
        InputStreamReader isr = new InputStreamReader(stream, "UTF-8");
        br = new BufferedReader(isr);
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
        br.mark(0);

        int read = br.read();
        if (read == -1) {
            c = 0;
            eos = true;
            return false;
        } else {
            if (c == '\n' || c == ';') {
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
        br.reset();
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

                    if (c == '/') {
                        read();
                        break;
                    } else {
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
        StringBuilder builder = new StringBuilder();

        while (Character.isDigit(c)) {
            builder.append(c);
            read();
        }
        if (c != '.') {
            return new Token(Type.INTEGER, builder.toString(), row, col);
        } else {
            builder.append('.');
            read();
            if (!Character.isDigit(c))
                throw new LexerException("Invalid number [" + builder.toString() + "]", this);
        }
        while (Character.isDigit(c)) {
            builder.append(c);
            read();
        }

        return new Token(Type.DECIMAL, builder.toString(), row, col);
    }

    private Token readString() throws IOException, LexerException {
        StringBuilder builder = new StringBuilder();
        boolean warn = false;
        
        while (read() && c != '"') {
            if (c == '\\') {
                read();
                readEscapeChar(builder);
            } else if (c == '$') {
            	if (showWarnings)
            		warn = true;
            	builder.append(c);
            } else {
                builder.append(c);
            }
        }

        if (eos)
            throw new LexerException("End of stream is reached before finding '\"'", this);

        read();
        if (warn)
        	warnings.add(new StringInterpolationWarning(row, builder.toString()));

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

            if (c == '=') {
                read();
                return new Token(Type.OPERATOR_L, op + "=", row, col);
            } else {
                return new Token(Type.OPERATOR_L, op, row, col);
            }
        } else if (c == '|') {
            String op = String.valueOf(c);
            read();

            if (c == '|') {
                read();
                return new Token(Type.OPERATOR_L, op + "|", row, col);
            } else {
                throw new LexerException("Bit operator is not yet implemented", this);
            }
        } else if (c == '&') {
            String op = String.valueOf(c);
            read();

            if (c == '&') {
                read();
                return new Token(Type.OPERATOR_L, op + "&", row, col);
            } else {
                throw new LexerException("Bit operator is not yet implemented", this);
            }
        } else if (c == '=') {
            String op = String.valueOf(c);
            read();

            if (c == '=') {
                read();
                return new Token(Type.OPERATOR_L, op + "=", row, col);
            } else {
                return new Token(Type.OPERATOR, op, row, col);
            }
        } else {
            Token token = null;
            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%') {
                token = new Token(Type.OPERATOR_A, String.valueOf(c), row, col);
            } else {
                token = new Token(Type.OPERATOR, String.valueOf(c), row, col);
            }
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

            skipWhiteSpaces();
            if (!eos && c != '\n' && c != ';') {
                throw new LexerException("IMPORT expected end of line or ; at the end but found [" + c + "]", this);
            }

            return new Token(Type.IMPORT, classNameBuilder.toString(), row, col);
        } else {
            return new Token(Type.ID, builder.toString(), row, col);
        }
    }

    private Token readEndline() throws IOException {
        read();
        return new Token(Type.ENDL, (Object) null, row, col);
    }

    private static boolean isClassNameCharacter(char c) {
        return Character.isDigit(c) || Character.isAlphabetic(c) || c == '.' || c == '$';
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
        Charset charset = Charset.forName("UTF-8");
        String text = "";
        //String text = "#CMD \"w \"+name ";
        System.out.println("original: \n" + text);

        Lexer lexer = new Lexer(text, charset);
        System.out.println("result: \n");
        Token tok = null;
        while ((tok = lexer.getToken()) != null)
            System.out.println(tok.type + "] " + tok.value);
    }
}
