package io.github.wysohn.triggerreactor.core.manager.trigger.inventory;

import dagger.assisted.AssistedFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

@AssistedFactory
public interface InventoryTriggerFactory {
    InventoryTrigger create(TriggerInfo info, String script);
}
