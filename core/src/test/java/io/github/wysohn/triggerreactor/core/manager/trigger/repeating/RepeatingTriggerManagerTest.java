package io.github.wysohn.triggerreactor.core.manager.trigger.repeating;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RepeatingTriggerManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    TriggerReactorCore core;
    RepeatingTriggerLoader loader;
    RepeatingTriggerManager manager;
    TaskSupervisor task;
    Thread thread;

    @Before
    public void init() throws IllegalAccessException, NoSuchFieldException {
        core = mock(TriggerReactorCore.class, RETURNS_DEEP_STUBS);
        Field instanceField = TriggerReactorCore.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, core);

        when(core.getExecutorManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getPlaceholderManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getVariableManager().getGlobalVariableAdapter()).thenReturn(new HashMap<>());
        when(core.getDataFolder()).thenReturn(folder.getRoot());
        doAnswer(invocation -> {
            Runnable run = invocation.getArgument(0);
            run.run();
            return null;
        }).when(core).runTask(any());

        loader = mock(RepeatingTriggerLoader.class);
        task = mock(TaskSupervisor.class);
        manager = new RepeatingTriggerManager(core, loader, task);
        thread = mock(Thread.class);

        when(task.newThread(any(), anyString(), anyInt())).thenReturn(thread);
    }

    @Test
    public void reload() throws InvalidTrgConfigurationException {
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        RepeatingTrigger mockTrigger = mock(RepeatingTrigger.class);

        when(mockInfo.getTriggerName()).thenReturn("test");
        when(loader.listTriggers(any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockTrigger.getInfo()).thenReturn(mockInfo);
        when(mockTrigger.isAutoStart()).thenReturn(true);

        manager.reload();

        assertNotNull(manager.get("test"));
        verify(task).newThread(mockTrigger, "RepeatingTrigger-test", Thread.MIN_PRIORITY + 1);
    }

    @Test
    public void createTrigger() throws IOException, AbstractTriggerManager.TriggerInitFailedException {
        assertTrue(manager.createTrigger("test", "#MESSAGE \"test\""));
        assertNotNull(manager.get("test"));

        assertNotNull(manager.remove("test"));
        assertNull(manager.get("test"));
    }

    @Test
    public void startTrigger() throws AbstractTriggerManager.TriggerInitFailedException, IOException {
        assertTrue(manager.createTrigger("test", "#MESSAGE \"test\""));
        assertFalse(manager.isRunning("test"));

        assertTrue(manager.startTrigger("test"));
        assertTrue(manager.isRunning("test"));

        verify(thread).start();
    }

    @Test
    public void stopTrigger() throws AbstractTriggerManager.TriggerInitFailedException, IOException {
        assertTrue(manager.createTrigger("test", "#MESSAGE \"test\""));

        assertTrue(manager.startTrigger("test"));
        assertTrue(manager.isRunning("test"));

        assertTrue(manager.stopTrigger("test"));
        assertFalse(manager.isRunning("test"));

        verify(thread).interrupt();
    }
}