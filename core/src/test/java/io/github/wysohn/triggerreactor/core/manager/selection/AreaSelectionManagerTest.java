package io.github.wysohn.triggerreactor.core.manager.selection;

import io.github.wysohn.triggerreactor.components.DaggerAreaSelectionManagerTestComponent;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class AreaSelectionManagerTest {

    @Test
    public void testSelection() {
        AreaSelectionManager manager = DaggerAreaSelectionManagerTestComponent.builder()
                .build()
                .getAreaSelectionManager();

        UUID uuid = UUID.randomUUID();
        SimpleLocation loc1 = new SimpleLocation("world", 1, 2, 3);

        assertNull(manager.onClick(ClickType.LEFT_CLICK, uuid, loc1));
        assertNull(manager.getSelection(uuid));
    }

    @Test
    public void testSelectionToggle(){
        AreaSelectionManager manager = DaggerAreaSelectionManagerTestComponent.builder()
                .build()
                .getAreaSelectionManager();

        UUID uuid = UUID.randomUUID();
        SimpleLocation loc1 = new SimpleLocation("world", 1, 2, 3);
        SimpleLocation loc2 = new SimpleLocation("world", 4, 5, 6);
        Area area = new Area(loc1, loc2);

        assertTrue(manager.toggleSelection(uuid));
        assertEquals(manager.onClick(ClickType.LEFT_CLICK, uuid, loc1), ClickResult.LEFTSET);
        assertEquals(manager.onClick(ClickType.RIGHT_CLICK, uuid, loc2), ClickResult.COMPLETE);
        assertEquals(area, manager.getSelection(uuid));
        assertFalse(manager.toggleSelection(uuid));
    }

    @Test
    public void testSelectionToggle2(){
        AreaSelectionManager manager = DaggerAreaSelectionManagerTestComponent.builder()
                .build()
                .getAreaSelectionManager();

        UUID uuid = UUID.randomUUID();
        SimpleLocation loc1 = new SimpleLocation("world", 1, 2, 3);
        SimpleLocation loc2 = new SimpleLocation("world", 4, 5, 6);
        Area area = new Area(loc1, loc2);

        assertTrue(manager.toggleSelection(uuid));
        assertEquals(manager.onClick(ClickType.RIGHT_CLICK, uuid, loc1), ClickResult.RIGHTSET);
        assertEquals(manager.onClick(ClickType.LEFT_CLICK, uuid, loc2), ClickResult.COMPLETE);
        assertEquals(area, manager.getSelection(uuid));
        assertFalse(manager.toggleSelection(uuid));
    }
}