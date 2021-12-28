package io.github.wysohn.triggerreactor.core.tools;

import io.github.wysohn.triggerreactor.tools.CaseInsensitiveStringMap;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCaseInsensitiveStringMap {
    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test() {
        CaseInsensitiveStringMap<Integer> map = new CaseInsensitiveStringMap<>();

        assertNull(map.put("merp", 3));
        assertEquals(map.put("MERP", 4), Integer.valueOf(3));
        assertTrue(map.containsKey("MeRp"));
        assertFalse(map.containsKey("derp"));
        assertFalse(map.containsKey(42));
        assertNull(map.get(42));
        assertEquals(map.get("merp"), Integer.valueOf(4));
        assertEquals(map.get("MERP"), Integer.valueOf(4));
    }
}
