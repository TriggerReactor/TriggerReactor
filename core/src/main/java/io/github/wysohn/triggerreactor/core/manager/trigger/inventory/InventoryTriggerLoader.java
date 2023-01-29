/*
 * Copyright (C) 2022. TriggerReactor Team
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

import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.IInventoryHandle;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class InventoryTriggerLoader<ItemStack> implements ITriggerLoader<InventoryTrigger> {
    @Inject
    private IInventoryTriggerFactory factory;
    @Inject
    private IInventoryHandle<ItemStack> inventoryHandle;

    @Inject
    private InventoryTriggerLoader() {

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
            return factory.create(info, script, itemArray);
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
