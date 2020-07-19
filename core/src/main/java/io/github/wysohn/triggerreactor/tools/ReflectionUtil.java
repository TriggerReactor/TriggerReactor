/*******************************************************************************
 *     Copyright (C) 2017, 2018 wysohn
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

import org.apache.commons.lang3.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ReflectionUtil {
    public static void setField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalArgumentException {
        Class<?> clazz = obj.getClass();

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setFinalField(Object obj, String fieldName, Object value) throws NoSuchFieldException {
        setFinalField(obj.getClass(), obj, fieldName, value);
    }

    public static void setFinalField(Class<?> clazz, Object obj, String fieldName, Object value) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(fieldName);

        setFinalField(obj, field, value);
    }

    /**
     * https://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection
     *
     * @param field
     * @param newValue
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws Exception
     */
    private static void setFinalField(Object target, Field field, Object newValue) throws NoSuchFieldException {
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

    public static Object getField(Object obj, String fieldName) throws NoSuchFieldException, IllegalArgumentException {
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

    public static Object getField(Class<?> clazz, Object obj, String fieldName) throws NoSuchFieldException, IllegalArgumentException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    @SuppressWarnings({"unchecked"})
    public static Object invokeMethod(Class<?> clazz, Object obj, String methodName, Object... args)
            throws NoSuchMethodException, IllegalArgumentException, InvocationTargetException, IllegalAccessException {
        try {
            List<Method> validMethods = new ArrayList<>();

            for (Method method : clazz.getMethods()) {
                Class<?>[] parameterTypes = null;

                if (!method.getName().equals(methodName)) {
                    continue;
                }

                parameterTypes = method.getParameterTypes();
                if (method.isVarArgs()) {
                    if (method.isVarArgs() && (parameterTypes.length - args.length >= 2)) {
                        parameterTypes = null;
                        continue;
                    }
                } else {
                    if (parameterTypes.length != args.length) {
                        parameterTypes = null;
                        continue;
                    }
                }

                if (method.isVarArgs()) {
                    boolean matches = false;

                    // check non vararg part
                    for (int i = 0; i < parameterTypes.length - 1; i++) {
                        matches = checkMatch(parameterTypes[i], args[i]);
                        if (!matches)
                            break;
                    }

                    // check rest
                    for (int i = parameterTypes.length - 1; i < args.length; i++) {
                        Class<?> arrayType = parameterTypes[parameterTypes.length - 1].getComponentType();

                        matches = checkMatch(arrayType, args[i]);
                        if (!matches)
                            break;
                    }

                    if (matches) {
                        validMethods.add(method);
                    }
                } else {
                    boolean matches = true;

                    for (int i = 0; i < parameterTypes.length; i++) {
                        matches = checkMatch(parameterTypes[i], args[i]);
                        if (!matches)
                            break;
                    }

                    if (matches) {
                        validMethods.add(method);
                    }
                }
            }

            if (!validMethods.isEmpty()) {
                Method method = validMethods.get(0);
                for (int i = 1; i < validMethods.size(); i++) {
                    Method targetMethod = validMethods.get(i);

                    Class<?>[] params = method.getParameterTypes();
                    Class<?>[] otherParams = targetMethod.getParameterTypes();

                    if (method.isVarArgs() && targetMethod.isVarArgs()) {
                        for (int j = 0; j < params.length; j++) {
                            if (params[j].isAssignableFrom(otherParams[j])) {
                                method = targetMethod;
                                break;
                            }
                        }
                    } else if (method.isVarArgs()) {
                        //usually, non-vararg is more specific method. So we use that
                        method = targetMethod;
                    } else if (targetMethod.isVarArgs()) {
                        //do nothing
                    } else {
                        for (int j = 0; j < params.length; j++) {
                            if (otherParams[j].isEnum()) { // enum will be handled later
                                method = targetMethod;
                                break;
                            } else if (ClassUtils.isAssignable(otherParams[j], params[j], true)) { //narrow down to find the most specific method
                                method = targetMethod;
                                break;
                            }
                        }
                    }
                }

                method.setAccessible(true);

                for (int i = 0; i < args.length; i++) {
                    Class<?>[] parameterTypes = method.getParameterTypes();

                    if (args[i] instanceof String && i < parameterTypes.length && parameterTypes[i].isEnum()) {
                        try {
                            args[i] = Enum.valueOf((Class<? extends Enum>) parameterTypes[i], (String) args[i]);
                        } catch (IllegalArgumentException ex1) {
                            // Some overloaded methods already has
                            // String to Enum conversion
                            // So just lets see if one exists
                            Class<?>[] types = new Class<?>[args.length];
                            for (int k = 0; k < args.length; k++)
                                types[k] = args[k].getClass();

                            try {
                                Method alternative = clazz.getMethod(methodName, types);
                                return alternative.invoke(obj, args);
                            } catch (NoSuchMethodException ex2) {
                                throw new RuntimeException("Tried to convert value [" + args[i]
                                        + "] to Enum [" + parameterTypes[i]
                                        + "] or find appropriate method but found nothing. Make sure"
                                        + " that the value [" + args[i]
                                        + "] matches exactly with one of the Enums in [" + parameterTypes[i]
                                        + "] or the method you are looking exists.");
                            }
                        }
                    }
                }

                if (method.isVarArgs()) {
                    Class<?>[] parameterTypes = method.getParameterTypes();

                    Object varargs = Array.newInstance(
                            parameterTypes[parameterTypes.length - 1].getComponentType(),
                            args.length - parameterTypes.length + 1);
                    for (int k = 0; k < Array.getLength(varargs); k++) {
                        Array.set(varargs, k, args[parameterTypes.length - 1 + k]);
                    }

                    Object[] newArgs = new Object[parameterTypes.length];
                    for (int k = 0; k < newArgs.length - 1; k++) {
                        newArgs[k] = args[k];
                    }
                    newArgs[newArgs.length - 1] = varargs;

                    args = newArgs;
                }

                return method.invoke(obj, args);
            }

            if (args.length > 0) {
                StringBuilder builder = new StringBuilder(args[0].getClass().getSimpleName());

                for (int i = 1; i < args.length; i++) {
                    builder.append(", " + args[i].getClass().getSimpleName());
                }

                throw new NoSuchMethodException(methodName + "(" + builder.toString() + ")");
            } else {
                throw new NoSuchMethodException(methodName + "()");
            }
        } catch (NullPointerException e) {
            StringBuilder builder = new StringBuilder(String.valueOf(args[0]));
            for (int i = 1; i < args.length; i++)
                builder.append("," + args[i]);
            throw new NullPointerException("Call " + methodName + "(" + builder.toString() + ")");
        }
    }

    public static boolean checkMatch(Class<?> parameterType, Object arg) {
        // skip enum if argument was String. We will try valueOf() later
        return arg instanceof String && parameterType.isEnum()
                || ClassUtils.isAssignable(arg == null ? null : arg.getClass(), parameterType, true);
    }

    private static boolean compareClass(Class<?> clazz1, Class<?> clazz2) {
        if (ClassUtils.isPrimitiveWrapper(clazz1))
            clazz1 = ClassUtils.wrapperToPrimitive(clazz1);

        if (ClassUtils.isPrimitiveWrapper(clazz2))
            clazz2 = ClassUtils.wrapperToPrimitive(clazz2);

        if (clazz1 != null) {
            return clazz1.equals(clazz2);
        } else if (clazz2 != null) {
            return clazz2.equals(clazz1);
        } else {
            return true;
        }
    }

    public static Object invokeMethod(Object obj, String methodName, Object... args) throws NoSuchMethodException, IllegalArgumentException, InvocationTargetException, IllegalAccessException {
        Class<?> clazz = obj.getClass();

        return invokeMethod(clazz, obj, methodName, args);
    }

    /**
     * extract all possible field values. Primitive types will be 'copied,' and
     * reference types can be referenced.
     *
     * @param e
     * @return
     */
    public static Map<String, Object> extractVariables(Object e) {
        Map<String, Object> map = new HashMap<String, Object>();

        Class<?> clazz = e.getClass();
        for (Field field : getAllFields(new ArrayList<Field>(), clazz)) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(e));
            } catch (IllegalArgumentException | IllegalAccessException e1) {
                e1.printStackTrace();
            }
        }

        return map;
    }

    public static Map<String, Object> extractVariablesWithEnumAsString(Object e) {
        Map<String, Object> map = new HashMap<String, Object>();

        Class<?> clazz = e.getClass();
        for (Field field : getAllFields(new ArrayList<Field>(), clazz)) {
            field.setAccessible(true);
            try {
                if (field.getClass().isEnum()) {
                    Enum<?> enumVal = (Enum<?>) field.get(e);
                    map.put(field.getName(), enumVal.name());
                } else {
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
     *
     * @param fields
     * @param c
     * @return
     */
    public static List<Field> getAllFields(List<Field> fields, Class<?> c) {
        fields.addAll(Arrays.asList(c.getDeclaredFields()));

        if (c.getSuperclass() != null) {
            fields = getAllFields(fields, c.getSuperclass());
        }

        return fields;
    }

    public static List<Field> getAllPublicFields(List<Field> fields, Class<?> c) {
        fields.addAll(Arrays.asList(c.getFields()));

        if (c.getSuperclass() != null) {
            fields = getAllPublicFields(fields, c.getSuperclass());
        }

        return fields;
    }

    public static List<Method> getAllPublicMethods(List<Method> methods, Class<?> c) {
        methods.addAll(Arrays.asList(c.getMethods()));

        if (c.getSuperclass() != null) {
            methods = getAllPublicMethods(methods, c.getSuperclass());
        }

        return methods;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection

    public static List<String> getAllClasses(ClassLoader cl, String packageName) {
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
            } else {
                // try to open as ZIP
                try {
                    ZipFile zip = new ZipFile(jar);
                    for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
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

    public static Object constructNew(Class<?> clazz, Object[] args) throws NoSuchMethodException, InstantiationException, IllegalArgumentException, IllegalAccessException {
        if (args.length < 1) {
            return clazz.newInstance();
        } else {
            Class<?>[] paramTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] == null ? null : args[i].getClass();
            }

            List<Constructor<?>> possibleTarget = new ArrayList<>();
            out:
            for (Constructor<?> con : clazz.getConstructors()) {
                if (con.isVarArgs()) {
                    for (int i = 0; i < paramTypes.length; i++) {
                        Class<?> paramType = getParamType(con, i);

                        if (!checkMatch(paramType, args[i]))
                            continue out;
                    }

                    possibleTarget.add(con);
                } else {
                    if (con.getParameterCount() != paramTypes.length)
                        continue;

                    for (int i = 0; i < paramTypes.length; i++) {
                        Class<?> paramType = paramTypes[i];

                        if (!checkMatch(paramType, args[i]))
                            continue out;
                    }

                    possibleTarget.add(con);
                }
            }

            if (possibleTarget.isEmpty())
                return null;

            Constructor<?> target = possibleTarget.get(0);
            if (possibleTarget.size() > 1) {
                s:
                for (Constructor<?> con : possibleTarget.subList(1, possibleTarget.size())) {
                    // compare all parameters before replacing it
                    for (int i = 0; i < paramTypes.length; i++) {
                        Class<?> paramsOther = getParamType(con, i);
                        Class<?> params = getParamType(target, i);

                        if (!ClassUtils.isAssignable(paramsOther, params, true)) {
                            // not a good replacement
                            continue s;
                        }
                    }

                    target = con;
                }
            }

            if (target.isVarArgs()) {
                Class<?>[] parameterTypes = target.getParameterTypes();

                Object varargs = Array.newInstance(
                        parameterTypes[parameterTypes.length - 1].getComponentType(),
                        args.length - parameterTypes.length + 1);
                for (int k = 0; k < Array.getLength(varargs); k++) {
                    Array.set(varargs, k, args[parameterTypes.length - 1 + k]);
                }

                Object[] newArgs = new Object[parameterTypes.length];
                for (int k = 0; k < newArgs.length - 1; k++) {
                    newArgs[k] = args[k];
                }
                newArgs[newArgs.length - 1] = varargs;

                args = newArgs;
            }

            try {
                return target.newInstance(args);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private static Class<?> getParamType(Executable exec, int index) {
        Class<?>[] paramTypes = exec.getParameterTypes();

        if (exec.isVarArgs()) {
            int varArgIndex = paramTypes.length - 1;
            Class<?> varArgType = paramTypes[varArgIndex].getComponentType();

            return index < varArgIndex ? paramTypes[index] : varArgType;
        } else {
            return paramTypes[index];
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/*    public static void main(String[] ar) throws NoSuchMethodException, IllegalArgumentException, InvocationTargetException, IllegalAccessException{
        System.out.println(invokeMethod(Test.class, (Object) null, "someMethod1", 1,2,"hey"));
    }

    public static class Test{
        public static Object someMethod1(Object... val) {
            return "Object";
        }

        public static Object someMethod1(Integer... val) {
            return "Integer";
        }
        public static Object someMethod1(Integer val) {
            return "test";
        }
    }*/

}
