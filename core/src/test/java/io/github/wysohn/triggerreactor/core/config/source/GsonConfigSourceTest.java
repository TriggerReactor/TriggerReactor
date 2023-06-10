package io.github.wysohn.triggerreactor.core.config.source;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class GsonConfigSourceTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    File configFile;
    GsonConfigSource gsonConfigSource;

    @Before
    public void setUp() throws Exception {
        configFile = folder.newFile();
        gsonConfigSource = new GsonConfigSource(configFile);
    }

    @Test
    public void fileExists() throws IOException {
        // arrange
        Files.write(configFile.toPath(), "test".getBytes());

        // act
        boolean result = gsonConfigSource.fileExists();

        // assert
        assertTrue(result);
    }

    @Test
    public void reload() throws IOException {
        // arrange
        Files.write(configFile.toPath(), "{\"test\": \"val\"}".getBytes());

        // act
        gsonConfigSource.reload();

        // assert
        assertTrue(gsonConfigSource.has("test"));
        assertTrue(gsonConfigSource.get("test").map(o -> o.equals("val")).orElse(false));
    }

    @Test
    public void saveAll() {
    }

    @Test
    public void get() {
    }

    @Test
    public void testGet() {
    }

    @Test
    public void put() {
    }

    @Test
    public void has() {
    }

    @Test
    public void keys() {
    }

    @Test
    public void isSection() {
    }

    @Test
    public void shutdown() {
        // arrange
        int max = 1000;

        Map<String, Object> values1 = new HashMap<>();

        for (int i = 0; i < max; i++) {
            values1.put("key" + i, "val" + i);
        }

        // act
        values1.forEach(gsonConfigSource::put);

        gsonConfigSource.shutdown();

        // assert
        for (int i = 0; i < max; i++) {
            int finalI = i;
            assertTrue(gsonConfigSource.has("key" + finalI));
            assertTrue(gsonConfigSource.get("key" + finalI).map(o -> o.equals("val" + finalI)).orElse(false));
        }
    }

    @Test
    public void delete() {
    }
}