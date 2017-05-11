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
package io.github.wysohn.triggerreactor.core.lexer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import io.github.wysohn.triggerreactor.core.Token;
import io.github.wysohn.triggerreactor.core.Token.Type;

public class Lexer {
    private static final char[] OPERATORS;
    static {
        OPERATORS = new char[] { '+', '-', '*', '/', '%', '=', '!', '<', '>', '&', '|', '(', ')' };
        Arrays.sort(OPERATORS);
    }

    private InputStream stream;
    private BufferedInputStream inputStream;

    private boolean eos = false;
    private char c = 0;

    private int row = 0;
    private int col = 0;

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
        this.inputStream = new BufferedInputStream(stream);
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
        inputStream.mark(0);
        int read = inputStream.read();
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
        inputStream.reset();
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

        //check unary minus
        if(c == '-'){
            if(read() && Character.isDigit(c)){
                return readNegativeNumber();
            }else if(!eos){//just a minus sign so unread it
                unread();
                c = '-';
            }else{//do nothing as eos is reached

            }
        }

        if(c == '"'){
            return readString();
        }

        if (isOperator(c)) {
            return readOperator();
        }

        if(c == '{'){
            return readGlobalId();
        }

        if(isIdCharacter(c)){
            return readId();
        }

        if(c == '\n'){
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

    private Token readNegativeNumber() throws IOException, LexerException{
        StringBuilder builder = new StringBuilder();
        builder.append('-');

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

        if(c != '.'){
            return new Token(Type.ID, builder.toString());
        }else{
            builder.append('.');
            read();
            if(!isIdCharacter(c))
                throw new LexerException("Invalid id", this);
        }

        while(isIdCharacter(c) || Character.isDigit(c)){
            builder.append(c);
            read();
        }

        return new Token(Type.ID, builder.toString());
    }

    private Token readGlobalId() throws IOException, LexerException{
        if(c != '{'){
            throw new LexerException(c+" is not a valid start of global Id!", this);
        }
        read();

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

        while(c == '.'){
            read();
            builder.append('.');
            while(isIdCharacter(c) || Character.isDigit(c)){
                builder.append(c);
                read();
            }
        }

        if(c != '}'){
            throw new LexerException("} expected!", this);
        }
        read();

        return new Token(Type.GID, builder.toString());
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
/*        String text = ""
                + "X = 5\n"
                + "WHILE 1 > 0\n"
                + "    IF {player.test.health} > 2 || {player.health} > 0\n"
                + "        #MESSAGE 3*4\n"
                + "    ELSE\n"
                + "        #MESSAGE 777\n"
                + "    ENDIF\n"
                + "    X = X - 1\n"
                + "    IF X < 0\n"
                + "        #STOP\n"
                + "    ENDIF\n"
                + "    #WAIT 1\n"
                + "ENDWHILE";*/
        String text = "#CMD \"w \"+name ";
        System.out.println("original: \n"+text);

        Lexer lexer = new Lexer(text, charset);
        System.out.println("result: \n");
        Token tok = null;
        while((tok = lexer.getToken()) != null)
            System.out.println(tok.type+"] "+tok.value);
    }
}
