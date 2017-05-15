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
                if(params.length == 0){
                    return method.invoke(target);
                }else{
                    return method.invoke(target, params);
                }
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
