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
package io.github.wysohn.triggerreactor.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ClassUtil {
    /**
     * http://stackoverflow.com/questions/1042798/retrieving-the-inherited-attribute-names-values-using-java-reflection
     * @param fields
     * @param c
     * @return
     */
    public static List<Field> getAllFields(List<Field> fields, Class<?> c){
        fields.addAll(Arrays.asList(c.getDeclaredFields()));

        if(c.getSuperclass() != null){
            fields = getAllFields(fields, c.getSuperclass());
        }

        return fields;
    }

    public static List<Field> getAllPublicFields(List<Field> fields, Class<?> c){
        fields.addAll(Arrays.asList(c.getFields()));

        if(c.getSuperclass() != null){
            fields = getAllPublicFields(fields, c.getSuperclass());
        }

        return fields;
    }

    public static List<Method> getAllPublicMethods(List<Method> methods, Class<?> c){
        methods.addAll(Arrays.asList(c.getMethods()));

        if(c.getSuperclass() != null){
            methods = getAllPublicMethods(methods, c.getSuperclass());
        }

        return methods;
    }
}
