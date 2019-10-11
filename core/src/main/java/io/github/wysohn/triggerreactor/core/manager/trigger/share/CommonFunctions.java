package io.github.wysohn.triggerreactor.core.manager.trigger.share;

import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

public class CommonFunctions implements SelfReference {
    private static final Random rand = new Random();

    /**
     * get a random integer value between 0 to end
     *
     * <p>
     * Example) #MESSAGE "You rolled the dice: "+(random(6) + 1)
     * </p>
     *
     * @param end exclusive
     * @return
     */
    public int random(int end) {
        return rand.nextInt(end);
    }

    public float random(float end) {
        return rand.nextFloat() * (end - 0) + 0;
    }

    public double random(double end) {
        return rand.nextDouble() * (end - 0) + 0;
    }

    public long random(long end) {
        return (rand.nextLong() & (Long.MAX_VALUE)) % end;
    }

    /**
     * get a random integer value between start to end
     * *<p>
     * Example) #MESSAGE "You rolled the dice: "+random(1, 7)
     * </p>
     *
     * @param start inclusive
     * @param end   exclusive
     * @return
     */
    public int random(int start, int end) {
        return start + rand.nextInt(end - start);
    }

    public float random(float start, float end) {
        return rand.nextFloat() * (end - start) + start;
    }

    public double random(double start, double end) {
        return rand.nextDouble() * (end - start) + start;
    }

    public long random(long start, long end) {
        return (rand.nextLong() & (Long.MAX_VALUE)) % (end - start) + start;
    }

    /**
     * get a string representing the input value rounded to the set decimal place
     * <p>
     * Example) #MESSAGE "1.09 rounded to the nearest tenth is " + round(1.09,1)
     * </p>
     *
     * @param val     the double to be rounded
     * @param decimal the decimal place to round to
     * @return string representing rounded number
     */

    public String round(double val, int decimal) {
        return BigDecimal.valueOf(val).setScale(decimal, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    /**
     * Try to get static value for specified field from specified class.
     *
     * @param className the full class name
     * @param fieldName the name of static field
     * @return the value in the static field
     * @throws ClassNotFoundException error if the specified 'className' does not exist
     * @throws NoSuchFieldException   error if the specified 'fieldName' field does not exist in the class.
     */
    public Object staticGetFieldValue(String className, String fieldName) throws ClassNotFoundException, NoSuchFieldException {
        Class<?> clazz = Class.forName(className);

        Field field = clazz.getField(fieldName);

        try {
            return field.get(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            //Unexpected error
        }

        return null;
    }

    /**
     * Try to set static value for specified field from specified class.
     * This can lead any plugin to catastrpohic failuer if you don't what exactly you are doing.
     * Use it with your own risk.
     *
     * @param className full name of the class
     * @param fieldName name of the static field
     * @param value     the value to save into the field
     * @throws ClassNotFoundException   error if the specified 'className' does not exist
     * @throws NoSuchFieldException     error if the specified 'fieldName' field does not exist in the class.
     * @throws IllegalArgumentException if the 'value' is incompatible with the field type.
     */
    public void staticSetFieldValue(String className, String fieldName, Object value) throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException {
        Class<?> clazz = Class.forName(className);

        Field field = clazz.getField(fieldName);

        try {
            field.set(null, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            //Unexpected error
        }
    }

    /**
     * Invoke the static method of provided class.
     *
     * @param className  the full name of the class.
     * @param methodName the name of static method.
     * @param args       array of arguments. This can be empty if the method doesn't have any arguments.
     *                   (Ex. staticMethod("my.class", "PewPew")
     * @return some value depends on the method; it can be null if the method returns nothing.
     * @throws ClassNotFoundException   error if the 'className' does not exist.
     * @throws NoSuchMethodException    error if the 'methodName' does not exist in the class.
     * @throws IllegalArgumentException error if invalid 'args' are passed to the method.
     * @throws IllegalAccessException
     */
    public Object staticMethod(String className, String methodName, Object... args) throws ClassNotFoundException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = Class.forName(className);

        try {
            return ReflectionUtil.invokeMethod(clazz, null, methodName, args);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get enum value manually. Usually, you can simply pass String value to the
     * Enum parameter, yet in some cases, you need to manually create enum
     * value. For example, TriggerReactor cannot detect Generic argument, so you
     * have to manually create enum value.
     * <p>
     * Example) /trg run #MESSAGE "value is "+parseEnum("org.bukkit.GameMode",
     * "CREATIVE")
     * </p>
     * <p>
     * Example) /trg run player.setGameMode(parseEnum("org.bukkit.GameMode",
     * "CREATIVE"))
     * </p>
     * <p>
     * Example) /trg run player.setGameMode("CREATIVE") //This also works
     * </p>
     *
     * @param enumClassName the full class name of enum.
     * @param valueName     the enum value to parse.
     * @return the actual enum value.
     * @throws ClassNotFoundException if the provided class name doesn't exist or not enum class.
     */
    @SuppressWarnings("unchecked")
    public Object parseEnum(String enumClassName, String valueName) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(enumClassName);
        if (!clazz.isEnum())
            throw new ClassNotFoundException("Found the class [" + clazz.getName() + "], but it wasn't Enum");

        return Enum.valueOf((Class<? extends Enum>) clazz, valueName);
    }

    public SimpleLocation slocation(String world, int x, int y, int z) {
    	if (world == null)
    	{
    		throw new IllegalArgumentException("world cannot be null");
    	}
        return new SimpleLocation(world, x, y, z);
    }

    public Object newInstance(String className, Object... args) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalArgumentException, InvocationTargetException {
        try {
            Class<?> clazz = Class.forName(className);

            List<Constructor<?>> validConstructors = new ArrayList<>();

            for (Constructor<?> constructor : clazz.getConstructors()) {
                Class<?>[] parameterTypes = null;

                parameterTypes = constructor.getParameterTypes();
                if (constructor.isVarArgs()) {
                    if (constructor.isVarArgs() && (parameterTypes.length - args.length >= 2)) {
                        parameterTypes = null;
                        continue;
                    }
                } else {
                    if (parameterTypes.length != args.length) {
                        parameterTypes = null;
                        continue;
                    }
                }

                if (constructor.isVarArgs()) {
                    boolean matches = false;

                    // check non vararg part
                    for (int i = 0; i < parameterTypes.length - 1; i++) {
                        matches = ReflectionUtil.checkMatch(parameterTypes[i], args[i]);
                        if (!matches)
                            break;
                    }

                    // check rest
                    for (int i = parameterTypes.length - 1; i < args.length; i++) {
                        Class<?> arrayType = parameterTypes[parameterTypes.length - 1].getComponentType();

                        matches =  ReflectionUtil.checkMatch(arrayType, args[i]);
                        if (!matches)
                            break;
                    }

                    if (matches) {
                        validConstructors.add(constructor);
                    }
                } else {
                    boolean matches = true;

                    for (int i = 0; i < parameterTypes.length; i++) {
                        matches =  ReflectionUtil.checkMatch(parameterTypes[i], args[i]);
                        if (!matches)
                            break;
                    }

                    if (matches) {
                        validConstructors.add(constructor);
                    }
                }
            }

            if (!validConstructors.isEmpty()) {
                Constructor<?> constructor = validConstructors.get(0);
                for (int i = 1; i < validConstructors.size(); i++) {
                    Constructor<?> targetConstructor = validConstructors.get(i);

                    Class<?>[] params = constructor.getParameterTypes();
                    Class<?>[] otherParams = targetConstructor.getParameterTypes();

                    if (constructor.isVarArgs() && targetConstructor.isVarArgs()) {
                        for (int j = 0; j < params.length; j++) {
                            if (params[j].isAssignableFrom(otherParams[j])) {
                                constructor = targetConstructor;
                                break;
                            }
                        }
                    } else if (constructor.isVarArgs()) {
                        //usually, non-vararg is more specific method. So we use that
                        constructor = targetConstructor;
                    } else if (targetConstructor.isVarArgs()) {
                        //do nothing
                    } else {
                        for (int j = 0; j < params.length; j++) {
                            if (otherParams[j].isEnum()) { // enum will be handled later
                                constructor = targetConstructor;
                                break;
                            } else if (ClassUtils.isAssignable(otherParams[j], params[j], true)) { //narrow down to find the most specific method
                                constructor = targetConstructor;
                                break;
                            }
                        }
                    }
                }

                constructor.setAccessible(true);

                for (int i = 0; i < args.length; i++) {
                    Class<?>[] parameterTypes = constructor.getParameterTypes();

                    if (args[i] instanceof String && i < parameterTypes.length && parameterTypes[i].isEnum()) {
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

                if (constructor.isVarArgs()) {
                    Class<?>[] parameterTypes = constructor.getParameterTypes();

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

                return constructor.newInstance(args);
            }

            if (args.length > 0) {
                StringBuilder builder = new StringBuilder(String.valueOf(args[0].getClass().getSimpleName()));

                for (int i = 1; i < args.length; i++) {
                    builder.append(", " + args[i].getClass().getSimpleName());
                }

                throw new IllegalArgumentException(className+"(" + builder.toString() + "). " +
                        "Make sure the arguments match.");
            } else {
                throw new IllegalArgumentException(className + "(). Make sure the arguments match.");
            }
        } catch (NullPointerException e) {
            StringBuilder builder = new StringBuilder(String.valueOf(args[0]));
            for (int i = 1; i < args.length; i++)
                builder.append("," + String.valueOf(args[i]));
            throw new NullPointerException("Attempted to instantiate " + className + "(" + builder.toString() + ")");
        } catch (IllegalAccessException e){
            throw new RuntimeException("Unexpected exception. Please contact the plugin author!", e);
        }
    }

    /**
     * matches the str by using regular expression regex
     * <p>
     * Example) /trg run IF matches("PewPew", "[a-zA-Z0-9 ]+"); #MESSAGE "Seems to be English"; ENDIF;
     * </p>
     *
     * @param str
     * @param regex
     * @return true if str matches with regex; false if not
     */
    public boolean matches(String str, String regex) {
        return str.matches(regex);
    }

    /**
     * parse the string that contains integer into real integer value.
     * <p>
     * Example) /trg run #MESSAGE parseInt("300") + 50
     * </p>
     *
     * @param str the string that contains integer
     * @return the real integer
     */
    public int parseInt(String str) {
        return Integer.parseInt(str);
    }

    /**
     * parse the string that contains decimal into real decimal value.
     * <p>
     * Example) /trg run #MESSAGE parseDouble("15.30") + 2.12
     * </p>
     *
     * @param str the string that contains decimal
     * @return the real decimal
     */
    public double parseDouble(String str) {
        return Double.parseDouble(str);
    }

    /**
     * create an empty array
     * <p>
     * Example) /trg run temp = array(5); temp[3] = "hi"; #MESSAGE temp[3];
     * </p>
     *
     * @param size size of array
     * @return
     */
    public Object[] array(int size) {
        return new Object[size];
    }

    public List<Object> list() {
        return new ArrayList<Object>();
    }

    public Map<Object, Object> map() {
        return new HashMap<Object, Object>();
    }

    public Set<Object> set() {
        return new HashSet<Object>();
    }

    /**
     * Merge array of String. This is specifically useful for args variable of
     * Command Trigger but not limited to.
     *
     * @param args array to merge
     * @return
     */
    public String mergeArguments(String[] args) {
        return mergeArguments(args, 0, args.length - 1);
    }

    /**
     * Merge array of String. This is specifically useful for args variable of
     * Command Trigger but not limited to.
     *
     * @param args  array to merge
     * @param indexFrom inclusive
     * @return
     */
    public String mergeArguments(String[] args, int indexFrom) {
        return mergeArguments(args, indexFrom, args.length - 1);
    }

    /**
     * Merge array of String. This is specifically useful for args variable of
     * Command Trigger but not limited to.
     *
     * @param args  array to merge
     * @param indexFrom inclusive
     * @param indexTo   inclusive
     * @return
     */
    public String mergeArguments(String[] args, int indexFrom, int indexTo) {
        StringBuilder builder = new StringBuilder(args[indexFrom]);
        for (int i = indexFrom + 1; i <= indexTo; i++) {
            builder.append(" " + args[i]);
        }
        return builder.toString();
    }

    public byte toByte(Number number) {
        return number.byteValue();
    }

    public short toShort(Number number) {
        return number.shortValue();
    }

    public int toInt(Number number) {
        return number.intValue();
    }

    public long toLong(Number number) {
        return number.longValue();
    }

    public float toFloat(Number number) {
        return number.floatValue();
    }

    public double toDouble(Number number) {
        return number.doubleValue();
    }

    public double sqrt(int num) {

        double squareRoot = num / 2;
        double test = 0;

        while ((test - squareRoot) != 0) {
            test = squareRoot;
            squareRoot = (test + (num / test)) / 2;
        }

        return squareRoot;
    }

    /**
     * Translate money into specified country's currency format. You need to
     * provide exact locale provided in
     * http://www.oracle.com/technetwork/java/javase/java8locales-2095355.html
     *
     * @param money
     * @param locale1 language code (Ex. en)
     * @param locale2 country code (Ex. US)
     * @return formatted currecy
     */
    public String formatCurrency(double money, String locale1, String locale2) {
        Locale locale = new Locale(locale1, locale2);
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        return currencyFormatter.format(money);
    }

    /**
     * Translate money into specified country's currency format. US currency
     * will be used.
     *
     * @param money
     * @return formatted currecy
     */
    public String formatCurrency(double money) {
        return formatCurrency(money, "en", "US");
    }

    /**
     * Return the name of the class that provided object was created by.
     * if hasFullPath is set to true, this will return class name that
     * contains full path. (Ex. org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer)
     *
     * @param value  the object to get its class name
     * @param withFullPath  if true this returns class name containing full path
     * @return source class of the object
     */
    public String typeOf(Object value, boolean withFullPath){
        if (withFullPath == false) {
            return value.getClass().getSimpleName();
        }else {
            return value.getClass().getCanonicalName();
        }
    }

}
