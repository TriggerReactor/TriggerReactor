package io.github.wysohn.triggerreactor.core.script.validation;

import io.github.wysohn.triggerreactor.core.script.validation.option.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestOptions {
    public TestOptions() {
    }

    private ValidationOptions validationOptions;

    @Before
    public void init() {
        validationOptions = new ValidationOptionsBuilder()
                .addOption(new MinimumOption(), "minimum")
                .addOption(new MaximumOption(), "maximum")
                .addOption(new NameOption(), "name")
                .addOption(new TypeOption(), "type")
                .build();
    }

    @Test
    public void testSanity() {
        assertEquals(validationOptions.forName("type").getClass(), TypeOption.class);
        assertEquals(validationOptions.forName("minimum").getClass(), MinimumOption.class);
        assertEquals(validationOptions.forName("maximum").getClass(), MaximumOption.class);
        assertEquals(validationOptions.forName("name").getClass(), NameOption.class);
        assertEquals(validationOptions.forName("kjfdsdjjfjfffff()#@##"), null);
    }

    @Test
    public void testMinimum() {
        ValidationOption min = validationOptions.forName("minimum");
        assertTrue(min.canContain(3));
        assertEquals(min.validate(0, 5), null);
        assertFalse(min.canContain("derp"));
        assertNotEquals(min.validate(0, -1), null);
    }

    @Test
    public void testMaximum() {
        ValidationOption max = validationOptions.forName("maximum");
        assertTrue(max.canContain(23));
        assertEquals(max.validate(99, 5), null);
        assertFalse(max.canContain("derp2"));
        assertNotEquals(max.validate(5, 12), null);
    }

    @Test
    public void testType() {
        ValidationOption type = validationOptions.forName("type");
        assertTrue(type.canContain("int"));
        assertTrue(type.canContain("string"));
        assertTrue(type.canContain("boolean"));
        assertFalse(type.canContain("oOoOoOof"));

        assertEquals(type.validate("int", 6), null);
        assertEquals(type.validate("int", 6.0), null);
        assertEquals(type.validate("int", 0), null);
        assertEquals(type.validate("int", -1), null);
        assertEquals(type.validate("number", 30), null);
        assertEquals(type.validate("number", 30.8), null);
        assertEquals(type.validate("number", -30), null);
        assertEquals(type.validate("string", "j"), null);
        assertNull(type.validate("boolean", true));
        assertNull(type.validate("boolean", false));


        assertNotEquals(type.validate("int", 7.1), null);
        assertNotEquals(type.validate("string", new Object()), null);
        assertNotEquals(type.validate("number", "32"), null);
        assertNotNull(type.validate("boolean", 2));
    }
}
