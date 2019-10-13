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
		assertTrue(min.validate(0, 5));
		assertFalse(min.canContain("derp"));
		assertFalse(min.validate(0, -1));
	}
	
	@Test
	public void testType() {
		ValidationOption type = ValidationOption.forName("type");
		assertTrue(type.canContain("int"));
		assertTrue(type.canContain("string"));
		assertFalse(type.canContain("oOoOoOof"));
		
		assertTrue(type.validate("int", 6));
		assertTrue(type.validate("int", 6.0));
		assertTrue(type.validate("int", 0));
		assertTrue(type.validate("int", -1));
		assertTrue(type.validate("number", 30));
		assertTrue(type.validate("number", 30.8));
		assertTrue(type.validate("number", -30));
		assertTrue(type.validate("string", "j"));
		
		assertFalse(type.validate("int", 7.1));
		assertFalse(type.validate("string", new Object()));
		assertFalse(type.validate("number", "32"));
	}
}
