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

        if(value instanceof Float){
            value = (double) ((float) value);
        }
        this.value = value;
        this.row = row;
        this.col = col;
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

    public boolean isInt(){
        return value instanceof Integer;
    }

    public boolean isDouble(){
        return value instanceof Double;
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
        return !isInt() && !isDouble() && !isBoolean() && !isArray();
    }

    public int toInt(){
        return (int) value;
    }

    public double toDouble(){
        return (double) value;
    }

    public boolean isNumeric() {
        return isInt() || isDouble();
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

        OBJECT, /**Function Call**/CALL, ACCESS, ARRAYACCESS, /**self reference**/THIS, ITERATOR,

        /**Parenthesis, Blocks**/OPERATOR, /**Arithmetic**/OPERATOR_A, UNARYMINUS, /**Logical**/OPERATOR_L,

        GID, ID, PLACEHOLDER, NULLVALUE,

        BODY, EXECUTOR,

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
