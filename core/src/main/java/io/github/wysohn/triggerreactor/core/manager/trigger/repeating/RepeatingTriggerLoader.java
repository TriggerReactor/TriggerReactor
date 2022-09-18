package io.github.wysohn.triggerreactor.core.manager.trigger.repeating;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.IOException;

class RepeatingTriggerLoader implements ITriggerLoader<RepeatingTrigger> {
    @Override
    public RepeatingTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        boolean autoStart = info.get(TriggerConfigKey.KEY_TRIGGER_REPEATING_AUTOSTART, Boolean.class)
                .orElse(false);
        int interval = info.get(TriggerConfigKey.KEY_TRIGGER_REPEATING_INTERVAL, Integer.class).orElse(1000);

        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            RepeatingTrigger trigger = new RepeatingTrigger(info, script);
            trigger.setAutoStart(autoStart);
            trigger.setInterval(interval);
            return trigger;
        } catch (AbstractTriggerManager.TriggerInitFailedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(RepeatingTrigger trigger) {
        try {
            FileUtil.writeToFile(trigger.getInfo().getSourceCodeFile(), trigger.getScript());

            trigger.getInfo().put(TriggerConfigKey.KEY_TRIGGER_REPEATING_AUTOSTART, trigger.isAutoStart());
            trigger.getInfo().put(TriggerConfigKey.KEY_TRIGGER_REPEATING_INTERVAL, trigger.getInterval());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
