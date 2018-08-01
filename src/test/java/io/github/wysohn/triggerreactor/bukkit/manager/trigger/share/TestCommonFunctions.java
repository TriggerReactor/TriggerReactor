package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share;

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Test;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;

public class TestCommonFunctions {
    int trials = 100;

    @Test
    public void testRandoms() throws Exception {
        TriggerReactor mockPlugin = mock(TriggerReactor.class);
        CommonFunctions fn = new CommonFunctions(mockPlugin);

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
