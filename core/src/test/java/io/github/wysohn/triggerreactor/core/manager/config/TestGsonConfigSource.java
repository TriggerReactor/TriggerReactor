package io.github.wysohn.triggerreactor.core.manager.config;

import copy.com.google.gson.Gson;
import copy.com.google.gson.GsonBuilder;
import copy.com.google.gson.reflect.TypeToken;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class TestGsonConfigSource {
    private final String jsonString = "{\r\n" +
            "  \"array\": [\r\n" +
            "    1,\r\n" +
            "    2,\r\n" +
            "    3\r\n" +
            "  ],\r\n" +
            "  \"boolean\": true,\r\n" +
            "  \"color\": \"#82b92c\",\r\n" +
            "  \"null\": null,\r\n" +
            "  \"number\": 123,\r\n" +
            "  \"fnumber\": 345.67,\r\n" +
            "  \"object\": {\r\n" +
            "    \"a\": \"b\",\r\n" +
            "    \"c\": \"d\",\r\n" +
            "    \"e\": \"f\"\r\n" +
            "  },\r\n" +
            "  \"string\": \"Hello World\"\r\n" +
            "}";

    private TriggerReactorCore mockMain;
    private File mockFile;
    private StringWriter stringWriter;
    private GsonConfigSource manager;

    @Before
    public void init() {
        mockMain = Mockito.mock(TriggerReactorCore.class);
        mockFile = Mockito.mock(File.class);
        stringWriter = new StringWriter();

        manager = new GsonConfigSource(mockFile, (f) -> new StringReader(jsonString), (f) -> stringWriter);
    }

    @Test
    public void testReload() {
        Mockito.when(mockFile.exists()).thenReturn(true);

        manager.reload();

        Map<String, Object> cache = Whitebox.getInternalState(manager, "cache");
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        Assert.assertEquals(list, cache.get("array"));
        Assert.assertEquals(true, cache.get("boolean"));
        Assert.assertEquals("#82b92c", cache.get("color"));
        Assert.assertEquals(null, cache.get("null"));
        Assert.assertEquals(123, cache.get("number"));
        Assert.assertEquals(345.67, cache.get("fnumber"));


        Assert.assertTrue(cache.get("object") instanceof Map);
        Map<String, Object> obj = (Map<String, Object>) cache.get("object");

        Assert.assertEquals("b", obj.get("a"));
        Assert.assertEquals("d", obj.get("c"));
        Assert.assertEquals("f", obj.get("e"));

        Assert.assertEquals("Hello World", cache.get("string"));
    }

    @Test
    public void testSaveAll() {
        Mockito.when(mockFile.exists()).thenReturn(true);

        Map<String, Object> cache = Whitebox.getInternalState(manager, "cache");
        cache.put("array2", new int[]{4, 5, 6});
        cache.put("boolean2", false);
        cache.put("color2", "#123b9cc");
        cache.put("null", null);
        cache.put("number2", 456);
        cache.put("fnumber2", 789.12);

        Map<String, Object> obj = new HashMap<>();
        obj.put("i", "j");
        obj.put("k", "l");
        obj.put("m", "n");
        cache.put("object", obj);

        cache.put("string2", "World Hello");

        manager.saveAll();

        String out = stringWriter.toString();
        GsonBuilder builder = org.powermock.reflect.Whitebox.getInternalState(GsonConfigSource.class, "builder");
        Gson gson = builder.create();
        Map<String, Object> deser = gson.fromJson(out, new TypeToken<Map<String, Object>>() {
        }.getType());

        List<Integer> list = new ArrayList<>();
        list.add(4);
        list.add(5);
        list.add(6);
        Assert.assertEquals(list, deser.get("array2"));
        Assert.assertEquals(false, deser.get("boolean2"));
        Assert.assertEquals("#123b9cc", deser.get("color2"));
        Assert.assertEquals(null, deser.get("null"));
        Assert.assertEquals(456, deser.get("number2"));
        Assert.assertEquals(789.12, deser.get("fnumber2"));

        Assert.assertTrue(deser.get("object") instanceof Map);
        Map<String, Object> obj2 = (Map<String, Object>) deser.get("object");

        Assert.assertEquals("j", obj2.get("i"));
        Assert.assertEquals("l", obj2.get("k"));
        Assert.assertEquals("n", obj2.get("m"));

        Assert.assertEquals("World Hello", deser.get("string2"));
    }

    @Test
    public void testGet() {
        Mockito.when(mockFile.exists()).thenReturn(true);
        manager.reload();

        Assert.assertTrue(manager.get("object").orElse(null) instanceof Map);
        Assert.assertEquals("b", manager.get("object.a").orElse(null));
        Assert.assertEquals("d", manager.get("object.c").orElse(null));
        Assert.assertEquals("f", manager.get("object.e").orElse(null));
    }

    @Test
    public void testPut() {
        Mockito.when(mockFile.exists()).thenReturn(true);

        manager.put("some.data.value", "abc");
        manager.put("some.data.value2", 123);
        manager.put("some.data.value3", false);

        Assert.assertEquals("abc", manager.get("some.data.value").orElse(null));
        Assert.assertEquals(123, manager.get("some.data.value2").orElse(null));
        Assert.assertEquals(false, manager.get("some.data.value3").orElse(null));

        manager.put("some.data", 556);

        Assert.assertEquals(556, manager.get("some.data").orElse(null));
        Assert.assertNull(manager.get("some.data.value").orElse(null));
        Assert.assertNull(manager.get("some.data.value2").orElse(null));
        Assert.assertNull(manager.get("some.data.value3").orElse(null));
    }

    @Test
    public void testKeys() {
        Mockito.when(mockFile.exists()).thenReturn(true);

        manager.reload();

        Set<String> expected = new HashSet<>();
        expected.add("array");
        expected.add("boolean");
        expected.add("color");
        expected.add("null");
        expected.add("number");
        expected.add("fnumber");
        expected.add("object");
        expected.add("string");
        Assert.assertEquals(expected, manager.keys());
    }

    @Test
    public void testIsSection() {
        Mockito.when(mockFile.exists()).thenReturn(true);

        manager.reload();

        Assert.assertFalse(manager.isSection("array"));
        Assert.assertFalse(manager.isSection("boolean"));
        Assert.assertFalse(manager.isSection("color"));
        Assert.assertFalse(manager.isSection("null"));
        Assert.assertFalse(manager.isSection("number"));
        Assert.assertFalse(manager.isSection("fnumber"));
        Assert.assertTrue(manager.isSection("object"));
        Assert.assertFalse(manager.isSection("string"));

    }
}
