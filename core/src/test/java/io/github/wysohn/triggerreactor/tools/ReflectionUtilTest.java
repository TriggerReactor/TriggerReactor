package io.github.wysohn.triggerreactor.tools;

import io.github.wysohn.triggerreactor.core.script.interpreter.lambda.LambdaFunction;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReflectionUtilTest {

    @Test
    public void invokeMethod1() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // test overloads
        assertEquals(1, ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1",
                7, 7, 7));

        assertEquals(2, ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1",
                7.0, 7.0, 7));

        assertEquals(2, ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1",
                7, 7.0, 7));

        assertEquals(2, ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1",
                7, 7, 7.0));

        assertEquals(3, ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1",
                new SomeValue("v1"), new SomeValue("v2"), new SomeValue("v3")));

        assertEquals(4, ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1",
                new SomeValue("v1"), 1, new SomeValue("v2")));

        assertEquals(4, ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1",
                7, 7.0, new SomeValue("v1")));
    }

    @Test
    public void invokeMethod2() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // test varargs
        assertArrayEquals(new int[0], (int[]) ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1"));

        // ambiguous call
//        assertArrayEquals(new int[]{}, (int[]) ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
//                (Object) null,
//                "method1",
//                1));

        // ambiguous call
//        assertArrayEquals(new int[]{2}, (int[]) ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
//                (Object) null,
//                "method1",
//                1, 2));

        // the concrete argument method should have priority in this case
        assertEquals(1, ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1",
                1, 2, 3));

        // the concrete argument method should have priority in this case
        assertEquals(2, ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1",
                5.0, 6.0, 7.0));

        // ambiguous call
//        assertArrayEquals(new int[]{1, 2, 3, 4}, (int[]) ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
//                (Object)null,
//                "method1",
//                1, 2, 3, 4));

        assertArrayEquals(new double[]{6.0, 7.0, 8.0}, (double[]) ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1",
                5.0, 6.0, 7.0, 8.0), 0.00001);
    }

    @Test
    public void invokeMethod3() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // test String to Enum conversion
        assertEquals(5, ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1",
                SomeEnum.VALUE1));

        assertEquals(5, ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1",
                "VALUE1"));

        assertEquals(6, ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1",
                SomeEnum.VALUE1, SomeEnum.VALUE2));


        assertEquals(7, ReflectionUtil.invokeMethod(ReflectionUtilTest.class,
                (Object) null,
                "method1",
                SomeEnum.VALUE1, "VALUE2"));
    }

    @Test
    public void constructNew1() throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        assertEquals(new OtherValue(), ReflectionUtil.constructNew(OtherValue.class));

        assertEquals(new OtherValue(1, 1), ReflectionUtil.constructNew(OtherValue.class,
                1, 1.0));

        assertEquals(new OtherValue(1, 2.5), ReflectionUtil.constructNew(OtherValue.class,
                1, 2.5));

        assertEquals(new OtherValue(3, 4, new SomeValue("v1"), new SomeValue("v2")), ReflectionUtil.constructNew(OtherValue.class,
                3, 4, new SomeValue("v1"), new SomeValue("v2")));

        assertEquals(new OtherValue(7, 8, new SomeValue("v1"), "str"), ReflectionUtil.constructNew(OtherValue.class,
                7, 8, new SomeValue("v1"), "str"));
    }

    @Test
    public void constructNew2() throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        //varargs in constructors
        assertEquals(new OtherValue(1, 55.55),
                ReflectionUtil.constructNew(OtherValue.class, 1, 2.5, 3.5, 6.5, 9.9));

        assertEquals(new OtherValue(3, -33.33, null, new SomeValue("v2")), ReflectionUtil.constructNew(OtherValue.class,
                3, 4, new Object(), new SomeValue("v2")));
    }

    @Test(expected = NoSuchMethodException.class)
    public void searchFail() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ReflectionUtil.invokeMethod(ReflectionUtilTest.class, (Object) null, "method88", 1, 4, 5);
    }

    @Test
    public void invocationHandlerTest() throws Throwable{
        SomeInterface obj = mock(SomeInterface.class);
        LambdaFunction lambdaFunction = mock(LambdaFunction.class);

        SomeStaticClass.obj = obj;

        doAnswer(invocation -> {
            Runnable run = invocation.getArgument(0);
            run.run();
            return null;
        }).when(obj).noArg(any(Runnable.class));

        ReflectionUtil.invokeMethod(SomeStaticClass.class, (Object) null,
                "noArg",
                lambdaFunction);

        ReflectionUtil.invokeMethod(SomeStaticClass.class, (Object) null,
                "noArg",
                1, lambdaFunction);

        ReflectionUtil.invokeMethod(SomeStaticClass.class, (Object) null,
                "noArg",
                1, lambdaFunction, "a");

        ReflectionUtil.invokeMethod(SomeStaticClass.class, (Object) null,
                "noArg",
                "a", lambdaFunction, 1);

        ReflectionUtil.invokeMethod(SomeStaticClass.class, (Object) null,
                "noArg",
                "a", lambdaFunction, "a");

        ReflectionUtil.invokeMethod(SomeStaticClass.class, (Object) null,
                "noArg",
                lambdaFunction, "a");

        ReflectionUtil.invokeMethod(SomeStaticClass.class, (Object) null,
                "noArg",
                lambdaFunction, "1");

        verify(obj, times(7)).noArg(any(Runnable.class));
        verify(lambdaFunction, times(7)).invoke(any(), any(), any());
    }

    @Test
    public void invocationHandlerTest2() throws Throwable{
        SomeInterface obj = mock(SomeInterface.class);
        LambdaFunction lambdaFunction = mock(LambdaFunction.class);

        SomeStaticClass.obj = obj;

        doAnswer(invocation -> {
            Consumer run = invocation.getArgument(0);
            run.accept("abc");
            return null;
        }).when(obj).oneArg(any(Consumer.class));

        ReflectionUtil.invokeMethod(SomeStaticClass.class, (Object) null,
                "oneArg",
                lambdaFunction);

        verify(obj).oneArg(any(Consumer.class));
        verify(lambdaFunction).invoke(any(), any(), eq(new Object[]{"abc"}));
    }

    @Test
    public void invocationHandlerTest3() throws Throwable{
        SomeInterface obj = mock(SomeInterface.class);
        LambdaFunction lambdaFunction = mock(LambdaFunction.class);

        SomeStaticClass.obj = obj;

        doAnswer(invocation -> {
            BiConsumer run = invocation.getArgument(0);
            run.accept(false, 44.5);
            return null;
        }).when(obj).twoArg(any(BiConsumer.class));

        ReflectionUtil.invokeMethod(SomeStaticClass.class, (Object) null,
                "twoArg",
                lambdaFunction);

        verify(obj).twoArg(any(BiConsumer.class));
        verify(lambdaFunction).invoke(any(), any(), eq(new Object[]{false, 44.5}));
    }

    public static int method1(int a, int b, int c) {
        return 1;
    }

    public static int method1(double a, double b, double c) {
        return 2;
    }

    public static int method1(SomeValue a, SomeValue b, final SomeValue c) {
        return 3;
    }

    public static int method1(Object a, Object b, Object c) {
        return 4;
    }

    public static int method1(SomeEnum a) {
        return 5;
    }

    public static int method1(SomeEnum a, SomeEnum b) {
        return 6;
    }

    public static int method1(SomeEnum a, String b) {
        return 7;
    }

    public static int[] method1(int... a) {
        return a;
    }

    public static int[] method1(int a, int... b) {
        return b;
    }

    public static double[] method1(double a, double... b) {
        return b;
    }

    public static SomeValue[] method1(SomeValue a, SomeValue b, SomeValue... c) {
        return c;
    }

    public enum SomeEnum {
        VALUE1, VALUE2
    }

    public static class SomeValue {
        private final String value;

        public SomeValue(String value) {
            assert value != null;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SomeValue someValue = (SomeValue) o;
            return value.equals(someValue.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    public static class OtherValue {
        int a = -1;
        double b = -1.0;
        SomeValue c = null;
        Object d = null;

        public OtherValue() {
        }

        public OtherValue(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public OtherValue(int a, double b) {
            this.a = a;
            this.b = b;
        }

        public OtherValue(int a, double... b) {
            this.a = a;
            this.b = 55.55;
        }

        public OtherValue(int a, double b, SomeValue c, SomeValue d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        public OtherValue(int a, double b, Object... c) {
            this.a = a;
            this.b = -33.33;
            this.c = null;
            this.d = c[c.length - 1];
        }

        public OtherValue(int a, double b, SomeValue c, Object d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OtherValue that = (OtherValue) o;
            return a == that.a &&
                    Double.compare(that.b, b) == 0 &&
                    Objects.equals(c, that.c) &&
                    Objects.equals(d, that.d);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b, c, d);
        }
    }

    private interface SomeInterface{
        void noArg(Runnable run);

        void oneArg(Consumer<Object> run);

        void twoArg(BiConsumer<Object, Object> run);
    }

    private static class SomeStaticClass{
        static SomeInterface obj;

        public static void noArg(Runnable run){
            obj.noArg(run);
        }

        public static void noArg(int b, Runnable run){
            obj.noArg(run);
        }

        public static void noArg(int b, Runnable run, String a){
            obj.noArg(run);
        }

        public static void noArg(String c, Runnable run, int a){
            obj.noArg(run);
        }

        public static void noArg(String c, Runnable run, String a){
            obj.noArg(run);
        }

        public static void noArg(Runnable run, String a){
            obj.noArg(run);
        }

        public static void noArg(Runnable run, int a){
            obj.noArg(run);
        }

        public static void oneArg(Consumer<Object> run){
            obj.oneArg(run);
        }

        public static void twoArg(BiConsumer<Object, Object> run){
            obj.twoArg(run);
        }
    }
}