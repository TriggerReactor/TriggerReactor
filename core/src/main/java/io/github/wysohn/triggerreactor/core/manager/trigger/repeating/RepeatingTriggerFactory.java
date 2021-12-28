package io.github.wysohn.triggerreactor.core.manager.trigger.repeating;

import dagger.assisted.AssistedFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

@AssistedFactory
public interface RepeatingTriggerFactory {
    RepeatingTrigger create(TriggerInfo info, String script, long interval);
}
