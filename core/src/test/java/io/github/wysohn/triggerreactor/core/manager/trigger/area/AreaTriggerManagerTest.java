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

package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.module.TestFileModule;
import io.github.wysohn.triggerreactor.core.module.TestTriggerDependencyModule;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.inject.Named;
import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class AreaTriggerManagerTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    TaskSupervisor taskSupervisor;
    ITriggerLoader<AreaTrigger> loader;

    AreaTriggerManager manager;

    @Before
    public void setUp() throws Exception {
        taskSupervisor = mock(TaskSupervisor.class);
        loader = mock(ITriggerLoader.class);

        manager = Guice.createInjector(
                new TestFileModule(folder),
                TestTriggerDependencyModule.Builder.begin().build(),
                new FactoryModuleBuilder().build(IAreaTriggerFactory.class),
                new FactoryModuleBuilder().build(IEnterTriggerFactory.class),
                new FactoryModuleBuilder().build(IExitTriggerFactory.class),
                new FactoryModuleBuilder()
                        .implement(IConfigSource.class, GsonConfigSource.class)
                        .build(IConfigSourceFactory.class),
                new AbstractModule() {
                    @Provides
                    public ITriggerLoader<AreaTrigger> provideLoader() {
                        return loader;
                    }

                    @Provides
                    @Named("AreaTriggerManagerFolder")
                    public String provideFolder() throws IOException {
                        return "AreaTrigger";
                    }
                }
        ).getInstance(AreaTriggerManager.class);
    }

    @Test
    public void reload() throws InvalidTrgConfigurationException {
        AreaTriggerInfo mockInfo = mock(AreaTriggerInfo.class);
        AreaTrigger mockTrigger = mock(AreaTrigger.class);

        when(mockInfo.getTriggerName()).thenReturn("test");
        when(mockTrigger.getArea()).thenReturn(new Area(new SimpleLocation("world", 0, 0, 0),
                new SimpleLocation("world", 0, 0, 0)));
        when(loader.listTriggers(any(), any(), any())).thenReturn(new AreaTriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);

        manager.reload();

        verify(mockInfo, times(1)).reload();
    }

    @Test
    public void remove() throws InvalidTrgConfigurationException {
        AreaTriggerInfo mockInfo = mock(AreaTriggerInfo.class);
        AreaTrigger mockTrigger = mock(AreaTrigger.class);

        when(mockInfo.getTriggerName()).thenReturn("test");
        when(mockTrigger.getArea()).thenReturn(new Area(new SimpleLocation("world", 0, 0, 0),
                new SimpleLocation("world", 0, 0, 0)));
        when(loader.listTriggers(any(), any(), any())).thenReturn(new AreaTriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);

        manager.reload();
        assertNotNull(manager.get("test"));

        manager.remove("test");
        assertNull(manager.get("test"));
    }

    @Test
    public void onJoin() {
        IPlayer player = mock(IPlayer.class);
        manager.onJoin(new SimpleLocation("world", 0, 0, 0), player);
    }

    @Test
    public void onLocationChange() {
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();

        when(player.getUniqueId()).thenReturn(uuid);

        manager.onLocationChange(null,
                new SimpleLocation("world", 0, 0, 0),
                new SimpleLocation("world", 0, 0, 3),
                player);
    }

    @Test
    public void onLocationChangeEnteringArea() {
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();

        when(player.getUniqueId()).thenReturn(uuid);

        manager.createArea("test",
                new SimpleLocation("world", 16, 30, 16),
                new SimpleLocation("world", 25, 100, 25));
        manager.onLocationChange(null,
                new SimpleLocation("world", 0, 50, 0),
                new SimpleLocation("world", 16, 50, 16),
                player);

        AreaTrigger trigger = manager.get("test");
        assertNotNull(trigger);
        assertNotNull(trigger.getEntity(uuid));
    }

    @Test
    public void onLocationChangeLeavingArea() {
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();

        when(player.getUniqueId()).thenReturn(uuid);

        manager.createArea("test",
                new SimpleLocation("world", 16, 30, 16),
                new SimpleLocation("world", 25, 100, 25));
        manager.onLocationChange(null,
                new SimpleLocation("world", 16, 50, 16),
                new SimpleLocation("world", 15, 50, 15),
                player);

        AreaTrigger trigger = manager.get("test");
        assertNotNull(trigger);
        assertNull(trigger.getEntity(uuid));
    }

    @Test
    public void onSpawn() {
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();

        when(player.getUniqueId()).thenReturn(uuid);

        manager.createArea("test",
                new SimpleLocation("world", -25, 30, -25),
                new SimpleLocation("world", 25, 100, 25));
        manager.onSpawn(player, new SimpleLocation("world", 0, 50, 0));

        AreaTrigger trigger = manager.get("test");
        assertNotNull(trigger);
        assertNotNull(trigger.getEntity(uuid));
    }

    @Test
    public void onDeath() {
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();

        when(player.getUniqueId()).thenReturn(uuid);

        manager.createArea("test",
                new SimpleLocation("world", -25, 30, -25),
                new SimpleLocation("world", 25, 100, 25));
        manager.onSpawn(player, new SimpleLocation("world", 0, 50, 0));
        manager.onDeath(uuid, new SimpleLocation("world", 0, 50, 0));

        AreaTrigger trigger = manager.get("test");
        assertNotNull(trigger);
        assertNull(trigger.getEntity(uuid));
    }
}
