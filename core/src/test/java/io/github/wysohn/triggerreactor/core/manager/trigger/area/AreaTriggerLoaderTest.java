package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AreaTriggerLoaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    TriggerReactorCore core;
    AreaTriggerLoader loader;

    @Before
    public void setUp() throws Exception {
        core = mock(TriggerReactorCore.class, RETURNS_DEEP_STUBS);
        Field instanceField = TriggerReactorCore.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, core);

        when(core.getExecutorManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getPlaceholderManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getVariableManager().getGlobalVariableAdapter()).thenReturn(new HashMap<>());
        when(core.getDataFolder()).thenReturn(folder.getRoot());

        loader = new AreaTriggerLoader(core);
    }

    @Test
    public void listTriggers() throws IOException {
        File areaTriggerFolder1 = folder.newFolder("trigger1");
        File areaTriggerFolder2 = folder.newFolder("trigger2");

        ConfigSourceFactory factory = mock(ConfigSourceFactory.class);
        IConfigSource source1 = mock(IConfigSource.class);
        IConfigSource source2 = mock(IConfigSource.class);

        when(factory.create(areaTriggerFolder1, "trigger1")).thenReturn(source1);
        when(factory.create(areaTriggerFolder2, "trigger2")).thenReturn(source2);

        TriggerInfo[] triggerInfos = loader.listTriggers(folder.getRoot(), factory);
        assertEquals(2, triggerInfos.length);
        // check if triggers in the array
        assertTrue(Arrays.stream(triggerInfos).anyMatch(info -> info.getTriggerName().equals("trigger1")));
        assertTrue(Arrays.stream(triggerInfos).anyMatch(info -> info.getTriggerName().equals("trigger2")));
    }

    @Test
    public void load() throws InvalidTrgConfigurationException, IOException {
        File areaTriggerFolder1 = folder.newFolder("trigger1");

        IConfigSource source = mock(IConfigSource.class);
        when(source.get(TriggerConfigKey.KEY_TRIGGER_AREA_SMALLEST.getKey(), String.class))
                .thenReturn(Optional.of("world@0,0,0"));
        when(source.get(TriggerConfigKey.KEY_TRIGGER_AREA_LARGEST.getKey(), String.class))
                .thenReturn(Optional.of("world@10,10,10"));

        TriggerInfo info = new AreaTriggerInfo(areaTriggerFolder1, source, "trigger1");
        AreaTrigger trigger = loader.load(info);

        assertNotNull(trigger);
        assertEquals(new SimpleLocation("world", 0, 0, 0), trigger.getArea().getSmallest());
        assertEquals(new SimpleLocation("world", 10, 10, 10), trigger.getArea().getLargest());

    }

    @Test
    public void save() throws InvalidTrgConfigurationException, IOException {
        File areaTriggerFolder1 = folder.newFolder("trigger1");

        IConfigSource source = mock(IConfigSource.class);
        when(source.get(TriggerConfigKey.KEY_TRIGGER_AREA_SMALLEST.getKey(), String.class))
                .thenReturn(Optional.of("world@0,0,0"));
        when(source.get(TriggerConfigKey.KEY_TRIGGER_AREA_LARGEST.getKey(), String.class))
                .thenReturn(Optional.of("world@10,10,10"));

        TriggerInfo info = new AreaTriggerInfo(areaTriggerFolder1, source, "trigger1");
        AreaTrigger trigger = loader.load(info);

        loader.save(trigger);

        verify(source).put(TriggerConfigKey.KEY_TRIGGER_AREA_SMALLEST.getKey(), "world@0,0,0");
        verify(source).put(TriggerConfigKey.KEY_TRIGGER_AREA_LARGEST.getKey(), "world@10,10,10");
    }
}