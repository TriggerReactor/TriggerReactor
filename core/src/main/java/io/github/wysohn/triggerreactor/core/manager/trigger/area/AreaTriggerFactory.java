package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import dagger.assisted.AssistedFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import java.io.File;

@AssistedFactory
public interface AreaTriggerFactory {
    AreaTrigger create(TriggerInfo info, File folder);
}