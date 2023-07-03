package io.github.wysohn.triggerreactor.core.config.source;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class ConfigSourceFactoryTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    ConfigSourceFactory configSourceFactory = new ConfigSourceFactory();

    @Test
    public void create() throws IOException {
        // arrange
        File configFile = folder.newFile("test.json");
        Files.write(configFile.toPath(), ("{" +
                "\"first\": 123," +
                "\"second\": \"abc\"," +
                "\"third\": true" +
                "}").getBytes());

        // act
        IConfigSource configSource = configSourceFactory.create(folder.getRoot(), "test");

        // assert
        assertEquals(123, (int) configSource.get("first", Integer.class).orElseThrow(RuntimeException::new));
        assertEquals("abc", configSource.get("second", String.class).orElseThrow(RuntimeException::new));
        assertEquals(true, configSource.get("third", Boolean.class).orElseThrow(RuntimeException::new));
    }
}