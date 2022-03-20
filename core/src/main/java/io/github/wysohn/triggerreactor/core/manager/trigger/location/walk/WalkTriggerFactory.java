package io.github.wysohn.triggerreactor.core.manager.trigger.location.walk;

import dagger.assisted.AssistedFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.LocationTriggerFactory;

@AssistedFactory
public interface WalkTriggerFactory extends LocationTriggerFactory<WalkTrigger> {
    WalkTrigger create(TriggerInfo info, String script);
}
