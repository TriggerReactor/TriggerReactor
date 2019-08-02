package io.github.wysohn.triggerreactor.core.manager.trigger.share;

import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

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
        Class<?> clazz = Class.forName(className);

        Class<?>[] types = new Class[args.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = args[i].getClass();
        }

        Constructor con = null;
        Constructor[] cons = clazz.getConstructors();
        outer:
        for (Constructor check : cons) {
            Class<?>[] params = check.getParameterTypes();
            if (params.length == types.length) {
                for (int i = 0; i < types.length; i++) {
                    if (!params[i].isAssignableFrom(types[i])) {
                        break;
                    }

                    //we found the constructor
                    con = check;
                    break outer;
                }
            }
        }

        if (con != null) {
            con.setAccessible(true);

            try {
                return con.newInstance(args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return null;
        } else {
            StringBuilder builder = new StringBuilder("Could not found counstuctor with matching parameters.");
            builder.append(" -- ");
            builder.append(className + "<init>");
            builder.append("(");
            for (int i = 0; i < types.length - 1; i++)
                builder.append(types[i].getSimpleName() + ", ");
            builder.append(types[types.length - 1].getSimpleName());
            builder.append(")");

            throw new NoSuchMethodException(builder.toString());
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
     * @param argument array to merge
     * @return
     */
    public String mergeArguments(String[] args) {
        return mergeArguments(args, 0, args.length - 1);
    }

    /**
     * Merge array of String. This is specifically useful for args variable of
     * Command Trigger but not limited to.
     *
     * @param argument  array to merge
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
     * @param argument  array to merge
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
}
