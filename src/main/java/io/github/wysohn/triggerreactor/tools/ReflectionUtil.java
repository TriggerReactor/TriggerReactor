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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.Event;

public class ReflectionUtil {
    public static void setField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalArgumentException{
        Class<?> clazz = obj.getClass();

        Field field = clazz.getField(fieldName);
        field.setAccessible(true);

        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object getField(Object obj, String fieldName) throws NoSuchFieldException, IllegalArgumentException{
        Class<?> clazz = obj.getClass();

        Field field = clazz.getField(fieldName);
        field.setAccessible(true);

        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object invokeMethod(Object obj, String methodName, Object... args) throws NoSuchMethodException, IllegalArgumentException, InvocationTargetException{
        Class<?> clazz = obj.getClass();

        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            if(parameterTypes.length != args.length)
                continue;

            boolean matches = true;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (!parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                try {
                    method.setAccessible(true);
                    return method.invoke(obj, args);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if (args.length > 1) {
            StringBuilder builder = new StringBuilder(args[0].getClass().getName());
            for (int i = 1; i < args.length; i++)
                builder.append("," + args[i].getClass().getName());
            throw new NoSuchMethodException(methodName+"("+builder.toString()+")");
        }else{
            throw new NoSuchMethodException(methodName+"()");
        }
    }

    public static Map<String, Object> extractVariables(Event e){
        Map<String, Object> map = new HashMap<String, Object>();

        Class<? extends Event> clazz = e.getClass();
        for(Field field : getAllFields(new ArrayList<Field>(), clazz)){
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(e));
            } catch (IllegalArgumentException | IllegalAccessException e1) {
                e1.printStackTrace();
            }
        }

        return map;
    }

    public static Map<String, Object> extractVariablesWithEnumAsString(Event e){
        Map<String, Object> map = new HashMap<String, Object>();

        Class<? extends Event> clazz = e.getClass();
        for(Field field : getAllFields(new ArrayList<Field>(), clazz)){
            field.setAccessible(true);
            try {
                if(field.getClass().isEnum()){
                    Enum enumVal = (Enum) field.get(e);
                    map.put(field.getName(), enumVal.name());
                }else{
                    map.put(field.getName(), field.get(e));
                }
            } catch (IllegalArgumentException | IllegalAccessException e1) {
                e1.printStackTrace();
            }
        }

        return map;
    }

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
