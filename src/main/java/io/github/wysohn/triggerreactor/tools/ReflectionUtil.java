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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.lang.ClassUtils;

public class ReflectionUtil {
    public static void setField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalArgumentException{
        Class<?> clazz = obj.getClass();

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setFinalField(Object obj, String fieldName, Object value) throws NoSuchFieldException{
        setFinalField(obj.getClass(), obj, fieldName, value);
    }

    public static void setFinalField(Class<?> clazz, Object obj, String fieldName, Object value) throws NoSuchFieldException{
        Field field = clazz.getDeclaredField(fieldName);

        setFinalField(obj, field, value);
    }

    /**
     * https://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection
     * @param field
     * @param newValue
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws Exception
     */
    private static void setFinalField(Object target, Field field, Object newValue) throws NoSuchFieldException{
        field.setAccessible(true);

        Field modifiersField = null;
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
        } catch (SecurityException e1) {
            e1.printStackTrace();
        }

        modifiersField.setAccessible(true);
        try {
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            field.set(target, newValue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object getField(Object obj, String fieldName) throws NoSuchFieldException, IllegalArgumentException{
        Class<?> clazz = obj.getClass();

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getField(Class<?> clazz, Object obj, String fieldName) throws NoSuchFieldException, IllegalArgumentException{
        Field field = clazz.getDeclaredField(fieldName);
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

        try{
            for (Method method : clazz.getMethods()) {
                if (!method.getName().equals(methodName)) {
                    continue;
                }

                Class<?>[] parameterTypes = method.getParameterTypes();
                if(parameterTypes.length != args.length)
                    continue;

                boolean matches = true;
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (!ClassUtils.isAssignable(args[i].getClass(), parameterTypes[i], true)) {
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
        }catch(NullPointerException e){
            StringBuilder builder = new StringBuilder(String.valueOf(args[0]));
            for (int i = 1; i < args.length; i++)
                builder.append("," + String.valueOf(args[i]));
            throw new NullPointerException("Call "+methodName+"("+builder.toString()+")");
        }
    }

    public static Map<String, Object> extractVariables(Object e){
        Map<String, Object> map = new HashMap<String, Object>();

        Class<?> clazz = e.getClass();
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

    public static Map<String, Object> extractVariablesWithEnumAsString(Object e){
        Map<String, Object> map = new HashMap<String, Object>();

        Class<?> clazz = e.getClass();
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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection

    public static List<String> getAllClasses(ClassLoader cl, String packageName){
        packageName = packageName.replace('.', '/');

        List<String> classes = new ArrayList<>();

        URL[] urls = ((URLClassLoader) cl).getURLs();
        for (URL url : urls) {
            //System.out.println(url.getFile());
            File jar = new File(url.getFile());

            if (jar.isDirectory()) {
                File subdir = new File(jar, packageName);
                if (!subdir.exists())
                    continue;
                File[] files = subdir.listFiles();
                for (File file : files) {
                    if (!file.isFile())
                        continue;
                    if (file.getName().endsWith(".class"))
                        classes.add(file.getName().substring(0, file.getName().length() - 6).replace('/', '.'));
                }
            }

            else {
                // try to open as ZIP
                try {
                    ZipFile zip = new ZipFile(jar);
                    for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
                        ZipEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (!name.startsWith(packageName))
                            continue;
                        if (name.endsWith(".class") && name.indexOf('$') < 0)
                            classes.add(name.substring(0, name.length() - 6).replace('/', '.'));
                    }
                    zip.close();
                } catch (ZipException e) {
                    //System.out.println("Not a ZIP: " + e.getMessage());
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }

        return classes;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] ar){
       System.out.println(ClassUtils.isAssignable(Integer.class, double.class, true));
    }
}
