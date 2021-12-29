package io.github.wysohn.triggerreactor.core.manager.trigger.custom;


import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

import java.util.HashMap;
import java.util.Map;

public class CustomTrigger extends Trigger implements CustomTriggerManager.EventHook {
    final Class<?> event;
    final String eventName;

    @AssistedInject
    CustomTrigger(@Assisted TriggerInfo info,
                  @Assisted String script,
                  @Assisted Class<?> event,
                  @Assisted("eventName") String eventName) {
        super(info, script);
        this.event = event;
        this.eventName = eventName;

    }

    public CustomTrigger(Trigger o) {
        super(o);
        ValidationUtil.assertTrue(o, v -> v instanceof CustomTrigger);
        CustomTrigger other = (CustomTrigger) o;

        this.event = other.event;
        this.eventName = other.eventName;
    }

    @Override
    public String toString() {
        return super.toString() + "{event=" + (event == null ? null : event.getName()) + '}';
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
        } else
            return getInfo().equals(other.getInfo());
    }

    @Override
    public void onEvent(Object e) {
        if (e.getClass() != event
                // temporary way to deal with sponge events
                && !e.getClass().getSimpleName().contains(event.getSimpleName()))
            return;

        Map<String, Object> vars = new HashMap<>();
        vars.put(Trigger.VAR_NAME_EVENT, e);
        this.activate(vars);
    }

    public String getEventName() {
        return eventName;
    }
}
