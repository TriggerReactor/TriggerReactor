package io.github.wysohn.triggerreactor.core.manager.trigger.custom;

import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerFacade;

public class CustomTriggerFacade extends TriggerFacade {
    public CustomTriggerFacade(CustomTrigger trigger) {
        super(trigger);
    }

    public Class<?> getEvent() {
        return ((CustomTrigger) trigger).getEvent();
    }

    public String getEventName() {
        return ((CustomTrigger) trigger).getEventName();
    }
}
