package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.components.DaggerPlayerLocationManagerTestComponent;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class PlayerLocationManagerTest {

    IGameController gameController;

    @Before
    public void init(){
        gameController = mock(IGameController.class);
    }

    @Test
    public void testLocationChange() {
        PlayerLocationManager manager = DaggerPlayerLocationManagerTestComponent.builder()
                .gameController(gameController)
                .build()
                .getPlayerLocationManager();

        IPlayerBlockLocationEvent event = mock(IPlayerBlockLocationEvent.class);
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();

        when(player.getName()).thenReturn("player");
        when(player.getUniqueId()).thenReturn(uuid);
        when(event.getFrom()).thenReturn(new SimpleLocation("world", 1, 2, 3));
        when(event.getTo()).thenReturn(new SimpleLocation("world", 4, 5, 6));
        when(event.getIPlayer()).thenReturn(player);

        manager.checkIfCancelled(event);

        verify(gameController).callEvent(eq(event));
        assertEquals(new SimpleLocation("world", 4, 5, 6), manager.getCurrentBlockLocation(uuid));
    }

    @Test
    public void testLocationChangeCancelled() {
        PlayerLocationManager manager = DaggerPlayerLocationManagerTestComponent.builder()
                .gameController(gameController)
                .build()
                .getPlayerLocationManager();

        IPlayerBlockLocationEvent event = mock(IPlayerBlockLocationEvent.class);
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();

        when(player.getName()).thenReturn("player");
        when(player.getUniqueId()).thenReturn(uuid);
        when(event.getFrom()).thenReturn(new SimpleLocation("world", 1, 2, 3));
        when(event.getTo()).thenReturn(new SimpleLocation("world", 4, 5, 6));
        when(event.getIPlayer()).thenReturn(player);
        when(event.isCancelled()).thenReturn(true);

        manager.checkIfCancelled(event);

        verify(gameController).callEvent(eq(event));
        assertNull(manager.getCurrentBlockLocation(uuid));
    }
}