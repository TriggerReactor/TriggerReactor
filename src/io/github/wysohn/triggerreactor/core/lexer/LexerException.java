package io.github.wysohn.triggerreactor.core.lexer;

public class LexerException extends Exception {
    public LexerException(Lexer lexer){
        super("An Error near row:"+lexer.getRow()+" col:"+lexer.getCol());
    }

    public LexerException(String message, Lexer lexer){
        super(message+" near row:"+lexer.getRow()+" col:"+lexer.getCol());
    }
}
