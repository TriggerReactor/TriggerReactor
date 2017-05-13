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
