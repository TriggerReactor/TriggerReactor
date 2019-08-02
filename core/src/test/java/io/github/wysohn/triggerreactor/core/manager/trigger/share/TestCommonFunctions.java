package io.github.wysohn.triggerreactor.core.manager.trigger.share;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * This test class require static method data().
 * See description of Parameterized.class for more details
 */
@RunWith(Parameterized.class)
public class TestCommonFunctions<FN extends CommonFunctions> {
	
    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { {null} });
    }

    protected final FN fn;
    private int trials = 100;

    public TestCommonFunctions(FN fn){
        this.fn = fn;
    }

    @Before
    public void init(){

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
    public void testDataStructures()
    {
    	Set<Object> set = fn.set();
    	List<Object> list = fn.list();
    	Map<Object, Object> map = fn.map();
    	assertTrue(map.size() == 0 && list.size() == 0 && set.size() == 0);
    }
    
    @Test
    public void testStaticAccess() throws Exception
    {
    	ExampleClass.reset();
    	
    	String example = "io.github.wysohn.triggerreactor.core.manager.trigger.share.ExampleClass";
    	assertEquals(42, fn.staticGetFieldValue(example, "foo"));
    	assertEquals(false, fn.staticGetFieldValue(example, "bar"));
    	
    	fn.staticSetFieldValue(example, "bar", true);
    	fn.staticSetFieldValue(example, "foo", -1);
    	
    	assertEquals(-1, ExampleClass.foo);
    	assertEquals(true, ExampleClass.bar);
    	
    	assertEquals(44, fn.staticMethod(example, "baz", 22));
    	assertEquals("TriggerReactor", fn.staticMethod(example, "add", "Trigger", "Reactor"));
    	
    }
}
