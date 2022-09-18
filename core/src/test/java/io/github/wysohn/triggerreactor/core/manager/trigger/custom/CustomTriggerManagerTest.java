package io.github.wysohn.triggerreactor.core.manager.trigger.custom;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.IEventRegistry;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class CustomTriggerManagerTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    TriggerReactorCore core;

    CustomTriggerLoader loader;
    IEventRegistry registry;
    CustomTriggerManager manager;

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

        loader = mock(CustomTriggerLoader.class);
        registry = mock(IEventRegistry.class);
        manager = new CustomTriggerManager(core, registry, loader);
    }

    @Test
    public void reload() throws InvalidTrgConfigurationException, ClassNotFoundException {
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        CustomTrigger mockTrigger = mock(CustomTrigger.class);

        when(mockInfo.getTriggerName()).thenReturn("test");
        when(mockInfo.get(TriggerConfigKey.KEY_TRIGGER_CUSTOM_EVENT, String.class))
                .thenReturn(Optional.of(DummyEvent.class.getName()));
        when(loader.listTriggers(any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockTrigger.getInfo()).thenReturn(mockInfo);
        doReturn(DummyEvent.class).when(mockTrigger).getEvent();

        manager.reload();

        verify(registry).registerEvent(core, DummyEvent.class, mockTrigger);
    }

    @Test
    public void createCustomTrigger() throws AbstractTriggerManager.TriggerInitFailedException, ClassNotFoundException {
        String eventName = DummyEvent.class.getName();
        String name = "test";
        String script = "#MESSAGE \"hello world\"";

        doReturn(DummyEvent.class).when(registry).getEvent(eventName);

        manager.createCustomTrigger(eventName, name, script);

        assertNotNull(manager.get(name));
        verify(registry).registerEvent(eq(core), eq(DummyEvent.class), any());
    }

    @Test
    public void remove() throws ClassNotFoundException, InvalidTrgConfigurationException {
        reload();

        manager.remove("test");

        verify(registry).unregisterEvent(eq(core), any());
        assertNull(manager.get("test"));
    }

    public static class DummyEvent{

    }
}