package io.github.wysohn.triggerreactor.core.manager.trigger.inventory;

import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.IInventoryHandle;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InventoryTriggerLoader<ItemStack> implements ITriggerLoader<InventoryTrigger> {
    private final IInventoryHandle<ItemStack> inventoryHandle;

    public InventoryTriggerLoader(IInventoryHandle<ItemStack> inventoryHandle) {
        this.inventoryHandle = inventoryHandle;
    }

    @Override
    public InventoryTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        int size = info.get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_SIZE, Integer.class)
                .filter(s -> s != 0 && s % 9 == 0)
                .filter(s -> s <= InventoryTrigger.MAXSIZE)
                .orElseThrow(() -> new InvalidTrgConfigurationException("Couldn't find or invalid Size", info));
        Map<Integer, IItemStack> items = new HashMap<>();

        if (info.has(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS)) {
            if (!info.isSection(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS)) {
                throw new InvalidTrgConfigurationException("Items should be an object", info);
            }

            for (int i = 0; i < size; i++) {
                final int itemIndex = i;
                info.get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS, i, inventoryHandle.getItemClass())
                        .ifPresent(item -> items.put(itemIndex, inventoryHandle.wrapItemStack(item)));
            }
        }

        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            IItemStack[] itemArray = new IItemStack[size];
            for (int i = 0; i < size; i++)
                itemArray[i] = items.getOrDefault(i, null);
            return new InventoryTrigger(info, script, itemArray);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(InventoryTrigger trigger) {
        try {
            FileUtil.writeToFile(trigger.getInfo().getSourceCodeFile(), trigger.getScript());

            IItemStack[] items = trigger.items;
            int size = trigger.items.length;

            trigger.getInfo().put(TriggerConfigKey.KEY_TRIGGER_INVENTORY_SIZE, size);
            trigger.getInfo()
                    .put(TriggerConfigKey.KEY_TRIGGER_INVENTORY_TITLE, trigger.getInfo().getTriggerName());
            for (int i = 0; i < items.length; i++) {
                IItemStack item = items[i];
                if (item == null)
                    continue;

                trigger.getInfo().put(TriggerConfigKey.KEY_TRIGGER_INVENTORY_ITEMS, i, item.get());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
