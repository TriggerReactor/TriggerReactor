/*
 * Copyright (C) 2022. TriggerReactor Team
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

package io.github.wysohn.triggerreactor.core.manager.trigger.custom;

import com.google.inject.Guice;
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

        loader = mock(CustomTriggerLoader.class);
        registry = mock(IEventRegistry.class);
        manager = Guice.createInjector(

        ).getInstance(CustomTriggerManager.class);
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

        verify(registry).registerEvent(DummyEvent.class, mockTrigger);
    }

    @Test
    public void createCustomTrigger() throws AbstractTriggerManager.TriggerInitFailedException, ClassNotFoundException {
        String eventName = DummyEvent.class.getName();
        String name = "test";
        String script = "#MESSAGE \"hello world\"";

        doReturn(DummyEvent.class).when(registry).getEvent(eventName);

        manager.createCustomTrigger(eventName, name, script);

        assertNotNull(manager.get(name));
        verify(registry).registerEvent(eq(DummyEvent.class), any());
    }

    @Test
    public void remove() throws ClassNotFoundException, InvalidTrgConfigurationException {
        reload();

        manager.remove("test");

        verify(registry).unregisterEvent(any());
        assertNull(manager.get("test"));
    }

    public static class DummyEvent{

    }
}