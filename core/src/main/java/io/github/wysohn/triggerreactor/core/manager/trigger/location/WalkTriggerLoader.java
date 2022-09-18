package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.IOException;

class WalkTriggerLoader implements ITriggerLoader<WalkTrigger> {
    @Override
    public WalkTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            return new WalkTrigger(info, script);
        } catch (AbstractTriggerManager.TriggerInitFailedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(WalkTrigger trigger) {
        try {
            FileUtil.writeToFile(trigger.getInfo().getSourceCodeFile(), trigger.getScript());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
