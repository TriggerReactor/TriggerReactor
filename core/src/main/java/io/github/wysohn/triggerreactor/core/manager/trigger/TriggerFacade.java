package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.util.Map;

public class TriggerFacade {

    protected final Trigger trigger;

    public TriggerFacade(Trigger trigger) {
        this.trigger = trigger;
    }

    public TriggerInfo getInfo() {
        return trigger.getInfo();
    }

    public String getScript() {
        return trigger.getScript();
    }

    public synchronized Map<String, Object> getVarCopy() {
        return trigger.getVarCopy();
    }

    public Trigger triggerCopy() {
        return trigger.clone();
    }

    @Override
    public String toString() {
        return trigger.toString();
    }
}
