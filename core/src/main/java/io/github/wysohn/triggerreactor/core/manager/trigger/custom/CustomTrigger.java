package io.github.wysohn.triggerreactor.core.manager.trigger.custom;

import io.github.wysohn.triggerreactor.core.IEventHook;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import java.util.HashMap;
import java.util.Map;

public class CustomTrigger extends Trigger implements IEventHook {
    final Class<?> event;
    final String eventName;

    public CustomTrigger(TriggerInfo info, String script, Class<?> event, String eventName) throws AbstractTriggerManager.TriggerInitFailedException {
        super(info, script);
        this.event = event;
        this.eventName = eventName;

        init();
    }

    @Override
    public CustomTrigger clone() {
        try {
            return new CustomTrigger(info, script, event, eventName);
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + "{" +
                "event=" + (event == null ? null : event.getName()) +
                '}';
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getInfo() == null) ? 0 : getInfo().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomTrigger other = (CustomTrigger) obj;
        if (getInfo() == null) {
            return other.getInfo() == null;
        } else return getInfo().equals(other.getInfo());
    }

    public String getEventName() {
        return eventName;
    }

    @Override
    public void onEvent(Object e) {
        if (e.getClass() != event
                // temporary way to deal with sponge events
                && !e.getClass().getSimpleName().contains(event.getSimpleName()))
            return;

        Map<String, Object> vars = new HashMap<>();
        this.activate(e, vars);
    }
}
