package io.github.wysohn.triggerreactor.core.config.source;

import io.github.wysohn.gsoncopy.JsonElement;
import io.github.wysohn.gsoncopy.JsonParser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
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
    public void reload_RBW() throws IOException {
        // arrange
        Files.write(configFile.toPath(), "{\"test\": \"val\"}".getBytes());

        // act
        gsonConfigSource.put("test", "other");
        gsonConfigSource.reload();

        // assert
        assertTrue(gsonConfigSource.has("test"));
        assertTrue(gsonConfigSource.get("test").map(o -> o.equals("val")).orElse(false));
    }

    @Test
    public void reload_RBW_prevented() throws IOException {
        // arrange
        Files.write(configFile.toPath(), "{\"test\": \"val\"}".getBytes());

        // act
        gsonConfigSource.put("test", "other");
        gsonConfigSource.saveAll();
        gsonConfigSource.reload();

        // assert
        assertTrue(gsonConfigSource.has("test"));
        assertTrue(gsonConfigSource.get("test").map(o -> o.equals("other")).orElse(false));
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
        // arrange
        int tries = 30;
        int max = 10000;

        Map<String, Object> values1 = new HashMap<>();

        for (int i = 0; i < max; i++) {
            values1.put("key" + i, "val" + i);
        }

        // act
        long[] duration = new long[tries];
        for (int i = 0; i < tries; i++) {
            long start = System.currentTimeMillis();
            values1.forEach(gsonConfigSource::put);
            duration[i] = System.currentTimeMillis() - start;
        }

        LongSummaryStatistics summary = Arrays.stream(duration).summaryStatistics();
        System.out.println("put: " + summary);
        //        put: LongSummaryStatistics{count=30, sum=3451, min=82, average=115.033333, max=257}
        //        put: LongSummaryStatistics{count=30, sum=489, min=8, average=16.300000, max=103}
        //        new implementation is about 7 times faster

        // assert
        for (int i = 0; i < max; i++) {
            int finalI = i;
            assertTrue(gsonConfigSource.has("key" + finalI));
            assertTrue(gsonConfigSource.get("key" + finalI).map(o -> o.equals("val" + finalI)).orElse(false));
        }
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
    public void shutdown() throws IOException {
        // arrange
        int max = 100000;

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

        assertJsonEquals("{" + IntStream.range(0, max)
                        .mapToObj(i -> "\"key" + i + "\":\"val" + i + "\"")
                        .collect(Collectors.joining(",")) + "}",
                readContent(configFile.getName()));
    }

    private void assertJsonEquals(String expected, String actual) {
        JsonParser parser = new JsonParser();
        JsonElement expectedJson = parser.parse(expected);
        JsonElement actualJson = parser.parse(actual);
        assertEquals(expectedJson, actualJson);
    }

    private String readContent(String... paths) throws IOException {
        Path path = Paths.get(folder.getRoot().getAbsolutePath(), paths);
        return new String(Files.readAllBytes(path));
    }

    @Test
    public void delete() {
    }
}