package io.github.wysohn.triggerreactor.core.manager.trigger.custom;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.IEventRegistry;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.IOException;

class CustomTriggerLoader implements ITriggerLoader<CustomTrigger> {
    private final IEventRegistry registry;

    public CustomTriggerLoader(IEventRegistry registry) {
        this.registry = registry;
    }

    @Override
    public CustomTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        String eventName = info.get(TriggerConfigKey.KEY_TRIGGER_CUSTOM_EVENT, String.class)
                .filter(registry::eventExist)
                .orElseThrow(() -> new InvalidTrgConfigurationException(
                        "Couldn't find target Event or is not a valid Event",
                        info));

        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            return new CustomTrigger(info, script, registry.getEvent(eventName), eventName);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(CustomTrigger trigger) {
        try {
            FileUtil.writeToFile(trigger.getInfo().getSourceCodeFile(), trigger.getScript());

            trigger.getInfo().put(TriggerConfigKey.KEY_TRIGGER_CUSTOM_EVENT, trigger.getEventName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
