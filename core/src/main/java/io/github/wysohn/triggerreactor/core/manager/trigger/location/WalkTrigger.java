package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

public class WalkTrigger extends Trigger {
    public WalkTrigger(TriggerInfo info, String script) throws AbstractTriggerManager.TriggerInitFailedException {
        super(info, script);

        init();

    }

    @Override
    public Trigger clone() {
        try {
            return new WalkTrigger(info, script);
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
