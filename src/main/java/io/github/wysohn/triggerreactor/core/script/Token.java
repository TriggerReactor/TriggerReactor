/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
package io.github.wysohn.triggerreactor.core.script;

public class Token {
    public final Type type;
    public final Object value;
    public final int row;
    public final int col;
    
    public Token(Type type, Object value, int row, int col) {
        this.type = type;
        this.value = value;
        this.row = row;
        this.col = col;
    }


    public Token(Type type, Object value, Token tokenOrigin) {
        this.type = type;
        this.value = value;
        this.row = tokenOrigin.row;
        this.col = tokenOrigin.col;
    }

/*    public Token(Type type, Object value, Lexer lexer) {
        this(type, value, lexer.getRow(), lexer.getCol());
    }
*/
    public Token(Type type, Object value) {
        this(type, value, -1, -1);
    }

    public Object getValue() {
        return value;
    }

    public boolean isInteger(){
        return value instanceof Integer
                || value instanceof Long
                || value instanceof Short
                || value instanceof Byte;
    }

    public boolean isDecimal(){
        return value instanceof Double
                || value instanceof Float;
    }

    public boolean isBoolean(){
        return value instanceof Boolean;
    }

    public boolean isString() {
        return value instanceof String;
    }

    public boolean isArray() {
        return value != null && value.getClass().isArray();
    }

    public boolean isIterable() {
        return value != null && (value.getClass().isArray() || value instanceof Iterable);
    }

    public boolean isObject(){
        return !isInteger() && !isDecimal() && !isBoolean() && !isArray();
    }

    public int toInteger(){
        return ((Number) value).intValue();
    }

    public double toDecimal(){
        return ((Number) value).doubleValue();
    }

    public boolean isNumeric() {
        return isInteger() || isDecimal();
    }

    public boolean toBoolean(){
        return (boolean) value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "[type: "+type.name()+", value: '"+value+"'] at row["+row+"], col["+col+"]";
    }
    
    public String toStringRowColOnly() {
    	return "at row["+row+"], col["+col+"]";
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
        IMPORT, CLAZZ,

        ROOT, ENDL,

        //Literal
        STRING(true), INTEGER(true), DECIMAL(true), BOOLEAN(true),

        OBJECT, /**Function Call**/CALL, ACCESS, ARRAYACCESS, /**self reference**/THIS, ITERATOR,

        /**Parenthesis, Blocks**/OPERATOR, /**Arithmetic**/OPERATOR_A, UNARYMINUS, /**Logical**/OPERATOR_L,

        GID, GID_TEMP, ID, PLACEHOLDER, NULLVALUE,

        BODY, EXECUTOR,

        SYNC, ASYNC,
        
        /**Temporary use only**/EPS, 
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

    public static void main(String[] ar){
        Object bool = true;
        System.out.println(bool instanceof Boolean);
    }
}
