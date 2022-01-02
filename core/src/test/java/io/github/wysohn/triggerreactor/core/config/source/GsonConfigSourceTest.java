package io.github.wysohn.triggerreactor.core.config.source;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class GsonConfigSourceTest {

    @Test
    public void testToString() {
        IConfigSource config = new GsonConfigSource(new File(""));
        config.put("a.b.c", 11);
        config.put("a.b.d", 22);
        config.put("b.c", "55");
        config.put("b.d", 88);
        config.put("e", false);

        assertEquals("{a: {b: ... } b: {c: 55 d: 88 } e: false }", config.toString());
    }
}