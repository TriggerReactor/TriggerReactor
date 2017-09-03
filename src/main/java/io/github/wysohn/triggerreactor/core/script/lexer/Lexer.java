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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

import io.github.wysohn.triggerreactor.core.script.Token;
import io.github.wysohn.triggerreactor.core.script.Token.Type;

public class Lexer {
    private static final char[] OPERATORS;
    static {
        OPERATORS = new char[] { '+', '-', '*', '/', '%', '=', '!', '<', '>', '&', '|', '(', ')' , '{', '}', ',', '.', '[', ']', ':'};
        Arrays.sort(OPERATORS);
    }

    private InputStream stream;
    private BufferedReader br;

    private boolean eos = false;
    private char c = 0;

    private int row = 1;
    private int col = 1;

    public Lexer(InputStream stream) throws IOException {
        this.stream = stream;
        initInputStream();
    }

    public Lexer(String str, Charset charset) throws IOException{
        this.stream = new ByteArrayInputStream(str.getBytes(charset));
        initInputStream();
    }

    public Lexer(String str, String charset) throws IOException{
        this.stream = new ByteArrayInputStream(str.getBytes(charset));
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

    /**
     *
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
            if(c == '\n'){
                row++; col = 0;
            }else{
                col++;
            }
            c = (char) read;
            return true;
        }
    }

    private void unread() throws IOException{
        col--;
        br.reset();
    }

    /**
     *
     * @return null if end of stream is reached.
     * @throws IOException
     * @throws LexerException
     */
    public Token getToken() throws IOException, LexerException{
        skipWhiteSpaces();
        skipComment();

        if(Character.isDigit(c)){
            return readNumber();
        }

        if(c == '"'){
            return readString();
        }

        if (isOperator(c)) {
            return readOperator();
        }

        if(isIdCharacter(c)){
            return readId();
        }

        if(c == '\n' || c == ';'){
            return readEndline();
        }

        return null;
    }

    private void skipWhiteSpaces() throws IOException{
        //skip white spaces
        while(c == ' ' || c == '\t' || c == '\r'){
            read();
        }
    }

    private void skipComment() throws LexerException, IOException{
        if(c == '/'){
            read();

            if(c == '/'){
                //skip until next line or end of line
                while(c != '\n' && read());
            }else if(c == '*'){
                while(read()){
                    while(c != '*' && read());
                    read();

                    if(c == '/'){
                        read();
                        break;
                    }else{
                        throw new LexerException("Expected '/' but end of stream is reached.", this);
                    }
                }
            }else{
                //was not comment
                unread();
                c = '/';
            }
        }
    }

    private Token readNumber() throws IOException, LexerException{
        StringBuilder builder = new StringBuilder();

        while(Character.isDigit(c)){
            builder.append(c);
            read();
        }
        if(c != '.'){
            return new Token(Type.INTEGER, builder.toString());
        }else{
            builder.append('.');
            read();
            if(!Character.isDigit(c))
                throw new LexerException("Invalid number ["+builder.toString()+"]!", this);
        }
        while(Character.isDigit(c)){
            builder.append(c);
            read();
        }

        return new Token(Type.DECIMAL, builder.toString());
    }

    private Token readString() throws IOException, LexerException{
        StringBuilder builder = new StringBuilder();

        while(read() && c != '"'){
            builder.append(c);
        }
        read();

        return new Token(Type.STRING, builder.toString());
    }

    private Token readOperator() throws IOException, LexerException{
        if(c == '<' || c == '>' || c == '!'){
            String op = String.valueOf(c);
            read();

            if(c == '='){
                read();
                return new Token(Type.OPERATOR_L, op+"=");
            }else{
                return new Token(Type.OPERATOR_L, op);
            }
        } else if (c == '|') {
            String op = String.valueOf(c);
            read();

            if(c == '|'){
                read();
                return new Token(Type.OPERATOR_L, op+"|");
            }else{
                throw new LexerException("Bit operator is not yet implemented.", this);
            }
        } else if (c == '&') {
            String op = String.valueOf(c);
            read();

            if(c == '&'){
                read();
                return new Token(Type.OPERATOR_L, op+"&");
            }else{
                throw new LexerException("Bit operator is not yet implemented.", this);
            }
        } else if (c == '=') {
            String op = String.valueOf(c);
            read();

            if(c == '='){
                read();
                return new Token(Type.OPERATOR_L, op+"=");
            }else{
                return new Token(Type.OPERATOR, op);
            }
        }else{
            Token token = null;
            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%') {
                token = new Token(Type.OPERATOR_A, String.valueOf(c));
            } else {
                token = new Token(Type.OPERATOR, String.valueOf(c));
            }
            read();
            return token;
        }
    }

    private Token readId() throws IOException, LexerException{
        StringBuilder builder = new StringBuilder();

        //first character cannot be digit, etc
        if(isIdCharacter(c)){
            builder.append(c);
            read();
        }

        while(isIdCharacter(c) || Character.isDigit(c)){
            builder.append(c);
            read();
        }

        return new Token(Type.ID, builder.toString());
    }

    private Token readEndline() throws IOException {
        read();
        return new Token(Type.ENDL, null);
    }

    private static boolean isIdCharacter(char c){
        return Character.isAlphabetic(c) || c == '_' || c == '#';
    }

    private static boolean isOperator(char c){
        return Arrays.binarySearch(OPERATORS, c) >= 0;
    }

    public static void main(String[] ar) throws IOException, LexerException{
        Charset charset = Charset.forName("UTF-8");
        String text = ""
                + "X = 5\n"
                + "str = \"abc\"\n"
                + "FOR i = 0 : 10\n"
                + "    str = str + X\n"
                + "    IF player.in.health > 2 && player.in.health[3] > 0\n"
                + "        #MESSAGE 3*4\n"
                + "    ELSE\n"
                + "        #MESSAGE str\n"
                + "    ENDIF\n"
                + "    #MESSAGE player.in.hasPermission(x, 2+3, 5 > 4)\n"
                + "    X = X - 1\n"
                + "    IF X < 0\n"
                + "        #STOP\n"
                + "    ENDIF\n"
                + "    #WAIT 1\n"
                + "ENDFOR";
        //String text = "#CMD \"w \"+name ";
        System.out.println("original: \n"+text);

        Lexer lexer = new Lexer(text, charset);
        System.out.println("result: \n");
        Token tok = null;
        while((tok = lexer.getToken()) != null)
            System.out.println(tok.type+"] "+tok.value);
    }
}
