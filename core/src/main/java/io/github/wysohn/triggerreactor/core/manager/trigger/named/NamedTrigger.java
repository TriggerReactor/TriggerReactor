package io.github.wysohn.triggerreactor.core.manager.trigger.named;

import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

public class NamedTrigger extends Trigger {
    public NamedTrigger(TriggerInfo info, String script) throws AbstractTriggerManager.TriggerInitFailedException {
        super(info, script);

        init();
    }

    @Override
    public NamedTrigger clone() {
        try {
            return new NamedTrigger(info, script);
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
