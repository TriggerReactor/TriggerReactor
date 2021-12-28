package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import dagger.assisted.AssistedFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

@AssistedFactory
public interface CommandTriggerFactory {
    CommandTrigger create(TriggerInfo info, String script);
}
