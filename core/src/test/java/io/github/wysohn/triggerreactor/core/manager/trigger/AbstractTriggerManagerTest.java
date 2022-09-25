package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AbstractTriggerManagerTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    TriggerReactorCore core;
    ITriggerLoader<TempTrigger> loader;

    AbstractTriggerManager<TempTrigger> manager;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        core = mock(TriggerReactorCore.class, RETURNS_DEEP_STUBS);
        Field instanceField = TriggerReactorCore.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, core);

        when(core.getExecutorManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getPlaceholderManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getVariableManager().getGlobalVariableAdapter()).thenReturn(new HashMap<>());

        loader = mock(ITriggerLoader.class);
        manager = new AbstractTriggerManager<TempTrigger>(core, folder.getRoot(), loader) {

        };
    }

    @Test
    public void reload() throws InvalidTrgConfigurationException {
        TriggerInfo info = mock(TriggerInfo.class);

        when(loader.listTriggers(any(), any())).thenReturn(new TriggerInfo[]{info});
        when(info.getTriggerName()).thenReturn("test");
        when(loader.load(info)).thenReturn(mock(TempTrigger.class));

        manager.reload();
    }

    @Test
    public void testReload() throws InvalidTrgConfigurationException, IOException {
        TriggerInfo info = mock(TriggerInfo.class);
        File file = folder.newFile("test.trg");

        when(loader.toTriggerInfo(eq(file), any())).thenReturn(info);
        when(info.getTriggerName()).thenReturn("test");
        when(loader.load(any())).thenReturn(mock(TempTrigger.class));
        when(info.hasDuplicate(any())).thenReturn(true);

        manager.reload("test");

        verify(info, times(TriggerConfigKey.values().length)).hasDuplicate(any());
    }

    public static class TempTrigger extends Trigger{
        public TempTrigger(TriggerInfo info, String script) {
            super(info, script);
        }

        @Override
        public Trigger clone() {
            return null;
        }
    }
}