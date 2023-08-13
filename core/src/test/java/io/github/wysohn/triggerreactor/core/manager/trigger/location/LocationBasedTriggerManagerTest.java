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
import io.github.wysohn.triggerreactor.core.bridge.IBlock;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.manager.ScriptEditManager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
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
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LocationBasedTriggerManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    ClickTriggerManager manager;
    ITriggerLoader<ClickTrigger> loader;
    ScriptEditManager mockEditManager;

    private TriggerInfo mockInfo;
    private ClickTrigger mockTrigger;

    @Before
    public void setUp() throws Exception {
        loader = mock(ITriggerLoader.class);
        mockEditManager = mock(ScriptEditManager.class);

        manager = Guice.createInjector(
                new TestFileModule(folder),
                TestTriggerDependencyModule.Builder.begin().build(),
                new FactoryModuleBuilder().build(IClickTriggerFactory.class),
                new AbstractModule() {
                    @Provides
                    public ITriggerLoader<ClickTrigger> provideLoader() {
                        return loader;
                    }

                    @Provides
                    @Named("ClickTriggerManagerFolder")
                    public String provideFolder() throws IOException {
                        return "ClickTrigger";
                    }

                    @Provides
                    public ScriptEditManager provideScriptEditManager() {
                        return mockEditManager;
                    }
                }
        ).getInstance(ClickTriggerManager.class);
    }

    @Test
    public void testReload() throws Exception {
        // arrange
        ClickTrigger mockTrigger = mock(ClickTrigger.class);
        TriggerInfo mockInfo = mock(TriggerInfo.class);

        when(mockTrigger.getInfo()).thenReturn(mockInfo);
        when(mockInfo.getTriggerName()).thenReturn("world@1,2,3");
        when(loader.listTriggers(any(), any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);

        // act
        manager.reload();

        // assert
        assertEquals(mockTrigger, manager.getTriggerForLocation(new SimpleLocation("world",
                1,
                2,
                3)));

    }

    @Test
    public void testGetTriggerForLocation() throws InvalidTrgConfigurationException {
        // arrange
        ClickTrigger mockTrigger = mock(ClickTrigger.class);
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        ILocation mockLocation = mock(ILocation.class);

        when(mockTrigger.getInfo()).thenReturn(mockInfo);
        when(mockInfo.getTriggerName()).thenReturn("world@1,2,3");
        when(loader.listTriggers(any(), any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockLocation.toSimpleLocation()).thenReturn(new SimpleLocation("world", 1, 2, 3));

        // act
        manager.reload();

        // assert
        assertEquals(mockTrigger, manager.getTriggerForLocation(mockLocation));
    }

    @Test
    public void startLocationSet() {
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("test");
        when(player.getLocation()).thenReturn(new TempLoc("world", 1, 2, 3));

        assertFalse(manager.isLocationSetting(player));

        manager.startLocationSet(player, "the script");
        assertTrue(manager.isLocationSetting(player));

        assertEquals("the script", manager.getSettingLocationScript(player));

        manager.stopLocationSet(player);
        assertFalse(manager.isLocationSetting(player));
    }

    @Test
    public void cutTrigger() throws InvalidTrgConfigurationException {
        // arrange
        ClickTrigger mockTrigger = mock(ClickTrigger.class);
        ClickTrigger mockTrigger2 = mock(ClickTrigger.class);
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();

        when(player.getUniqueId()).thenReturn(uuid);
        when(mockTrigger.getInfo()).thenReturn(mockInfo);
        when(mockInfo.getTriggerName()).thenReturn("world@1,2,3");
        when(loader.listTriggers(any(), any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockTrigger.clone()).thenReturn(mockTrigger2);

        // act
        manager.reload();

        boolean result1 = manager.cutTrigger(player, new TempLoc("world", 1, 2, 3));
        boolean result2 = manager.pasteTrigger(player, new TempLoc("world", 1, 2, 4));
        boolean result3 = manager.pasteTrigger(player, new TempLoc("world", 1, 2, 5));
        ClickTrigger result4 = manager.getTriggerForLocation(new TempLoc("world", 1, 2, 3));
        ClickTrigger result5 = manager.getTriggerForLocation(new SimpleLocation("world", 1, 2, 4));

        // assert
        assertTrue(result1);
        assertTrue(result2);
        assertFalse(result3);
        assertNull(result4);
        assertNotNull(result5);
    }

    @Test
    public void copyTrigger() throws InvalidTrgConfigurationException {
        // arrange
        ClickTrigger mockTrigger = mock(ClickTrigger.class);
        ClickTrigger mockTrigger2 = mock(ClickTrigger.class);
        TriggerInfo mockInfo = mock(TriggerInfo.class);
        IPlayer player = mock(IPlayer.class);

        UUID uuid = UUID.randomUUID();

        when(player.getUniqueId()).thenReturn(uuid);
        when(mockTrigger.getInfo()).thenReturn(mockInfo);
        when(mockInfo.getTriggerName()).thenReturn("world@1,2,3");
        when(loader.listTriggers(any(), any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockTrigger.clone()).thenReturn(mockTrigger2);

        // act
        manager.reload();

        boolean result1 = manager.copyTrigger(player, new TempLoc("world", 1, 2, 3));
        boolean result2 = manager.pasteTrigger(player, new TempLoc("world", 1, 2, 4));
        boolean result3 = manager.pasteTrigger(player, new TempLoc("world", 1, 2, 5));
        ClickTrigger result4 = manager.getTriggerForLocation(new TempLoc("world", 1, 2, 3));
        ClickTrigger result5 = manager.getTriggerForLocation(new SimpleLocation("world", 1, 2, 4));

        // assert
        assertTrue(result1);
        assertTrue(result2);
        assertFalse(result3);
        assertNotNull(result4);
        assertNotNull(result5);
    }

    private int indexOf(int[][] coords, int[] coord) {
        for (int i = 0; i < coords.length; i++) {
            if (coords[i][0] == coord[0] && coords[i][1] == coord[1] && coords[i][2] == coord[2]) {
                return i;
            }
        }
        return -1;
    }

    @Test
    public void getTriggersInChunk() throws InvalidTrgConfigurationException {
        // TODO

//        // arrange
//
//        int count = 8 * 8 + 8;
//        // prepare boundary cases for vertices (8 per 8 vertices)
//        //  ex) coordinate 0,0,0 has 8 boundary cases:
//        //        -1,-1,-1 / -1,-1,1 / 1,-1,-1 / 1,-1,1 / -1,1,-1 / -1,1,1 / 1,1,-1 / 1,1,1
//        ClickTrigger[] triggers = new ClickTrigger[count];
//        TriggerInfo[] infos = new TriggerInfo[count];
//        int[][] coords = new int[][]{
//                // 0,0,0
//                {-1, -1, -1},
//                {-1, -1, 1},
//                {1, -1, -1},
//                {1, -1, 1}, // valid
//                {-1, 1, -1},
//                {-1, 1, 1},
//                {1, 1, -1},
//                {1, 1, 1}, // valid 7
//                // 0,0,16
//                {-1, -1, 15},
//                {-1, -1, 17},
//                {1, -1, 15}, // valid
//                {1, -1, 17},
//                {-1, 1, 15},
//                {-1, 1, 17},
//                {1, 1, 15}, // valid
//                {1, 1, 17}, // 15
//                // 16,0,0
//                {15, -1, -1},
//                {15, -1, 1}, // valid
//                {17, -1, -1},
//                {17, -1, 1},
//                {15, 1, -1},
//                {15, 1, 1}, // valid
//                {17, 1, -1},
//                {17, 1, 1}, // 23
//                // 16,0,16
//                {15, -1, 15}, // valid
//                {15, -1, 17},
//                {17, -1, 15},
//                {17, -1, 17},
//                {15, 1, 15}, // valid
//                {15, 1, 17},
//                {17, 1, 15},
//                {17, 1, 17}, // 31
//                // 0,16,0
//                {-1, 15, -1},
//                {-1, 15, 1},
//                {1, 15, -1},
//                {1, 15, 1}, // valid
//                {-1, 17, -1},
//                {-1, 17, 1},
//                {1, 17, -1},
//                {1, 17, 1}, // valid 39
//                // 0,16,16
//                {-1, 15, 15},
//                {-1, 15, 17},
//                {1, 15, 15}, // valid
//                {1, 15, 17},
//                {-1, 17, 15},
//                {-1, 17, 17},
//                {1, 17, 15}, // valid
//                {1, 17, 17}, // 47
//                // 16,16,0
//                {15, 15, -1},
//                {15, 15, 1}, // valid
//                {17, 15, -1},
//                {17, 15, 1},
//                {15, 17, -1},
//                {15, 17, 1}, // valid
//                {17, 17, -1},
//                {17, 17, 1}, // 55
//                // 16,16,16
//                {15, 15, 15}, // valid
//                {15, 15, 17},
//                {17, 15, 15},
//                {17, 15, 17},
//                {15, 17, 15}, // valid
//                {15, 17, 17},
//                {17, 17, 15},
//                {17, 17, 17}, // 63
//                // actual vertices
//                {0, 0, 0},
//                {0, 0, 15},
//                {15, 0, 0},
//                {15, 0, 15},
//                {0, 15, 0},
//                {0, 15, 15},
//                {15, 15, 0},
//                {15, 15, 15}, // 71
//        };
//        Set<Integer> validCoordsIndices = Arrays.stream(new int[]{3, 7,
//                        10, 14,
//                        17, 21,
//                        14, 28,
//                        35, 39,
//                        42, 46,
//                        49, 53,
//                        56, 60,
//                        64, 65, 66, 67, 68, 69, 70, 71})
//                .boxed().collect(Collectors.toSet());
//
//        for (int i = 0; i < coords.length; i++) {
//            int[] coord = coords[i];
//            String name = String.format("world@%d,%d,%d", coord[0], coord[1], coord[2]);
//            TriggerInfo info = mock(TriggerInfo.class);
//            when(info.getTriggerName()).thenReturn(name);
//            infos[i] = info;
//
//            ClickTrigger trigger = mock(ClickTrigger.class);
//            triggers[i] = trigger;
//            when(trigger.getInfo()).thenReturn(info);
//
//            when(loader.load(info)).thenReturn(trigger);
//        }
//
//        when(loader.listTriggers(any(), any())).thenReturn(infos);
//
//        SimpleChunkLocation chunkLocation = new SimpleChunkLocation("world", 0, 0);
//
//        // act
//        manager.reload();
//
//        Set<Map.Entry<SimpleLocation, Trigger>> result = manager.getTriggersInChunk(chunkLocation);
//
//
//        // assert
//        assertEquals(validCoordsIndices.size(), result.size());
//        result.forEach(entry -> {
//            SimpleLocation loc = entry.getKey();
//            int[] coord = new int[]{loc.getX(), loc.getY(), loc.getZ()};
//            int index = indexOf(coords, coord);
//
//            assertTrue(validCoordsIndices.contains(index));
//        });
    }

    @Test
    public void handleLocationSetting() {
        // arrange
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();

        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("test");
        when(player.getLocation()).thenReturn(new TempLoc("world", 1, 2, 3));

        // act
        ClickTrigger result1 = manager.getTriggerForLocation(new SimpleLocation("world", 1, 2, 3));
        manager.startLocationSet(player, "the script");
        boolean result2 = manager.isLocationSetting(player);
        manager.handleLocationSetting(new TempLoc("world", 1, 2, 3), player);
        boolean result3 = manager.isLocationSetting(player);
        ClickTrigger result4 = manager.getTriggerForLocation(new SimpleLocation("world", 1, 2, 3));

        // assert
        assertNull(result1);
        assertTrue(result2);
        assertFalse(result3);
        assertNotNull(result4);
    }

    @Test
    public void handleScriptEdit() throws InvalidTrgConfigurationException,
            AbstractTriggerManager.TriggerInitFailedException {
        // arrange
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();
        ClickTrigger mockTrigger = mock(ClickTrigger.class);
        TriggerInfo mockInfo = mock(TriggerInfo.class);

        when(mockTrigger.getInfo()).thenReturn(mockInfo);
        when(mockInfo.getTriggerName()).thenReturn("test");

        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("test");
        when(player.getLocation()).thenReturn(new TempLoc("world", 1, 2, 3));

        // act
        manager.handleScriptEdit(player, mockTrigger);

        // assert
        verify(mockEditManager).startEdit(eq(player), any(), any(), any());
    }

    @Test
    public void removeTriggerForLocation() throws InvalidTrgConfigurationException {
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("test");
        when(player.getLocation()).thenReturn(new TempLoc("world", 1, 2, 3));

        manager.removeTriggerForLocation(new TempLoc("world", 1, 2, 3));
        assertNull(manager.getTriggerForLocation(new SimpleLocation("world", 1, 2, 3)));
    }

    @Test
    public void showTriggerInfo() throws InvalidTrgConfigurationException {
        // arrange
        ClickTrigger mockTrigger = mock(ClickTrigger.class);
        TriggerInfo mockInfo = mock(TriggerInfo.class);

        when(mockTrigger.getInfo()).thenReturn(mockInfo);
        when(mockInfo.getTriggerName()).thenReturn("world@1,2,3");

        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();

        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("test");
        when(player.getLocation()).thenReturn(new TempLoc("world", 1, 2, 3));

        when(loader.listTriggers(any(), any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);

        // act
        manager.reload();

        manager.showTriggerInfo(player, new TempLoc("world", 1, 2, 3));

        // assert
        verify(player, atLeastOnce()).sendMessage(anyString());
    }

    @Test
    public void onItemSwap() {
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("test");
        when(player.getLocation()).thenReturn(new TempLoc("world", 1, 2, 3));

        manager.startLocationSet(player, "the script");
        manager.onItemSwap(player);
        assertFalse(manager.pasteTrigger(player, new TempLoc("world", 1, 2, 3)));
    }

    @Test
    public void handleClick() throws InvalidTrgConfigurationException {
        // arrange
        ClickTrigger mockTrigger = mock(ClickTrigger.class);
        TriggerInfo mockInfo = mock(TriggerInfo.class);

        Object eventInstance = mock(Object.class);
        IBlock clickedBlock = mock(IBlock.class);
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();
        IItemStack itemInHand = mock(IItemStack.class);

        when(mockTrigger.getInfo()).thenReturn(mockInfo);
        when(mockInfo.getTriggerName()).thenReturn("world@1,2,3");

        when(clickedBlock.getLocation()).thenReturn(new TempLoc("world", 1, 2, 3));
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("test");
        when(player.getLocation()).thenReturn(new TempLoc("world", 1, 2, 3));

        when(loader.listTriggers(any(), any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);

        // act
        manager.reload();

        manager.startLocationSet(player, "the script");
        manager.handleClick(eventInstance,
                clickedBlock,
                player,
                itemInHand,
                Activity.LEFT_CLICK_BLOCK);

        // assert
        verify(mockTrigger).activate(eq(eventInstance), anyMap());
    }

    @Test
    public void handleWalk() throws InvalidTrgConfigurationException {
        // arrange
        ClickTrigger mockTrigger = mock(ClickTrigger.class);
        TriggerInfo mockInfo = mock(TriggerInfo.class);

        Object eventInstance = mock(Object.class);
        IBlock clickedBlock = mock(IBlock.class);
        IPlayer player = mock(IPlayer.class);
        UUID uuid = UUID.randomUUID();

        when(mockTrigger.getInfo()).thenReturn(mockInfo);
        when(mockInfo.getTriggerName()).thenReturn("world@1,2,3");

        when(clickedBlock.getLocation()).thenReturn(new TempLoc("world", 1, 2, 3));
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("test");
        when(player.getLocation()).thenReturn(new TempLoc("world", 1, 2, 3));

        when(loader.listTriggers(any(), any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);

        // act
        manager.reload();

        manager.startLocationSet(player, "the script");
        manager.handleWalk(eventInstance,
                player,
                new SimpleLocation("world", 1, 3, 2),
                new SimpleLocation("world", 1, 3, 3),
                new TempBlock(new TempLoc("world", 1, 2, 3)));

        // assert
        verify(mockTrigger).activate(eq(eventInstance), anyMap());
    }

    public static class TempLoc implements ILocation {
        private final String world;
        private final int x;
        private final int y;
        private final int z;

        public TempLoc(String world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public SimpleLocation toSimpleLocation() {
            return new SimpleLocation(world, x, y, z);
        }

        @Override
        public IWorld getWorld() {
            return new TempWorld(world);
        }

        @Override
        public <T> T get() {
            return null;
        }
    }

    public static class TempWorld implements IWorld {
        private final String name;

        public TempWorld(String name) {
            this.name = name;
        }

        @Override
        public Iterable<IEntity> getEntities() {
            return null;
        }

        @Override
        public IBlock getBlock(SimpleLocation clicked) {
            return null;
        }

        @Override
        public IBlock getBlock(ILocation location) {
            return new TempBlock(location);
        }
    }

    public static class TempBlock implements IBlock {
        private final ILocation location;

        public TempBlock(ILocation location) {
            this.location = location;
        }

        @Override
        public ILocation getLocation() {
            return location;
        }

        @Override
        public String getTypeName() {
            return "temptype";
        }

        @Override
        public <T> T get() {
            return null;
        }
    }
}
