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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.module.TestFileModule;
import io.github.wysohn.triggerreactor.core.module.TestTriggerDependencyModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.inject.Named;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class WalkTriggerManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    WalkTriggerLoader loader;
    WalkTriggerManager manager;


    @Before
    public void setUp() throws Exception {
        loader = mock(WalkTriggerLoader.class);

        manager = Guice.createInjector(
            new TestFileModule(folder),
            TestTriggerDependencyModule.Builder.begin().build(),
            new FactoryModuleBuilder().build(IWalkTriggerFactory.class),
            new FactoryModuleBuilder()
                .implement(IConfigSource.class, GsonConfigSource.class)
                .build(IConfigSourceFactory.class),
            new AbstractModule() {
                @Provides
                public ITriggerLoader<WalkTrigger> provideLoader() {
                    return loader;
                }

                @Provides
                @Named("WalkTriggerManagerFolder")
                public String provideFolder() throws IOException {
                    return "WalkTrigger";
                }
            }
        ).getInstance(WalkTriggerManager.class);
    }

    @Test
    public void getTriggerTypeName() {
        assertNotNull(manager.getTriggerTypeName());
    }

    @Test
    public void newTrigger() throws AbstractTriggerManager.TriggerInitFailedException {
        TriggerInfo info = mock(TriggerInfo.class);
        assertNotNull(manager.newInstance(info, "#MESSAGE \"Hello World\""));
    }
}
