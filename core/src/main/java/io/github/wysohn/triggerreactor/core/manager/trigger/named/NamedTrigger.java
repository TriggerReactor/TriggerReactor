package io.github.wysohn.triggerreactor.core.manager.trigger.named;

import io.github.wysohn.triggerreactor.core.main.ITriggerReactorAPI;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

public class NamedTrigger extends Trigger {
    public NamedTrigger(ITriggerReactorAPI api,
                        TriggerInfo info,
                        String script) throws AbstractTriggerManager.TriggerInitFailedException {
        super(api, info, script);

        init();
    }

    @Override
    public NamedTrigger clone() {
        try {
            return new NamedTrigger(api, info, script);
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
