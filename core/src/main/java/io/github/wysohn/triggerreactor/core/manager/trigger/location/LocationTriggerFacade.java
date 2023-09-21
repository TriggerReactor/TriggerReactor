package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerFacade;

public class LocationTriggerFacade extends TriggerFacade {
    public LocationTriggerFacade(Trigger trigger) {
        super(trigger);
    }

    public String getTriggerTypeName() {
        if (trigger instanceof ClickTrigger) {
            return "Click";
        } else if (trigger instanceof WalkTrigger) {
            return "Walk";
        } else {
            return "";
        }
    }

    public SimpleLocation getLocation() {
        return SimpleLocation.valueOf(trigger.getInfo().getTriggerName());
    }
}
