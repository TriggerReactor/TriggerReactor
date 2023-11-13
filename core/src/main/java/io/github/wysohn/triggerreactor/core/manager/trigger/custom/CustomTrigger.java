/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.manager.trigger.custom;

import com.google.inject.assistedinject.Assisted;
import io.github.wysohn.triggerreactor.core.IEventHook;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class CustomTrigger extends Trigger implements IEventHook {
    @Inject
    private ICustomTriggerFactory factory;

    private final Class<?> event;
    private final String eventName;

    @Inject
    private CustomTrigger(@Assisted TriggerInfo info,
                          @Assisted String script,
                          @Assisted Class<?> event,
                          @Assisted("CustomTriggerEventName") String eventName)
            throws AbstractTriggerManager.TriggerInitFailedException {
        super(info, script);
        this.event = event;
        this.eventName = eventName;
    }

    @Override
    public CustomTrigger clone() {
        return factory.create(info, script, event, eventName);
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

    public Class<?> getEvent() {
        return event;
    }

    public String getEventName() {
        return eventName;
    }

    public int getPriority() {
        return getInfo().get(TriggerConfigKey.KEY_TRIGGER_CUSTOM_PRIORITY, Integer.class).orElse(-1);
    }

    /**
     * Priority of this trigger. Lesser value means it will handle the event earlier. The specific mapping of the
     * value depends on the implementation.
     *
     * @param priority new priority.
     */
    public void setPriority(int priority) {
        getInfo().put(TriggerConfigKey.KEY_TRIGGER_CUSTOM_PRIORITY, priority);
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
