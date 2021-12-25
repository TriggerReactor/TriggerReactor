package io.github.wysohn.triggerreactor.core.manager.trigger.inventory;

import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.main.ITriggerReactorAPI;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import java.util.Map;

public class InventoryTrigger extends Trigger {
    public static final int MAXSIZE = 6 * 9;

    final IItemStack[] items;

    public InventoryTrigger(ITriggerReactorAPI api,
                            TriggerInfo info,
                            String script,
                            IItemStack[] items) throws AbstractTriggerManager.TriggerInitFailedException {
        super(api, info, script);
        this.items = items;

        init();
    }

    public InventoryTrigger(ITriggerReactorAPI api,
                            TriggerInfo info,
                            String script,
                            int size,
                            Map<Integer, IItemStack> items) throws AbstractTriggerManager.TriggerInitFailedException {
        super(api, info, script);
        if (size < 9 || size % 9 != 0)
            throw new IllegalArgumentException("Inventory Trigger size should be multiple of 9!");

        if (size > MAXSIZE) throw new IllegalArgumentException("Inventory Size cannot be larger than " + MAXSIZE);

        this.items = new IItemStack[size];

        for (Map.Entry<Integer, IItemStack> entry : items.entrySet()) {
            this.items[entry.getKey()] = entry.getValue();
        }

        init();
    }

    @Override
    public InventoryTrigger clone() {
        try {
            return new InventoryTrigger(api, getInfo(), getScript(), items.clone());
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void start(Timings.Timing timing,
                         Object e,
                         Map<String, Object> scriptVars,
                         Interpreter interpreter,
                         boolean sync) {
        try {
            interpreter.startWithContextAndInterrupter(e,
                                                       api.getGameController()
                                                               .createInterrupterForInv(cooldowns,
                                                                                        AbstractInventoryTriggerManager.inventoryMap),
                                                       timing);
        } catch (Exception ex) {
            api.getThrowableHandler()
                    .handleException(e,
                                     new Exception("Error occurred while processing Trigger [" + getInfo() + "]!", ex));
        }
    }

    public IItemStack[] getItems() {
        return items;
    }
}
