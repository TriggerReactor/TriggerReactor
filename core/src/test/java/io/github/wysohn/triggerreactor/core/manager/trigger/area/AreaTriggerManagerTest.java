package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.IGameStateSupervisor;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class AreaTriggerManagerTest {

    TriggerReactorCore core;
    TaskSupervisor taskSupervisor;
    IGameStateSupervisor gameStateSupervisor;
    ITriggerLoader<AreaTrigger> loader;

    AreaTriggerManager manager;

    @Before
    public void setUp() throws Exception {
        core = mock(TriggerReactorCore.class);
        Field instanceField = TriggerReactorCore.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, core);

        taskSupervisor = mock(TaskSupervisor.class);
        gameStateSupervisor = mock(IGameStateSupervisor.class);
        loader = mock(ITriggerLoader.class);

        manager = new AreaTriggerManager(core, taskSupervisor, gameStateSupervisor, loader);
    }

    @Test
    public void reload() throws InvalidTrgConfigurationException {
        AreaTriggerInfo mockInfo = mock(AreaTriggerInfo.class);
        AreaTrigger mockTrigger = mock(AreaTrigger.class);

        when(mockInfo.getTriggerName()).thenReturn("test");
        when(mockTrigger.getArea()).thenReturn(new Area(new SimpleLocation("world", 0, 0, 0),
                                                        new SimpleLocation("world", 0, 0, 0)));
        when(loader.listTriggers(any(), any())).thenReturn(new AreaTriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);

        manager.reload();

        verify(mockInfo, times(1)).reloadConfig();
    }

    @Test
    public void remove() throws InvalidTrgConfigurationException {
        AreaTriggerInfo mockInfo = mock(AreaTriggerInfo.class);
        AreaTrigger mockTrigger = mock(AreaTrigger.class);

        when(mockInfo.getTriggerName()).thenReturn("test");
        when(mockTrigger.getArea()).thenReturn(new Area(new SimpleLocation("world", 0, 0, 0),
                                                        new SimpleLocation("world", 0, 0, 0)));
        when(loader.listTriggers(any(), any())).thenReturn(new AreaTriggerInfo[]{mockInfo});
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