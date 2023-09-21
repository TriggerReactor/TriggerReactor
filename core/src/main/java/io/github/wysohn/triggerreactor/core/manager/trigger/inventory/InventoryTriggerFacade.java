package io.github.wysohn.triggerreactor.core.manager.trigger.inventory;

import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerFacade;

public class InventoryTriggerFacade extends TriggerFacade {
    public InventoryTriggerFacade(InventoryTrigger trigger) {
        super(trigger);
    }

    public IItemStack[] getItems() {
        return ((InventoryTrigger) trigger).getItems();
    }

    public boolean canPickup() {
        return ((InventoryTrigger) trigger).canPickup();
    }

    public String getTitle() {
        return ((InventoryTrigger) trigger).getTitle();
    }

}
