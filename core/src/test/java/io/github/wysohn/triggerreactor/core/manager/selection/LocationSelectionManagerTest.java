package io.github.wysohn.triggerreactor.core.manager.selection;

import io.github.wysohn.triggerreactor.components.DaggerLocationSelectionManagerTestComponent;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class LocationSelectionManagerTest {

    @Test
    public void testLocationSetNotStarted() {
        LocationSelectionManager manager = DaggerLocationSelectionManagerTestComponent.builder()
                .permissionString("admin.permission")
                .build()
                .getLocationSelectionManager();

        SimpleLocation clicked = new SimpleLocation("world", 0, 0, 0);
        IPlayer player = mock(IPlayer.class);

        assertTrue(manager.onClick(clicked, player, ClickType.LEFT_CLICK));
    }

    @Test
    public void testLocationSetStarted(){
        LocationSelectionManager manager = DaggerLocationSelectionManagerTestComponent.builder()
                .permissionString("admin.permission")
                .build()
                .getLocationSelectionManager();

        SimpleLocation clicked = new SimpleLocation("world", 0, 0, 0);
        IPlayer player = mock(IPlayer.class);
        Function<SimpleLocation, Boolean> consumer = mock(Function.class);

        when(consumer.apply(clicked)).thenReturn(true);

        manager.startLocationSet(player, consumer);
        assertFalse(manager.onClick(clicked, player, ClickType.LEFT_CLICK));

        verify(consumer).apply(clicked);
        assertFalse(manager.hasStarted(player));
    }

    @Test
    public void testLocationSetStartedAndContinued(){
        LocationSelectionManager manager = DaggerLocationSelectionManagerTestComponent.builder()
                .permissionString("admin.permission")
                .build()
                .getLocationSelectionManager();

        SimpleLocation clicked = new SimpleLocation("world", 0, 0, 0);
        IPlayer player = mock(IPlayer.class);
        Function<SimpleLocation, Boolean> consumer = mock(Function.class);

        when(consumer.apply(clicked)).thenReturn(false);

        manager.startLocationSet(player, consumer);
        assertFalse(manager.onClick(clicked, player, ClickType.RIGHT_CLICK));

        verify(consumer).apply(clicked);
        assertTrue(manager.hasStarted(player));
    }

    @Test
    public void testLocationSetStartedAndContinuedWithCancel(){
        LocationSelectionManager manager = DaggerLocationSelectionManagerTestComponent.builder()
                .permissionString("admin.permission")
                .build()
                .getLocationSelectionManager();

        SimpleLocation clicked = new SimpleLocation("world", 0, 0, 0);
        IPlayer player = mock(IPlayer.class);
        Function<SimpleLocation, Boolean> consumer = mock(Function.class);

        when(consumer.apply(clicked)).thenReturn(false);
        when(player.isSneaking()).thenReturn(true); // player is sneaking to cancel

        manager.startLocationSet(player, consumer);
        assertFalse(manager.onClick(clicked, player, ClickType.RIGHT_CLICK));

        verify(consumer, never()).apply(clicked);
        assertFalse(manager.hasStarted(player));
    }
}