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

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static <T extends Executable> List<T> getValidExecutables(Class<?> clazz,
                                                                      String name,
                                                                      Object[] args,
                                                                      Function<Class<?>, T[]> extractFn) {
        List<T> validMethods = new ArrayList<>();
        List<T> validVarargMethods = new ArrayList<>();

        for (T executable : extractFn.apply(clazz)) {
            // select method with matching name. All if name is null.
            if (name != null && !executable.getName().equals(name))
                continue;

            // get the method's argument types
            Class<?>[] parameterTypes = executable.getParameterTypes();
            if (executable.isVarArgs()) {
                // the vararg value will be passed as an Array
                // so expect args + the Array, or args + no Array (which is when nothing is passed to varargs)
                // since vararg will handle varying amount of arguments,
                // check if at least non-varargs parts are filled
                if (args.length < parameterTypes.length - 1) {
                    // if non-varargs are not filled, it's not the method we are looking for.
                    continue;
                }
            } else {
                // regular methods are easy. Just see if the size of arguments both matches
                if (parameterTypes.length != args.length) {
                    continue;
                }
            }

            boolean matches;
            if (executable.isVarArgs()) {
                matches = true;

                // check non-vararg part
                for (int i = 0; i < parameterTypes.length - 1; i++) {
                    matches = i < args.length && checkMatch(parameterTypes[i], args[i]);
                    if (!matches)
                        break;
                }

                // check vararg part
                if (matches) {
                    Class<?> methodVarargType = parameterTypes[parameterTypes.length - 1].getComponentType();
                    for (int i = parameterTypes.length - 1; i < args.length; i++) {
                        matches = checkMatch(methodVarargType, args[i]);
                        if (!matches)
                            break;
                    }
                }

                if (matches) {
                    validVarargMethods.add(executable);
                }
            } else {
                matches = true;

                // check one on one.
                for (int i = 0; i < parameterTypes.length; i++) {
                    matches = checkMatch(parameterTypes[i], args[i]);
                    if (!matches)
                        break;
                }

                if (matches) {
                    validMethods.add(executable);
                }
            }
        }

        return Stream.concat(validVarargMethods.stream(), validMethods.stream())
                .collect(Collectors.toList());
    }

    private interface ExtractExecutable<T extends Executable> {
        T apply(Class<?> clazz, String name, Class<?>[] parameters) throws NoSuchMethodException;
    }

    private static <T extends Executable> T findBestFit(Class<?> clazz, String name, List<T> validMethods,
                                                        Object[] args,
                                                        ExtractExecutable<T> extractFn) {
        // we found all methods that may can be used with the input arguments
        // yet we still have to find the best fit.
        // For example, method(1, 1) would be more suitable with method(int, int) than method(double, double)
        // while both of them can accept the arguments without problem.
        if (!validMethods.isEmpty()) {
            // pick one method
            T executable = validMethods.get(0);

            // compare the current method with other methods
            // to see if there is a better candidate
            c:
            for (int i = 1; i < validMethods.size(); i++) {
                T targetExecutable = validMethods.get(i);

                Class<?>[] currentParams = executable.getParameterTypes();
                Class<?>[] compareParams = targetExecutable.getParameterTypes();

                int len = Math.max(currentParams.length, compareParams.length);
                for (int j = 0; j < len; j++) {
                    // we already checked that all candidates can handle the arguments
                    // find at least one argument of other method that is more specific than of the current method
                    // if found one, don't have to check other arguments as we already know they will work

                    Class<?> currentParam = currentParams[Math.min(currentParams.length - 1, j)];
                    Class<?> compareParam = compareParams[Math.min(compareParams.length - 1, j)];

                    if (executable.isVarArgs() && j >= currentParams.length - 1)
                        currentParam = currentParam.getComponentType();
                    if (targetExecutable.isVarArgs() && j >= compareParams.length - 1)
                        compareParam = compareParam.getComponentType();

                    if (!ClassUtils.isAssignable(compareParam, currentParam, true))
                        continue c;
                }

                executable = targetExecutable;
            }

            executable.setAccessible(true);

            for (int i = 0; i < args.length; i++) {
                Class<?>[] parameterTypes = executable.getParameterTypes();
                Class<?>[] types = new Class<?>[args.length];
                for (int k = 0; k < args.length; k++)
                    types[k] = Optional.ofNullable(args[k])
                            .map(Object::getClass)
                            .orElse(null);

                if (args[i] instanceof InvocationHandler && i < parameterTypes.length && parameterTypes[i].isInterface()) {
                    // we need to proxy the interface to reroute calls to the InvocationHandler
                    InvocationHandler handler = (InvocationHandler) args[i];
                    args[i] = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                            new Class[]{parameterTypes[i]},
                            handler);
                }

                if (args[i] instanceof String && i < parameterTypes.length && parameterTypes[i].isEnum()) {
                    // Some methods already provide overloaded method to handle String instead of Enum
                    // So check it first before converting String to Enum manually
                    try {
                        executable = extractFn.apply(clazz, name, types);
                    } catch (NoSuchMethodException ex2) {
                        try {
                            args[i] = Enum.valueOf((Class<? extends Enum>) parameterTypes[i], (String) args[i]);
                        } catch (IllegalArgumentException ex1) {
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

            return executable;
        } else {
            return null;
        }
    }

    @SuppressWarnings({"unchecked"})
    public static Object invokeMethod(Class<?> clazz, Object obj, String methodName, Object... args)
            throws NoSuchMethodException, IllegalArgumentException, InvocationTargetException, IllegalAccessException {
        try {
            List<Method> validMethods = getValidExecutables(clazz, methodName, args, Class::getMethods);
            if (validMethods.isEmpty())
                throw new NoSuchMethodException(buildFailMessage(clazz, methodName, args));

            // we found all methods that may can be used with the input arguments
            // yet we still have to find the best fit.
            // For example, method(1, 1) would be more suitable with method(int, int) than method(double, double)
            // while both of them can accept the arguments without problem.
            // pick one method
            Method method = findBestFit(clazz, methodName, validMethods, args, Class::getMethod);
            if (method == null)
                throw new NoSuchMethodException(buildFailMessage(clazz, methodName, args));

            // we need to convert the last part of input arguments as Array
            if (method.isVarArgs()) {
                args = mergeVarargs(args, method.getParameterTypes());
            }

            return method.invoke(obj, args);
        } catch (NullPointerException e) {
            throw new RuntimeException(buildFailMessage(clazz, methodName, args), e);
        }
    }

    private static Object[] mergeVarargs(Object[] args, Class<?>[] parameterTypes) {
        // build the Array to be used
        Object varargs = Array.newInstance(parameterTypes[parameterTypes.length - 1].getComponentType(),
                args.length - parameterTypes.length + 1);
        for (int k = 0; k < Array.getLength(varargs); k++)
            Array.set(varargs, k, args[parameterTypes.length - 1 + k]);

        // copy the non-vararg part
        Object[] newArgs = new Object[parameterTypes.length];
        System.arraycopy(args, 0, newArgs, 0, newArgs.length - 1);
        // and the last argument is the Array we just created
        newArgs[newArgs.length - 1] = varargs;

        // replace the argument with the vararg merged version
        return newArgs;
    }

    private static String buildFailMessage(Class<?> clazz, String methodName, Object[] args) {
        StringBuilder builder = new StringBuilder(args.length > 0 ? String.valueOf(args[0]) : "");
        for (int i = 1; i < args.length; i++)
            builder.append(",").append(args[i]);
        return "[" + Optional.ofNullable(clazz)
                .map(Class::getSimpleName)
                .orElse(null) + "]." + methodName + "(" + builder.toString() + ")";
    }

    public static boolean checkMatch(Class<?> parameterType, Object arg) {
        // skip enum if argument was String. We will try valueOf() later
        if(arg instanceof String && parameterType.isEnum())
            return true;

        // if InvocationHandler is provided for the interface parameter, skip it.
        // will try it later
        if(arg instanceof InvocationHandler && parameterType.isInterface())
            return true;

        return ClassUtils.isAssignable(arg == null ? null : arg.getClass(), parameterType, true);
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

//        URL[] urls = ((URLClassLoader) cl).getURLs();
//        for (URL url : urls) {
//            //System.out.println(url.getFile());
//            File jar = new File(url.getFile());
//
//            if (jar.isDirectory()) {
//                File subdir = new File(jar, packageName);
//                if (!subdir.exists())
//                    continue;
//                File[] files = subdir.listFiles();
//                for (File file : files) {
//                    if (!file.isFile())
//                        continue;
//                    if (file.getName().endsWith(".class"))
//                        classes.add(file.getName().substring(0, file.getName().length() - 6).replace('/', '.'));
//                }
//            } else {
//                // try to open as ZIP
//                try {
//                    ZipFile zip = new ZipFile(jar);
//                    for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
//                        ZipEntry entry = entries.nextElement();
//                        String name = entry.getName();
//                        if (!name.startsWith(packageName))
//                            continue;
//                        if (name.endsWith(".class") && name.indexOf('$') < 0)
//                            classes.add(name.substring(0, name.length() - 6).replace('/', '.'));
//                    }
//                    zip.close();
//                } catch (ZipException e) {
//                    //System.out.println("Not a ZIP: " + e.getMessage());
//                } catch (IOException e) {
//                    System.err.println(e.getMessage());
//                }
//            }
//        }

        return classes;
    }

    public static Object constructNew(Class<?> clazz, Object... args) throws NoSuchMethodException, InstantiationException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (args.length < 1) {
            return clazz.newInstance();
        } else {
            List<Constructor<?>> validConstructors = getValidExecutables(clazz, null, args, Class::getConstructors);

            if (validConstructors.isEmpty())
                throw new NoSuchMethodException(buildFailMessage(clazz, "<init>", args));

            Constructor<?> target = findBestFit(clazz, null, validConstructors, args, (c, name, params) ->
                    c.getConstructor(params));
            if (target == null)
                throw new NoSuchMethodException(buildFailMessage(clazz, "<init>", args));

            if (target.isVarArgs()) {
                args = mergeVarargs(args, target.getParameterTypes());
            }

            return target.newInstance(args);
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
