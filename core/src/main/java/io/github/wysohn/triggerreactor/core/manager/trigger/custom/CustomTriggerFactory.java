package io.github.wysohn.triggerreactor.core.manager.trigger.custom;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

@AssistedFactory
public interface CustomTriggerFactory {
    CustomTrigger create(TriggerInfo info, String script, Class<?> event, @Assisted("eventName") String eventName);
}
