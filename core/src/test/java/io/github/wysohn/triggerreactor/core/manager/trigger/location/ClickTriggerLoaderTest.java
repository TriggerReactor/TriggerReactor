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

package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import com.google.inject.Guice;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ClickTriggerLoaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    TriggerReactorCore core;

    ClickTriggerLoader loader;

    @Before
    public void setUp() throws Exception {
        core = mock(TriggerReactorCore.class, RETURNS_DEEP_STUBS);
        Field instanceField = TriggerReactorCore.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, core);

        loader = Guice.createInjector().getInstance(ClickTriggerLoader.class);
    }

    @Test
    public void load() throws IOException, InvalidTrgConfigurationException {
        TriggerInfo info = mock(TriggerInfo.class);
        when(info.getTriggerName()).thenReturn("test");
        when(info.getSourceCodeFile()).thenReturn(folder.newFile("test.trg"));

        ClickTrigger trigger = loader.load(info);
    }

    @Test
    public void save() throws IOException {
        TriggerInfo info = mock(TriggerInfo.class);
        ClickTrigger trigger = mock(ClickTrigger.class);

        when(trigger.getInfo()).thenReturn(info);
        when(trigger.getScript()).thenReturn("#MESSAGE \"Hello World\"");
        when(info.getTriggerName()).thenReturn("test");
        when(info.getSourceCodeFile()).thenReturn(folder.newFile("test.trg"));

        loader.save(trigger);

        assertTrue(info.getSourceCodeFile().exists());
    }

    @Test
    public void newInstance() {
    }
}