package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import io.github.wysohn.triggerreactor.core.config.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import java.io.File;

public class AreaTriggerInfo extends TriggerInfo {
    public AreaTriggerInfo(File sourceCodeFile, IConfigSource config) {
        super(sourceCodeFile, config);
    }

    @Override
    public boolean isValid() {
        // unlike other triggers, AreaTrigger create a folder and put Enter.trg and Exit.trg in it.
        return getSourceCodeFile().isDirectory();
    }
}
