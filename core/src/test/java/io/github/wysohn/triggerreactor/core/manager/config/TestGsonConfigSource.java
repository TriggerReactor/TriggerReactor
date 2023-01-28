/*
 * Copyright (C) 2023. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.manager.config;

import io.github.wysohn.gsoncopy.Gson;
import io.github.wysohn.gsoncopy.GsonBuilder;
import io.github.wysohn.gsoncopy.internal.LinkedTreeMap;
import io.github.wysohn.gsoncopy.reflect.TypeToken;
import io.github.wysohn.triggerreactor.core.config.serialize.Serializer;
import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class TestGsonConfigSource {
    private final String jsonString = "{\n" +
            "   \"string\":\"teststring\",\n" +
            "   \"number\":8,\n" +
            "   \"number2\":9.9,\n" +
            "   \"boolean\":true,\n" +
            "   \"list\":[\n" +
            "      \"a\",\n" +
            "      \"b\",\n" +
            "      \"c\"\n" +
            "   ],\n" +
            "   \"object\":{\n" +
            "      \"value\":\"abc\"\n" +
            "   },\n" +
            "   \"uuid\":{\n" +
            "      \"" + Serializer.SER_KEY + "\":\"java.util.UUID\",\n" +
            "      \"" + Serializer.SER_VALUE + "\":\"968cee8d-ec72-4a2f-a3bc-09a521a06f89\"\n" +
            "   },\n" +
            "   \"hashset\":{\n" +
            "      \"" + Serializer.SER_KEY + "\":\"java.util.HashSet\",\n" +
            "      \"" + Serializer.SER_VALUE + "\":[\n" +
            "         \"e\",\n" +
            "         \"f\",\n" +
            "         \"g\"\n" +
            "      ]\n" +
            "   },\n" +
            "   \"myobj1\":{\n" +
            "      \"" + Serializer.SER_KEY + "\":\"" + CustomObject.class.getName() + "\",\n" +
            "      \"" + Serializer.SER_VALUE + "\":{\n" +
            "         \"s\":\"some string\",\n" +
            "         \"i\":22,\n" +
            "         \"d\":88.8,\n" +
            "         \"b\":false\n" +
            "      }\n" +
            "   }\n" +
            "}";

    private File mockFile;
    private StringWriter stringWriter;
    private GsonConfigSource manager;

    @Before
    public void init() {
        mockFile = Mockito.mock(File.class);
        stringWriter = new StringWriter();

        manager = new GsonConfigSource(mockFile,
                (f) -> new StringReader(jsonString),
                (f) -> stringWriter);

        Mockito.when(mockFile.exists()).thenReturn(true);
        Mockito.when(mockFile.length()).thenReturn(Long.MAX_VALUE);
    }

    @Test
    public void testReload() throws Exception{
        manager.reload();

        Field field = manager.getClass().getDeclaredField("cache");
        field.setAccessible(true);
        Map<String, Object> cache = (Map<String, Object>) field.get(manager);

        assertEquals("teststring", cache.get("string"));
        assertEquals(8, cache.get("number"));
        assertEquals(9.9, cache.get("number2"));
        assertEquals(true, cache.get("boolean"));
        assertEquals(new ArrayList<String>() {{
            add("a");
            add("b");
            add("c");
        }}, cache.get("list"));
        assertEquals(new LinkedHashMap<String, Object>() {{
            put("value", "abc");
        }}, cache.get("object"));
        assertEquals(UUID.fromString("968cee8d-ec72-4a2f-a3bc-09a521a06f89"), cache.get("uuid"));
        assertEquals(new HashSet<String>() {{
            add("e");
            add("f");
            add("g");
        }}, cache.get("hashset"));

        CustomObject myobj1 = new CustomObject();
        myobj1.s = "some string";
        myobj1.i = 22;
        myobj1.d = 88.8;
        myobj1.b = false;
        assertEquals(myobj1, cache.get("myobj1"));
    }

    @Test
    public void testSaveAll() throws Exception {
        Field field = manager.getClass().getDeclaredField("cache");
        field.setAccessible(true);
        Map<String, Object> cache = (Map<String, Object>) field.get(manager);

        cache.put("string2", "teststring2");
        cache.put("number2", 123);
        cache.put("number2_2", 123.45);
        cache.put("boolean2", false);
        cache.put("null", null);

        List<String> list2 = new ArrayList<String>();
        list2.add("o");
        list2.add("p");
        list2.add("q");
        cache.put("list2", list2);

        Map<String, Object> object2 = new HashMap<String, Object>();
        object2.put("value2", "ccdd");
        cache.put("object2", object2);

        cache.put("uuid2", UUID.fromString("12b6df56-1fc0-4d8a-bed4-9469e7798dea"));

        Set<String> hashset2 = new HashSet<String>();
        hashset2.add("j");
        hashset2.add("k");
        hashset2.add("l");
        cache.put("hashset2", hashset2);

        CustomObject myobj2 = new CustomObject();
        myobj2.s = "some string2";
        myobj2.i = 33;
        myobj2.d = 99.9;
        myobj2.b = false;
        cache.put("myobj2", myobj2);

        manager.saveAll();

        String out = stringWriter.toString();

        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        Map<String, Object> deser = gson.fromJson(out, new TypeToken<Map<String, Object>>() {
        }.getType());

        assertEquals("teststring2", deser.get("string2"));
        assertEquals(123, deser.get("number2"));
        assertEquals(123.45, deser.get("number2_2"));
        assertEquals(false, deser.get("boolean2"));
        assertEquals(new ArrayList<String>() {{
            add("o");
            add("p");
            add("q");
        }}, deser.get("list2"));
        assertEquals(new LinkedHashMap<String, Object>() {{
            put("value2", "ccdd");
        }}, deser.get("object2"));

        Map<String, Object> uuid2 = new LinkedTreeMap<>();
        uuid2.put(Serializer.SER_KEY, UUID.class.getName());
        uuid2.put(Serializer.SER_VALUE, "12b6df56-1fc0-4d8a-bed4-9469e7798dea");
        assertEquals(uuid2, deser.get("uuid2"));

        List<String> hashset22 = new ArrayList<>();
        hashset22.add("j");
        hashset22.add("k");
        hashset22.add("l");
        assertEquals(hashset22, deser.get("hashset2"));

        Map<String, Object> myobj2raw = new LinkedTreeMap<>();
        myobj2raw.put("s", "some string2");
        myobj2raw.put("i", 33);
        myobj2raw.put("d", 99.9);
        myobj2raw.put("b", false);
        assertEquals(myobj2raw, deser.get("myobj2"));
    }

    public interface SomeInterface {

    }

    public static class CustomObject implements SomeInterface {
        String s;
        int i;
        double d;
        boolean b = true;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CustomObject that = (CustomObject) o;
            return i == that.i &&
                    Double.compare(that.d, d) == 0 &&
                    b == that.b &&
                    Objects.equals(s, that.s);
        }

        @Override
        public int hashCode() {
            return Objects.hash(s, i, d, b);
        }
    }
}
