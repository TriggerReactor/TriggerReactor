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

package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import com.google.inject.Guice;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.module.TestFileModule;
import io.github.wysohn.triggerreactor.core.module.TestTriggerDependencyModule;
import io.github.wysohn.triggerreactor.core.module.manager.trigger.WalkTriggerModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WalkTriggerLoaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    WalkTriggerLoader loader;

    @Before
    public void setUp() throws Exception {
        loader = Guice.createInjector(
            new WalkTriggerModule(),
            new TestFileModule(folder),
            new FactoryModuleBuilder()
                .implement(IConfigSource.class, GsonConfigSource.class)
                .build(IConfigSourceFactory.class),
            TestTriggerDependencyModule.Builder.begin().build()
        ).getInstance(WalkTriggerLoader.class);
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
