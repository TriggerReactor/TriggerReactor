package io.github.wysohn.triggerreactor.core.manager.trigger.named;

import dagger.assisted.AssistedFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

@AssistedFactory
public interface NamedTriggerFactory {
    NamedTrigger create(TriggerInfo info, String script);
}
