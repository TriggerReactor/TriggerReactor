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

import com.google.inject.Guice;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class RepeatingTriggerLoaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    RepeatingTriggerLoader loader;

    @Before
    public void init() {
        loader = Guice.createInjector(
                new FactoryModuleBuilder()
                        .implement(RepeatingTrigger.class, RepeatingTrigger.class)
                        .build(IRepeatingTriggerFactory.class),
                new TestFileModule(folder),
                TestTriggerDependencyModule.Builder.begin().build()
        ).getInstance(RepeatingTriggerLoader.class);
    }

    @Test
    public void load() throws IOException, InvalidTrgConfigurationException {
        TriggerInfo info = mock(TriggerInfo.class);
        when(info.getTriggerName()).thenReturn("test");
        when(info.getSourceCodeFile()).thenReturn(folder.newFile("test.trg"));
        when(info.get(TriggerConfigKey.KEY_TRIGGER_REPEATING_AUTOSTART, Boolean.class)).thenReturn(Optional.of(true));
        // our slightly modified version of the GSON treat int and long interchangeably
        when(info.get(TriggerConfigKey.KEY_TRIGGER_REPEATING_INTERVAL, Long.class)).thenReturn(Optional.of(100L));

        RepeatingTrigger trigger = loader.load(info);

        assertTrue(trigger.isAutoStart());
        assertEquals(100L, trigger.getInterval());
    }

    @Test
    public void save() throws IOException {
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        when(mockInfo.getTriggerName()).thenReturn("test");
        when(mockInfo.getSourceCodeFile()).thenReturn(folder.newFile("test.trg"));
        when(mockInfo.get(TriggerConfigKey.KEY_TRIGGER_REPEATING_INTERVAL, Long.class)).thenReturn(Optional.of(100L));

//        RepeatingTrigger trigger = new RepeatingTrigger(mockInfo, "#MESSAGE \"Hello world!\"");
        RepeatingTrigger trigger = Guice.createInjector(
                new FactoryModuleBuilder()
                        .implement(RepeatingTrigger.class, RepeatingTrigger.class)
                        .build(TempFactory.class),
                new TestFileModule(folder),
                TestTriggerDependencyModule.Builder.begin().build()
        ).getInstance(TempFactory.class).create(mockInfo, "#MESSAGE \"Hello world!\"");
        trigger.setAutoStart(true);
        trigger.setInterval(100);

        loader.save(trigger); // both save autostart and interval upon save

        verify(mockInfo, times(2))
                .put(TriggerConfigKey.KEY_TRIGGER_REPEATING_AUTOSTART, true);
        verify(mockInfo, times(2))
                .put(TriggerConfigKey.KEY_TRIGGER_REPEATING_INTERVAL, 100L);
    }

    private interface TempFactory {
        RepeatingTrigger create(TriggerInfo info, String sourceCode);
    }
}