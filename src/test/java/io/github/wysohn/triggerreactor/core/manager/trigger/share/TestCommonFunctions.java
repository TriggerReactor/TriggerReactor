package io.github.wysohn.triggerreactor.core.manager.trigger.share;

import org.junit.Assert;
import org.junit.Test;

public class TestCommonFunctions {
    int trials = 100;

    @Test
    public void testRandoms() throws Exception {
        CommonFunctions fn = new CommonFunctions() {};

        for(int i = 0; i < trials; i++) {
            double value = fn.random(1.0);
            Assert.assertTrue(0.0 <= value && value < 1.0);

            value = fn.random(0.0, 1.0);
            Assert.assertTrue(0.0 <= value && value < 1.0);

            value = fn.random(1.0F);
            Assert.assertTrue(0.0 <= value && value < 1.0);

            value = fn.random(0.0F, 1.0F);
            Assert.assertTrue(0.0 <= value && value < 1.0);
        }

        for(int i = 0; i < trials; i++) {
            long value = fn.random(10L);
            Assert.assertTrue(0L <= value && value < 10L);

            value = fn.random(0L, 10L);
            Assert.assertTrue(0L <= value && value < 10L);

            value = fn.random(10);
            Assert.assertTrue(0 <= value && value < 10);

            value = fn.random(0, 10);
            Assert.assertTrue(0 <= value && value < 10);
        }
    }
}
