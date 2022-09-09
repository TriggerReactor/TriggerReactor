package io.github.wysohn.triggerreactor.core.manager.trigger.custom;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.IEventRegistry;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CustomTriggerLoaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    TriggerReactorCore core;
    IEventRegistry registry;
    CustomTriggerLoader loader;

    @Before
    public void init() throws IllegalAccessException, NoSuchFieldException {
        core = mock(TriggerReactorCore.class, RETURNS_DEEP_STUBS);
        Field instanceField = TriggerReactorCore.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, core);

        when(core.getExecutorManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getPlaceholderManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getVariableManager().getGlobalVariableAdapter()).thenReturn(new HashMap<>());

        registry = mock(IEventRegistry.class);

        loader = new CustomTriggerLoader(registry);
    }

    @Test
    public void load() throws InvalidTrgConfigurationException, IOException {
        TriggerInfo info = mock(TriggerInfo.class);

        when(registry.eventExist(anyString())).thenReturn(true);
        when(info.get(TriggerConfigKey.KEY_TRIGGER_CUSTOM_EVENT, String.class)).thenReturn(Optional.of("some.event"));
        when(info.getSourceCodeFile()).thenReturn(folder.newFile());

        CustomTrigger trigger = loader.load(info);

        verify(info).get(TriggerConfigKey.KEY_TRIGGER_CUSTOM_EVENT, String.class);
        assertEquals(trigger.getEventName(), "some.event");
    }

    @Test
    public void save() throws IOException {
        CustomTrigger trigger = mock(CustomTrigger.class);
        TriggerInfo info = mock(TriggerInfo.class);

        when(trigger.getInfo()).thenReturn(info);
        when(trigger.getEventName()).thenReturn("some.event");
        when(trigger.getScript()).thenReturn("some.script");
        when(info.getSourceCodeFile()).thenReturn(folder.newFile());

        loader.save(trigger);

        verify(info).put(TriggerConfigKey.KEY_TRIGGER_CUSTOM_EVENT, "some.event");
    }
}