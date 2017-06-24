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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.bukkit.event.Event;

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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //https://dzone.com/articles/get-all-classes-within-package

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(".jar");
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

/*    public static Set<Class<?>> getClasses(File fullpath, String packageName) {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(fullpath));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

            }

            for (Enumeration<JarEntry> entry = file.entries(); entry.hasMoreElements();) {
               JarEntry jarEntry = entry.nextElement();
               String name = jarEntry.getName().replace("/", ".");
               if(name.startsWith(packageName) && name.endsWith(".class"))
                   classes.add(Class.forName(name.substring(0, name.length() - 6)));
            }
            file.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return classes;
    }*/
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] ar){
       System.out.println(ClassUtils.isAssignable(Integer.class, double.class, true));
    }
}
