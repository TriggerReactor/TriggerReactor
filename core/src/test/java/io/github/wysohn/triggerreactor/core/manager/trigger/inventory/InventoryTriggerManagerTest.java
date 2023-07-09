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

package io.github.wysohn.triggerreactor.core.manager.trigger.inventory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.IInventoryHandle;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.module.TestFileModule;
import io.github.wysohn.triggerreactor.core.module.TestTriggerDependencyModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.inject.Named;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InventoryTriggerManagerTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    InventoryTriggerLoader loader;
    IInventoryHandle handle;
    InventoryTriggerManager<ItemStack> manager;
    private TriggerInfo mockInfo;
    private InventoryTrigger mockTrigger;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        loader = mock(InventoryTriggerLoader.class);
        handle = mock(IInventoryHandle.class);

        when(handle.getItemClass()).thenReturn((Class) ItemStack.class);

        manager = Guice.createInjector(
                TestTriggerDependencyModule.Builder.begin().build(),
                new TestFileModule(folder),
                new FactoryModuleBuilder()
                        .implement(InventoryTrigger.class, InventoryTrigger.class)
                        .build(IInventoryTriggerFactory.class),
                new AbstractModule() {
                    @Provides
                    @Named("InventoryTriggerManagerFolder")
                    public String provideFolder() {
                        return "InventoryTrigger";
                    }

                    @Provides
                    public ITriggerLoader<InventoryTrigger> provideLoader() {
                        return loader;
                    }

                    @Provides
                    public IInventoryHandle provideHandle() {
                        return handle;
                    }
                }
        ).getInstance(InventoryTriggerManager.class);
    }

    private void mockReload() throws InvalidTrgConfigurationException {
        mockInfo = mock(TriggerInfo.class);
        mockTrigger = mock(InventoryTrigger.class);

        when(mockTrigger.getInfo()).thenReturn(mockInfo);
        when(mockTrigger.getItems()).thenReturn(new IItemStack[9]);
        when(mockInfo.get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_TITLE, String.class)).thenReturn(Optional.of("title"));
        when(mockInfo.get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_SIZE, Integer.class)).thenReturn(Optional.of(9));
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockInfo.getTriggerName()).thenReturn("test");
        when(loader.listTriggers(any(), any())).thenReturn(new TriggerInfo[]{mockInfo});
        when(loader.load(any())).thenReturn(mockTrigger);
        when(mockTrigger.getInfo()).thenReturn(mockInfo);

        manager.reload();
    }

    @Test
    public void openGUI() throws IOException, InvalidTrgConfigurationException {
        mockReload();

        IInventory mockInventory = mock(IInventory.class);
        when(handle.createInventory(anyInt(), any())).thenReturn(mockInventory);

        IPlayer player = mock(IPlayer.class);
        String invName = "test";

        IInventory inventory = manager.openGUI(player, invName);

        assertEquals(mockInventory, inventory);
    }

    @Test
    public void createTrigger() throws AbstractTriggerManager.TriggerInitFailedException {
        assertTrue(manager.createTrigger(9, "test", "#MESSAGE \"test\""));

        assertNotNull(manager.get("test"));
    }

    @Test
    public void onOpen() throws InvalidTrgConfigurationException {
        mockReload();

        IInventory mockInventory = mock(IInventory.class);
        Object eventInstance = mock(Object.class);
        IPlayer player = mock(IPlayer.class);

        when(handle.createInventory(anyInt(), any())).thenReturn(mockInventory);

        IInventory inv = manager.openGUI(player, "test");
        manager.onOpen(eventInstance, inv, player);

        verify(mockTrigger).activate(any(), any());
    }

    @Test
    public void onClick() throws InvalidTrgConfigurationException {
        mockReload();

        IInventory mockInventory = mock(IInventory.class);
        Object eventInstance = mock(Object.class);
        IPlayer player = mock(IPlayer.class);
        Consumer<Boolean> callback = mock(Consumer.class);
        IItemStack item = mock(IItemStack.class);

        when(handle.createInventory(anyInt(), any())).thenReturn(mockInventory);
        when(item.clone()).thenReturn(item);
        when(item.get()).thenReturn(new ItemStack());

        IInventory inv = manager.openGUI(player, "test");
        manager.onClick(eventInstance,
                inv,
                item,
                0,
                "left",
                0,
                callback);

        verify(mockTrigger).activate(any(), any());
    }

    @Test
    public void onInventoryClose() throws InvalidTrgConfigurationException {
        mockReload();

        IInventory mockInventory = mock(IInventory.class);
        Object eventInstance = mock(Object.class);
        IPlayer player = mock(IPlayer.class);

        when(handle.createInventory(anyInt(), any())).thenReturn(mockInventory);

        IInventory inv = manager.openGUI(player, "test");
        manager.onInventoryClose(eventInstance, player, inv);

        verify(mockTrigger).activate(any(), any(), eq(true));
        assertFalse(manager.hasInventoryOpen(inv));
    }

    @Test
    public void hasInventoryOpen() throws InvalidTrgConfigurationException {
        mockReload();

        IInventory mockInventory = mock(IInventory.class);
        when(handle.createInventory(anyInt(), any())).thenReturn(mockInventory);

        assertFalse(manager.hasInventoryOpen(mockInventory));

        IInventory inv = manager.openGUI(mock(IPlayer.class), "test");
        assertEquals(mockInventory, inv);
        assertTrue(manager.hasInventoryOpen(mockInventory));
    }

    public static class ItemStack {

    }

    public static class Inventory {

    }
}