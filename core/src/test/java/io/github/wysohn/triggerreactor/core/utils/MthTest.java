package io.github.wysohn.triggerreactor.core.utils;

import io.github.wysohn.triggerreactor.core.util.Mth;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class MthTest {

    @Test
    public void testClamp() {
        assertEquals(100, Mth.clamp(100, 0, 500));
    }

    @Test
    public void testClamp2() {
        assertEquals(500, Mth.clamp(1000, 0, 500));
    }

    @Test
    public void testClamp3() {
        assertEquals(0, Mth.clamp(-10, 0, 500));
    }

}