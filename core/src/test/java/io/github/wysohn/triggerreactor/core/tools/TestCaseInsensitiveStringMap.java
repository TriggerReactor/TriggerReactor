package io.github.wysohn.triggerreactor.core.tools;

import org.junit.Test;

import io.github.wysohn.triggerreactor.tools.CaseInsensitiveStringMap;

import static org.junit.Assert.assertEquals;

public class TestCaseInsensitiveStringMap {
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void test() {
		CaseInsensitiveStringMap<Integer> map = new CaseInsensitiveStringMap<>();
		
		assertEquals(map.put("merp", 3), null);
		assertEquals(map.put("MERP", 4), new Integer(3));
		assertEquals(map.containsKey("MeRp"), true);
		assertEquals(map.containsKey("derp"), false);
		assertEquals(map.containsKey(42), false);
		assertEquals(map.get(42), null);
		assertEquals(map.get("merp"), new Integer(4));
		assertEquals(map.get("MERP"), new Integer(4));
	}
}
