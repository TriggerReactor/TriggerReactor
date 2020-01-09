package io.github.wysohn.triggerreactor.core.manager.config;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigManagerTest {
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
			"  \"object\": {\r\n" + 
			"    \"a\": \"b\",\r\n" + 
			"    \"c\": \"d\",\r\n" + 
			"    \"e\": \"f\"\r\n" + 
			"  },\r\n" + 
			"  \"string\": \"Hello World\"\r\n" + 
			"}";
	
	private TriggerReactor mockMain;
	private File mockFile;
	private FileWriter mockWriter;
	private ConfigManager manager;
	
	@Before
	public void init() {
		mockMain = mock(TriggerReactor.class);
		mockFile = mock(File.class);
		mockWriter = mock(FileWriter.class);
		
		manager = new ConfigManager(mockMain, mockFile, 
				(f) -> new StringReader(jsonString), (f) -> mockWriter);
	}
	
	@Test
	public void testReload() {
		when(mockFile.exists()).thenReturn(true);
		
		manager.reload();
		
		Map<String, Object> cache = Whitebox.getInternalState(manager, "cache");
		List<Integer> list = new ArrayList<>();
		list.add(1);list.add(2);list.add(3);
		assertEquals(list, (List<Integer>) cache.get("array"));
		assertEquals(true, (boolean) cache.get("boolean"));
		assertEquals("#82b92c", (String) cache.get("color"));
		assertEquals(null, cache.get("null"));
		assertEquals(123, (int) cache.get("number"));
		
		assertTrue(cache.get("object") instanceof Map);
		Map<String, Object> obj = (Map<String, Object>) cache.get("object");
		
		assertEquals("b", (String) obj.get("a"));
		assertEquals("d", (String) obj.get("c"));
		assertEquals("f", (String) obj.get("e"));
		
		assertEquals("Hello World", (String) cache.get("string"));
	}

	@Test
	public void testSaveAll() {
		fail("Not yet implemented");
	}

	@Test
	public void testGet() {
		fail("Not yet implemented");
	}

	@Test
	public void testPut() {
		fail("Not yet implemented");
	}

	@Test
	public void testKeys() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsSection() {
		fail("Not yet implemented");
	}

}
