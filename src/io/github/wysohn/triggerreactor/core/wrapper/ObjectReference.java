package io.github.wysohn.triggerreactor.core.wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.github.wysohn.triggerreactor.tools.ClassUtil;

public class ObjectReference {
    private final Object target;
    private final String varName;
    private final Map<String, Object> fieldMap;
    private final Map<String, Object> methodMap;

    public ObjectReference(Object target, String varName) {
        this.target = target;
        this.varName = varName;
        this.fieldMap = extractFields(target);
        this.methodMap = extractMethods(target);
    }

    public String getVarName() {
        return varName;
    }

    public <T> T getFieldValue(String key, T def){
        Field field = (Field) fieldMap.get(key);
        try {
            if(field != null){
                field.setAccessible(true);
                return (T) field.get(target);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return def;
    }

    public void setFieldValue(String key, Object value){
        Field field = (Field) fieldMap.get(key);

        try {
            if (field != null){
                field.setAccessible(true);
                field.set(target, value);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public Object invokeMethod(String key, Object[] params){
        Method method = (Method) methodMap.get(key);

        try {
            if(method != null){
                method.setAccessible(true);
                return method.invoke(target, params);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String toString() {
        return "ObjectReference [target=" + target + "]";
    }

    private static Map<String, Object> extractFields(Object obj){
        Map<String, Object> map = new HashMap<>();

        for(Field field : ClassUtil.getAllPublicFields(new ArrayList<>(), obj.getClass())){
            map.put(field.getName(), field);
        }

        return map;
    }

    private static Map<String, Object> extractMethods(Object obj){
        Map<String, Object> map = new HashMap<>();

        for(Method method : ClassUtil.getAllPublicMethods(new ArrayList<>(), obj.getClass())){
            map.put(method.getName(), method);
        }

        return map;
    }
}
