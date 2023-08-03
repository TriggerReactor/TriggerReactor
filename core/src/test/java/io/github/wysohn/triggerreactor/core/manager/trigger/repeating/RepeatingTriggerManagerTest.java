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

package io.github.wysohn.triggerreactor.core.manager.trigger.repeating;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerDependencyFacade;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.module.TestFileModule;
import io.github.wysohn.triggerreactor.core.module.TestTriggerDependencyModule;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.inject.Named;
import java.io.IOException;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RepeatingTriggerManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    RepeatingTriggerLoader loader;
    RepeatingTriggerManager manager;
    TaskSupervisor task;
    Thread thread;
    Future future;
    IPluginManagement pluginManagement;
    ProcessInterrupter interrupter;
    ITriggerDependencyFacade dependencyFacade;

    @Before
    public void init() throws IllegalAccessException, NoSuchFieldException {
        loader = mock(RepeatingTriggerLoader.class);
        task = mock(TaskSupervisor.class);
        thread = mock(Thread.class);
        future = mock(Future.class);
        pluginManagement = mock(IPluginManagement.class);
        interrupter = mock(ProcessInterrupter.class);

        manager = Guice.createInjector(
                new TestFileModule(folder),
                TestTriggerDependencyModule.Builder.begin()
                        .taskSupervisor(task)
                        .pluginManagement(pluginManagement)
                        .build(),
                new FactoryModuleBuilder().build(IRepeatingTriggerFactory.class),
                new AbstractModule() {
                    @Provides
                    public ITriggerLoader<RepeatingTrigger> provideLoader() {
                        return loader;
                    }

                    @Provides
                    @Named("RepeatingTriggerManagerFolder")
                    public String provideFolder() throws IOException {
                        return "RepeatingTrigger";
                    }
                }
        ).getInstance(RepeatingTriggerManager.class);

        when(task.newThread(any(), anyString(), anyInt())).thenReturn(thread);
        when(pluginManagement.createInterrupter(any())).thenReturn(interrupter);
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
        doAnswer(invocation -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return future;
        }).when(task).runTask(any(Runnable.class));

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
        when(task.submitSync(any())).thenReturn(future);

        assertTrue(manager.createTrigger("test", "#MESSAGE \"test\""));
        assertFalse(manager.isRunning("test"));

        assertTrue(manager.startTrigger("test"));
        assertTrue(manager.isRunning("test"));

        verify(thread).start();
    }

    @Test
    public void stopTrigger() throws AbstractTriggerManager.TriggerInitFailedException, IOException {
        when(task.submitSync(any())).thenReturn(future);

        assertTrue(manager.createTrigger("test", "#MESSAGE \"test\""));

        assertTrue(manager.startTrigger("test"));
        assertTrue(manager.isRunning("test"));

        assertTrue(manager.stopTrigger("test"));
        assertFalse(manager.isRunning("test"));

        verify(thread).interrupt();
    }
}