package io.github.wysohn.triggerreactor.core.manager.trigger.location.walk;

import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.LocationTriggerFactory;

public interface WalkTriggerFactory extends LocationTriggerFactory<WalkTrigger> {
    WalkTrigger create(TriggerInfo info, String script);
}
