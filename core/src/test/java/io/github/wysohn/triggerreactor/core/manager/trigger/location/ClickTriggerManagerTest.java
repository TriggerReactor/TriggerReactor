package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class ClickTriggerManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    ClickTriggerManager manager;
    TriggerReactorCore core;

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

        manager = new ClickTriggerManager(core);
    }

    @Test
    public void getTriggerTypeName() {
        assertNotNull(manager.getTriggerTypeName());
    }

    @Test
    public void newTrigger() throws AbstractTriggerManager.TriggerInitFailedException {
        TriggerInfo info = mock(TriggerInfo.class);
        assertNotNull(manager.newTrigger(info, "#MESSAGE \"Hello World\""));
    }
}