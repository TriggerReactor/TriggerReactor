package io.github.wysohn.triggerreactor.core.manager.trigger.share;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

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

    @Test
    public void testRandoms() throws Exception {
        for (int i = 0; i < trials; i++) {
            double value = fn.random(1.0);
            Assert.assertTrue(0.0 <= value && value < 1.0);

            value = fn.random(0.0, 1.0);
            Assert.assertTrue(0.0 <= value && value < 1.0);

            value = fn.random(1.0F);
            Assert.assertTrue(0.0 <= value && value < 1.0);

            value = fn.random(0.0F, 1.0F);
            Assert.assertTrue(0.0 <= value && value < 1.0);
        }

        for (int i = 0; i < trials; i++) {
            long value = fn.random(10L);
            Assert.assertTrue(0L <= value && value < 10L);

            value = fn.random(0L, 10L);
            Assert.assertTrue(0L <= value && value < 10L);

            int value2 = fn.random(10);
            Assert.assertTrue(0 <= value2 && value2 < 10);

            value2 = fn.random(0, 10);
            Assert.assertTrue(0 <= value2 && value2 < 10);
        }
    }
}
