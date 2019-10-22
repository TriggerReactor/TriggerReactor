package io.github.wysohn.triggerreactor.core.script.validation;

import org.junit.Test;
import static org.junit.Assert.*;

import io.github.wysohn.triggerreactor.core.script.validation.option.MinimumOption;
import io.github.wysohn.triggerreactor.core.script.validation.option.TypeOption;
import io.github.wysohn.triggerreactor.core.script.validation.option.ValidationOption;

public class TestOptions {
	public TestOptions() {}
	
	@Test
	public void testSanity() {
		assertEquals(ValidationOption.forName("type").getClass(), TypeOption.class);
		assertEquals(ValidationOption.forName("minimum").getClass(), MinimumOption.class);
		assertEquals(ValidationOption.forName("kjfdsdjjfjfffff()#@##"), null);
	}
	
	@Test
	public void testMinimum() {
		ValidationOption min = ValidationOption.forName("minimum");
		assertTrue(min.canContain(3));
		assertEquals(min.validate(0, 5), null);
		assertFalse(min.canContain("derp"));
		assertNotEquals(min.validate(0, -1), null);
	}
	
	@Test
	public void testType() {
		ValidationOption type = ValidationOption.forName("type");
		assertTrue(type.canContain("int"));
		assertTrue(type.canContain("string"));
		assertFalse(type.canContain("oOoOoOof"));
		
		assertEquals(type.validate("int", 6), null);
		assertEquals(type.validate("int", 6.0), null);
		assertEquals(type.validate("int", 0), null);
		assertEquals(type.validate("int", -1), null);
		assertEquals(type.validate("number", 30), null);
		assertEquals(type.validate("number", 30.8), null);
		assertEquals(type.validate("number", -30), null);
		assertEquals(type.validate("string", "j"), null);
		
		assertNotEquals(type.validate("int", 7.1), null);
		assertNotEquals(type.validate("string", new Object()), null);
		assertNotEquals(type.validate("number", "32"), null);
	}
}
