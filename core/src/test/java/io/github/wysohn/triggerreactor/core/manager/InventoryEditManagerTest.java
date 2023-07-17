package io.github.wysohn.triggerreactor.core.manager;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Providers;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IInventoryHandle;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class InventoryEditManagerTest {

    private InventoryEditManager<ItemStack> inventoryEditManager;

    private InventoryTriggerManager<ItemStack> inventoryTriggerManager;
    private IInventoryHandle inventoryHandle;
    private IPluginManagement pluginManagement;
    private ITriggerLoader<InventoryTrigger> inventoryTriggerLoader;

    @Before
    public void setUp() throws Exception {
        inventoryEditManager = Guice.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(new TypeLiteral<InventoryTriggerManager<ItemStack>>() {
                        })
                                .toProvider(Providers.of(inventoryTriggerManager = mock(InventoryTriggerManager.class)));
                        bind(IInventoryHandle.class)
                                .toInstance(inventoryHandle = mock(IInventoryHandle.class));
                        bind(IPluginManagement.class)
                                .toInstance(pluginManagement = mock(IPluginManagement.class));
                        bind(new TypeLiteral<ITriggerLoader<InventoryTrigger>>() {
                        })
                                .toInstance(inventoryTriggerLoader = mock(ITriggerLoader.class));
                    }
                }
        ).getInstance(Key.get(new TypeLiteral<InventoryEditManager<ItemStack>>() {
        }));
    }

    @Test
    public void initialize() {

    }

    @Test
    public void reload() {

    }

    @Test
    public void shutdown() {

    }

    @Test
    public void isEditing() {
        // arrange
        IPlayer player = mock(IPlayer.class);
        String invName = "MyInventory";
        InventoryTrigger trigger = mock(InventoryTrigger.class);
        TriggerInfo triggerInfo = mock(TriggerInfo.class);
        IItemStack[] items = new IItemStack[]{
                mock(IItemStack.class),
        };
        IInventory inventory = mock(IInventory.class);

        when(inventoryTriggerManager.get(invName)).thenReturn(trigger);
        when(trigger.getInfo()).thenReturn(triggerInfo);
        when(trigger.getItems()).thenReturn(items);
        when(inventoryHandle.createInventory(anyInt(), any())).thenReturn(inventory);

        // act
        inventoryEditManager.startEdit(player, invName);
        boolean result = inventoryEditManager.isEditing(player);

        // assert
        assertTrue(result);
    }

    @Test
    public void continueEdit() {
        // arrange
        IPlayer player = mock(IPlayer.class);
        String invName = "MyInventory";
        InventoryTrigger trigger = mock(InventoryTrigger.class);
        TriggerInfo triggerInfo = mock(TriggerInfo.class);
        IItemStack[] items = new IItemStack[]{
                mock(IItemStack.class),
        };
        IInventory inventory = mock(IInventory.class);

        when(player.getName()).thenReturn("SomePlayer");
        when(inventoryTriggerManager.get(invName)).thenReturn(trigger);
        when(trigger.getInfo()).thenReturn(triggerInfo);
        when(trigger.getItems()).thenReturn(items);
        when(inventoryHandle.createInventory(anyInt(), any())).thenReturn(inventory);

        // act
        inventoryEditManager.startEdit(player, invName);
        inventoryEditManager.onInventoryClose(player, inventory);
        inventoryEditManager.continueEdit(player);

        // assert
        assertTrue(inventoryEditManager.isEditing(player));
    }

    @Test
    public void discardEdit() {
        // arrange
        IPlayer player = mock(IPlayer.class);
        String invName = "MyInventory";
        InventoryTrigger trigger = mock(InventoryTrigger.class);
        TriggerInfo triggerInfo = mock(TriggerInfo.class);
        IItemStack[] items = new IItemStack[]{
                mock(IItemStack.class),
        };
        IInventory inventory = mock(IInventory.class);

        when(player.getName()).thenReturn("SomePlayer");
        when(inventoryTriggerManager.get(invName)).thenReturn(trigger);
        when(trigger.getInfo()).thenReturn(triggerInfo);
        when(trigger.getItems()).thenReturn(items);
        when(inventoryHandle.createInventory(anyInt(), any())).thenReturn(inventory);

        // act
        inventoryEditManager.startEdit(player, invName);
        inventoryEditManager.onInventoryClose(player, inventory);
        inventoryEditManager.discardEdit(player);

        // assert
        assertFalse(inventoryEditManager.isEditing(player));
    }

    @Test
    public void saveEdit() {
        // arrange
        IPlayer player = mock(IPlayer.class);
        String invName = "MyInventory";
        InventoryTrigger trigger = mock(InventoryTrigger.class);
        TriggerInfo triggerInfo = mock(TriggerInfo.class);
        IItemStack[] items = new IItemStack[]{
                mock(IItemStack.class),
        };
        IInventory inventory = mock(IInventory.class);

        when(player.getName()).thenReturn("SomePlayer");
        when(inventoryTriggerManager.get(invName)).thenReturn(trigger);
        when(trigger.getInfo()).thenReturn(triggerInfo);
        when(trigger.getItems()).thenReturn(items);
        when(inventoryHandle.createInventory(anyInt(), any())).thenReturn(inventory);
        when(inventoryHandle.getContents(any())).thenReturn(items);

        // act
        inventoryEditManager.startEdit(player, invName);
        inventoryEditManager.saveEdit(player);

        // assert
        verify(inventoryTriggerLoader).save(trigger);
    }

    private static class ItemStack {

    }
}