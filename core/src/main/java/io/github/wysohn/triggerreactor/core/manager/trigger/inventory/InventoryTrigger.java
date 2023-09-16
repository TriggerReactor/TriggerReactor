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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.interpreter.interrupt.ProcessInterrupter;

public class InventoryTrigger extends Trigger {
    @Inject
    private IInventoryTriggerFactory factory;

    @Inject
    private IPluginManagement pluginManagement;
    @Inject
    private IExceptionHandle exceptionHandle;

    public static final int MAXSIZE = 6 * 9;

    private final IItemStack[] items;

    @Inject
    private InventoryTrigger(@Assisted TriggerInfo info,
                             @Assisted String script,
                             @Assisted IItemStack[] items) throws AbstractTriggerManager.TriggerInitFailedException {
        super(info, script);
        this.items = items;
    }

//    public InventoryTrigger(@Assisted TriggerInfo info,
//                            @Assisted String script,
//                            @Assisted int size,
//                            @Assisted Map<Integer, IItemStack> items) throws
//            AbstractTriggerManager.TriggerInitFailedException {
//        super(info, script);
//        if (size < 9 || size % 9 != 0)
//            throw new IllegalArgumentException("Inventory Trigger size should be multiple of 9!");
//
//        if (size > MAXSIZE)
//            throw new IllegalArgumentException("Inventory Size cannot be larger than " + MAXSIZE);
//
//        this.items = new IItemStack[size];
//
//        for (Map.Entry<Integer, IItemStack> entry : items.entrySet()) {
//            this.items[entry.getKey()] = entry.getValue();
//        }
//    }

    @Override
    protected ProcessInterrupter createInterrupter() {
        return pluginManagement.createInterrupterForInv(cooldowns, InventoryTriggerManager.inventoryMap);
    }

    @Override
    public InventoryTrigger clone() {
        return factory.create(getInfo(), getScript(), items.clone());
    }

    public IItemStack[] getItems() {
        return items;
    }

    public boolean canPickup() {
        return getInfo().get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_PICKUP, Boolean.class)
                .orElse(false);
    }

    public String getTitle() {
        return getInfo().get(TriggerConfigKey.KEY_TRIGGER_INVENTORY_TITLE, String.class)
                .orElse(null);
    }
}
