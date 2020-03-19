package io.github.wysohn.triggerreactor.core.manager.trigger.named;

import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;

import java.io.File;

class NamedTrigger extends AbstractTriggerManager.Trigger {

    public NamedTrigger(String name, File file, String script) throws AbstractTriggerManager.TriggerInitFailedException {
        super(name, file, script);

        init();
    }

    @Override
    public AbstractTriggerManager.Trigger clone() {
        try {
            return new NamedTrigger(triggerName, file, getScript());
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
