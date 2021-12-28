package io.github.wysohn.triggerreactor.core.manager.trigger.share;

import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import static org.junit.Assert.*;

/**
 * This test class require static method data().
 * See description of Parameterized.class for more details
 */
@RunWith(Parameterized.class)
public class TestCommonFunctions<FN extends CommonFunctions> {
    protected final FN fn;
    private final int trials = 100;

    public TestCommonFunctions(FN fn) {
        this.fn = fn;
    }

    @Test
    public void arrayOf() {
        Object[] arr = fn.arrayOf("a", 1, false);
        assertArrayEquals(new Object[]{"a", 1, false}, arr);
    }

    @Test
    public void listOf() {
        List<Object> list = fn.listOf("b", 2, true);
        List<Object> expect = new ArrayList<>();
        expect.add("b");
        expect.add(2);
        expect.add(true);
        assertEquals(expect, list);
    }

    @Test
    public void setWithCollection() {
        List<Object> list = fn.listOf("e", "f", "g");
        Set<String> expect = new HashSet<>();
        expect.add("e");
        expect.add("f");
        expect.add("g");
        assertEquals(expect, fn.set(list));
    }

    @Test
    public void testConstructor() throws Exception {
        //thx for the test cases
        ExampleClass e = (ExampleClass) fn.newInstance(example, 1);
        assertEquals(0, e.marker);

        e = (ExampleClass) fn.newInstance(example, 1.1);
        assertEquals(2, e.marker);

        File file = (File) fn.newInstance("java.io.File", "test.txt");
        FileWriter writer = (FileWriter) fn.newInstance("java.io.FileWriter", file);
        FileReader reader = (FileReader) fn.newInstance("java.io.FileReader", file);
    }

    @Test
    public void testDataStructures() {
        Set<Object> set = fn.set();
        List<Object> list = fn.list();
        Map<Object, Object> map = fn.map();
        assertTrue(map.size() == 0 && list.size() == 0 && set.size() == 0);
    }

    @Test
    public void testRandoms() throws Exception {
        for (int i = 0; i < trials; i++) {
            double value = fn.random(1.0);
            assertTrue(0.0 <= value && value < 1.0);

            value = fn.random(0.0, 1.0);
            assertTrue(0.0 <= value && value < 1.0);

            value = fn.random(1.0F);
            assertTrue(0.0 <= value && value < 1.0);

            value = fn.random(0.0F, 1.0F);
            assertTrue(0.0 <= value && value < 1.0);
        }

        for (int i = 0; i < trials; i++) {
            long value = fn.random(10L);
            assertTrue(0L <= value && value < 10L);

            value = fn.random(0L, 10L);
            assertTrue(0L <= value && value < 10L);

            int value2 = fn.random(10);
            assertTrue(0 <= value2 && value2 < 10);

            value2 = fn.random(0, 10);
            assertTrue(0 <= value2 && value2 < 10);
        }
    }

    @Test
    public void testSLocation() throws Exception {
        assertEquals(fn.slocation("merp", 1, 2, 3), new SimpleLocation("merp", 1, 2, 3));
    }

    @Test
    public void testStaticAccess() throws Exception {
        ExampleClass.reset();

        assertEquals(42, fn.staticGetFieldValue(example, "foo"));
        assertEquals(false, fn.staticGetFieldValue(example, "bar"));

        fn.staticSetFieldValue(example, "bar", true);
        fn.staticSetFieldValue(example, "foo", -1);

        assertEquals(-1, ExampleClass.foo);
        assertEquals(true, ExampleClass.bar);

        assertEquals(44, fn.staticMethod(example, "baz", 22));
        assertEquals("TriggerReactor", fn.staticMethod(example, "add", "Trigger", "Reactor"));

    }

    private static final String example = "io.github.wysohn.triggerreactor.core.manager.trigger.share.ExampleClass";

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{{null}});
    }
}
