package io.github.wysohn.triggerreactor.core.manager;

import com.google.inject.Guice;
import io.github.wysohn.triggerreactor.core.bridge.IBlock;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AreaSelectionManagerTest {
    AreaSelectionManager areaSelectionManager;

    @Before
    public void setUp() throws Exception {
        areaSelectionManager = Guice.createInjector(

        ).getInstance(AreaSelectionManager.class);
    }

    @Test
    public void onInteract() {
        // arrange
        UUID uuid = UUID.randomUUID();
        ILocation clickLocation = mock(ILocation.class);
        SimpleLocation simpleLocation = new SimpleLocation("world", 1, 2, 3);

        IPlayer player = mock(IPlayer.class);
        Consumer<Boolean> eventCanceler = mock(Consumer.class);
        IBlock clickedBlock = mock(IBlock.class);

        when(player.getUniqueId()).thenReturn(uuid);
        when(clickedBlock.getLocation()).thenReturn(clickLocation);
        when(clickLocation.toSimpleLocation()).thenReturn(simpleLocation);

        // act
        areaSelectionManager.toggleSelection(uuid);

        areaSelectionManager.onInteract(player,
                true,
                eventCanceler,
                clickedBlock,
                AreaSelectionManager.ClickAction.LEFT_CLICK_BLOCK);
        SimpleLocation result1 = areaSelectionManager.getLeftPosition(uuid);
        SimpleLocation result2 = areaSelectionManager.getRightPosition(uuid);
        Area result3 = areaSelectionManager.getSelection(uuid);

        areaSelectionManager.onInteract(player,
                true,
                eventCanceler,
                clickedBlock,
                AreaSelectionManager.ClickAction.RIGHT_CLICK_BLOCK);
        SimpleLocation result4 = areaSelectionManager.getLeftPosition(uuid);
        SimpleLocation result5 = areaSelectionManager.getRightPosition(uuid);
        Area result6 = areaSelectionManager.getSelection(uuid);

        // assert
        assertNotNull(result1);
        assertNull(result2);
        assertNull(result3);

        assertNotNull(result4);
        assertNotNull(result5);
        assertNotNull(result6);
    }

    @Test
    public void onInteract_rightFirst() {
        // arrange
        UUID uuid = UUID.randomUUID();
        ILocation clickLocation = mock(ILocation.class);
        SimpleLocation simpleLocation = new SimpleLocation("world", 1, 2, 3);

        IPlayer player = mock(IPlayer.class);
        Consumer<Boolean> eventCanceler = mock(Consumer.class);
        IBlock clickedBlock = mock(IBlock.class);

        when(player.getUniqueId()).thenReturn(uuid);
        when(clickedBlock.getLocation()).thenReturn(clickLocation);
        when(clickLocation.toSimpleLocation()).thenReturn(simpleLocation);

        // act
        areaSelectionManager.toggleSelection(uuid);

        areaSelectionManager.onInteract(player,
                true,
                eventCanceler,
                clickedBlock,
                AreaSelectionManager.ClickAction.RIGHT_CLICK_BLOCK);
        SimpleLocation result1 = areaSelectionManager.getLeftPosition(uuid);
        SimpleLocation result2 = areaSelectionManager.getRightPosition(uuid);
        Area result3 = areaSelectionManager.getSelection(uuid);

        areaSelectionManager.onInteract(player,
                true,
                eventCanceler,
                clickedBlock,
                AreaSelectionManager.ClickAction.LEFT_CLICK_BLOCK);
        SimpleLocation result4 = areaSelectionManager.getLeftPosition(uuid);
        SimpleLocation result5 = areaSelectionManager.getRightPosition(uuid);
        Area result6 = areaSelectionManager.getSelection(uuid);

        // assert
        assertNull(result1);
        assertNotNull(result2);
        assertNull(result3);

        assertNotNull(result4);
        assertNotNull(result5);
        assertNotNull(result6);
    }

    @Test
    public void onInteract_differentWorld() {
        // arrange
        UUID uuid = UUID.randomUUID();
        ILocation clickLocation = mock(ILocation.class);
        SimpleLocation simpleLocation = new SimpleLocation("world", 1, 2, 3);
        ILocation clickLocation2 = mock(ILocation.class);
        SimpleLocation simpleLocation2 = new SimpleLocation("world2", 1, 2, 3);

        IPlayer player = mock(IPlayer.class);
        Consumer<Boolean> eventCanceler = mock(Consumer.class);
        IBlock clickedBlock = mock(IBlock.class);
        IBlock clickedBlock2 = mock(IBlock.class);

        when(player.getUniqueId()).thenReturn(uuid);
        when(clickedBlock.getLocation()).thenReturn(clickLocation);
        when(clickLocation.toSimpleLocation()).thenReturn(simpleLocation);
        when(clickedBlock2.getLocation()).thenReturn(clickLocation2);
        when(clickLocation2.toSimpleLocation()).thenReturn(simpleLocation2);

        // act
        areaSelectionManager.toggleSelection(uuid);

        areaSelectionManager.onInteract(player,
                true,
                eventCanceler,
                clickedBlock,
                AreaSelectionManager.ClickAction.LEFT_CLICK_BLOCK);
        SimpleLocation result1 = areaSelectionManager.getLeftPosition(uuid);
        SimpleLocation result2 = areaSelectionManager.getRightPosition(uuid);
        Area result3 = areaSelectionManager.getSelection(uuid);

        areaSelectionManager.onInteract(player,
                true,
                eventCanceler,
                clickedBlock2,
                AreaSelectionManager.ClickAction.RIGHT_CLICK_BLOCK);
        SimpleLocation result4 = areaSelectionManager.getLeftPosition(uuid);
        SimpleLocation result5 = areaSelectionManager.getRightPosition(uuid);
        Area result6 = areaSelectionManager.getSelection(uuid);

        // assert
        assertNotNull(result1);
        assertNull(result2);
        assertNull(result3);

        assertNotNull(result4);
        assertNotNull(result5);
        assertNull(result6);
    }
}