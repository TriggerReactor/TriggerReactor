package io.github.wysohn.triggerreactor.core.manager.trigger.inventory;

import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.IInventoryHandle;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class InventoryTriggerLoaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    TriggerReactorCore core;
    IInventoryHandle<ItemStack> handle;
    InventoryTriggerLoader<ItemStack> loader;

    @Before
    public void setUp() throws Exception {
        core = mock(TriggerReactorCore.class, RETURNS_DEEP_STUBS);
        Field instanceField = TriggerReactorCore.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, core);

        when(core.getExecutorManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getPlaceholderManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getVariableManager().getGlobalVariableAdapter()).thenReturn(new HashMap<>());

        handle = mock(IInventoryHandle.class);
        when(handle.getItemClass()).thenReturn(ItemStack.class);

        loader = new InventoryTriggerLoader<>(handle);
    }

    @Test
    public void load() throws IOException, InvalidTrgConfigurationException {
        TriggerInfo info = mock(TriggerInfo.class);

        when(info.getTriggerName()).thenReturn("test");
        when(info.getSourceCodeFile()).thenReturn(folder.newFile("test.trg"));
        when(info.get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_SIZE, Integer.class)).thenReturn(Optional.of(9));
        when(info.get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_TITLE,
                      String.class)).thenReturn(Optional.of("test title"));
        when(info.has(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS)).thenReturn(true);
        when(info.isSection(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS)).thenReturn(true);
        for (int i = 0; i < 9; i++) {
            when(info.get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS,
                          i,
                          ItemStack.class)).thenReturn(Optional.of(mock(ItemStack.class)));
        }

        InventoryTrigger trigger = loader.load(info);

        assertNotNull(trigger);
        verify(handle, times(9)).getItemClass();
        verify(handle, times(9)).wrapItemStack(any());
    }

    @Test
    public void save() throws AbstractTriggerManager.TriggerInitFailedException, IOException {
        ItemStack item = new ItemStack();
        IItemStack mockItem = mock(IItemStack.class);
        TriggerInfo info = mock(TriggerInfo.class);
        InventoryTrigger trigger = new InventoryTrigger(info,
                                                        "test",
                                                        new IItemStack[]{mockItem,
                                                                         null,
                                                                         null,
                                                                         null,
                                                                         null,
                                                                         null,
                                                                         null,
                                                                         null,
                                                                         null});

        when(mockItem.get()).thenReturn(item);
        when(info.getSourceCodeFile()).thenReturn(folder.newFile("test.trg"));
        when(info.get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_SIZE, Integer.class)).thenReturn(Optional.of(9));
        when(info.get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_TITLE,
                      String.class)).thenReturn(Optional.of("test title"));
        when(info.has(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS)).thenReturn(true);
        when(info.isSection(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS)).thenReturn(true);
        when(info.getTriggerName()).thenReturn("title");

        loader.save(trigger);

        verify(info).put(TriggerConfigKey.KEY_TRIGGER_INVENTORY_SIZE, 9);
        verify(info).put(TriggerConfigKey.KEY_TRIGGER_INVENTORY_TITLE, "title");
        verify(info).put(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS, 0, item);
        for (int i = 1; i < 9; i++) {
            verify(info, never()).put(eq(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS), eq(i), any());
        }

    }

    public static class ItemStack {

    }
}