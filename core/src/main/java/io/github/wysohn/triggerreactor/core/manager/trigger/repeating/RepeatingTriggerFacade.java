package io.github.wysohn.triggerreactor.core.manager.trigger.repeating;

import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerFacade;

public class RepeatingTriggerFacade extends TriggerFacade {
    public RepeatingTriggerFacade(RepeatingTrigger trigger) {
        super(trigger);
    }

    public boolean isAutoStart() {
        return ((RepeatingTrigger) trigger).isAutoStart();
    }

    public long getInterval() {
        return ((RepeatingTrigger) trigger).getInterval();
    }

    public void setInterval(long interval) {
        ((RepeatingTrigger) trigger).setInterval(interval);
    }


    public boolean isPause() {
        return ((RepeatingTrigger) trigger).isPaused();
    }

    public void setPause(boolean pause) {
        ((RepeatingTrigger) trigger).setPaused(pause);
    }
}
