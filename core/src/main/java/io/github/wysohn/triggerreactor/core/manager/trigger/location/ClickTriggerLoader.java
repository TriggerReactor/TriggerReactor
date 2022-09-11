package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.IOException;

class ClickTriggerLoader implements ITriggerLoader<ClickTrigger> {
    @Override
    public ClickTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            return newInstance(info, script);
        } catch (AbstractTriggerManager.TriggerInitFailedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(ClickTrigger trigger) {
        try {
            FileUtil.writeToFile(trigger.getInfo().getSourceCodeFile(), trigger.getScript());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ClickTrigger newInstance(TriggerInfo info, String script) throws
            AbstractTriggerManager.TriggerInitFailedException {
        return new ClickTrigger(info, script, activity ->
                activity == Activity.LEFT_CLICK_BLOCK
                        || activity == Activity.RIGHT_CLICK_BLOCK);
    }
}
