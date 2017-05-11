package io.github.wysohn.triggerreactor.core;

public class Token {
    public final Type type;
    public final Object value;

    public Token(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Token(Type type) {
        this(type, null);
    }

    public Object getValue() {
        return value;
    }

    public boolean isInt(){
        return value instanceof Integer;
    }

    public boolean isDouble(){
        return value instanceof Double;
    }

    public boolean isBoolean(){
        return value instanceof Boolean;
    }

    public int toInt(){
        return (int) value;
    }

    public double toDouble(){
        return (double) value;
    }

    public boolean toBoolean(){
        return (boolean) value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Token other = (Token) obj;
        if (type != other.type)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public enum Type {
        ROOT, ENDL,

        //Literal
        STRING(true), INTEGER(true), DECIMAL(true), BOOLEAN(true),

        OBJECT,


        /**Parenthesis, Blocks**/OPERATOR, /**Arithmetic**/OPERATOR_A, /**Logical**/OPERATOR_L,

        GID, ID,

        BODY, COMMAND,
        ;

        private final boolean literal;
        private Type(boolean literal){
            this.literal = literal;
        }
        private Type() {
            this.literal = false;
        }
        public boolean isLiteral() {
            return literal;
        }
    }
}
