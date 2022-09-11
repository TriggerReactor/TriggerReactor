package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class WalkTriggerLoaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    TriggerReactorCore core;

    WalkTriggerLoader loader;

    @Before
    public void setUp() throws Exception {
        core = mock(TriggerReactorCore.class, RETURNS_DEEP_STUBS);
        Field instanceField = TriggerReactorCore.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, core);

        when(core.getExecutorManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getPlaceholderManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getVariableManager().getGlobalVariableAdapter()).thenReturn(new HashMap<>());

        loader = new WalkTriggerLoader();
    }

    @Test
    public void load() throws Exception {
        TriggerInfo info = mock(TriggerInfo.class);
        when(info.getTriggerName()).thenReturn("test");
        when(info.getSourceCodeFile()).thenReturn(folder.newFile("test.trg"));

        WalkTrigger trigger = loader.load(info);
    }

    @Test
    public void save() throws Exception {
        TriggerInfo info = mock(TriggerInfo.class);
        WalkTrigger trigger = mock(WalkTrigger.class);

        when(trigger.getInfo()).thenReturn(info);
        when(trigger.getScript()).thenReturn("#MESSAGE \"Hello World\"");
        when(info.getTriggerName()).thenReturn("test");
        when(info.getSourceCodeFile()).thenReturn(folder.newFile("test.trg"));

        loader.save(trigger);

        assertTrue(info.getSourceCodeFile().exists());
    }
}