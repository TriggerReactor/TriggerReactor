package io.github.wysohn.triggerreactor.core.manager.trigger.named;

import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class NamedTriggerInfoTest {

    @Test
    public void toConventionalName() {
        File folder = Paths.get("abc", "q").toFile();
        File file = Paths.get("abc", "q", "e", "f", "w").toFile();

        assertEquals("e:f:w", NamedTriggerInfo.toConventionalName(folder, file));
    }
}