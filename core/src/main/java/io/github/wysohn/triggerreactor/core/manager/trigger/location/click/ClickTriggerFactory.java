package io.github.wysohn.triggerreactor.core.manager.trigger.location.click;

import dagger.assisted.AssistedFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.LocationTriggerFactory;

@AssistedFactory
public interface ClickTriggerFactory extends LocationTriggerFactory<ClickTrigger> {
    ClickTrigger create(TriggerInfo info, String script);
}
