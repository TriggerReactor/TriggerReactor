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

package io.github.wysohn.triggerreactor.core.manager.trigger.custom;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.IEventRegistry;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.module.TestFileModule;
import io.github.wysohn.triggerreactor.core.module.TestTriggerDependencyModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CustomTriggerLoaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    IEventRegistry registry;
    CustomTriggerLoader loader;

    @Before
    public void init() throws ClassNotFoundException {
        registry = mock(IEventRegistry.class);
        when(registry.getEvent(anyString())).thenReturn((Class) Object.class);

        loader = Guice.createInjector(
                new FactoryModuleBuilder().build(ICustomTriggerFactory.class),
                new TestFileModule(folder),
                TestTriggerDependencyModule.Builder.begin().build(),
                new AbstractModule() {
                    @Provides
                    IEventRegistry provideEventRegistry() {
                        return registry;
                    }

                    @Provides
                    ITriggerLoader<CustomTrigger> provideTriggerLoader() {
                        return loader;
                    }
                }
        ).getInstance(CustomTriggerLoader.class);
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