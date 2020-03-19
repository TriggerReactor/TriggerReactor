package io.github.wysohn.triggerreactor.core.manager.trigger.inventory;

import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import java.io.File;
import java.util.Map;

public class InventoryTrigger extends AbstractTriggerManager.Trigger {
    public static final int MAXSIZE = 6 * 9;

    final IItemStack[] items;

    private InventoryTrigger(String name, String script, File file, IItemStack[] items) throws AbstractTriggerManager.TriggerInitFailedException {
        super(name, file, script);
        this.items = items;

        init();
    }

    public InventoryTrigger(int size, String name, Map<Integer, IItemStack> items, File file, String script) throws AbstractTriggerManager.TriggerInitFailedException {
        super(name, file, script);
        if (size < 9 || size % 9 != 0)
            throw new IllegalArgumentException("Inventory Trigger size should be multiple of 9!");

        if (size > MAXSIZE)
            throw new IllegalArgumentException("Inventory Size cannot be larger than " + MAXSIZE);

        this.items = new IItemStack[size];

        for (Map.Entry<Integer, IItemStack> entry : items.entrySet()) {
            this.items[entry.getKey()] = entry.getValue();
        }

        init();
    }

    @Override
    protected void start(Timings.Timing timing, Object e, Map<String, Object> scriptVars, Interpreter interpreter,
                         boolean sync) {
        try {
            interpreter.startWithContextAndInterrupter(e,
                    TriggerReactorCore.getInstance().createInterrupterForInv(e, interpreter, cooldowns, AbstractInventoryTriggerManager.inventoryMap),
                    timing);
        } catch (Exception ex) {
            TriggerReactorCore.getInstance().handleException(e,
                    new Exception("Error occurred while processing Trigger [" + getTriggerName() + "]!", ex));
        }
    }

    @Override
    public AbstractTriggerManager.Trigger clone() {
        try {
            return new InventoryTrigger(triggerName, script, file, items);
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public IItemStack[] getItems() {
        return items;
    }
}
