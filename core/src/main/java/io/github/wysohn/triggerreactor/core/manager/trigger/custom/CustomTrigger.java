package io.github.wysohn.triggerreactor.core.manager.trigger.custom;

import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomTrigger extends Trigger implements AbstractCustomTriggerManager.EventHook {
    final Class<?> event;
    final String eventName;

    /**
     * @param event
     * @param name
     * @param script
     * @throws IOException     {@link Trigger#init()}
     * @throws LexerException  {@link Trigger#init()}
     * @throws ParserException {@link Trigger#init()}
     */
    public CustomTrigger(Class<?> event, String eventName, String name, File file, String script) throws AbstractTriggerManager.TriggerInitFailedException {
        super(name, file, script);
        this.event = event;
        this.eventName = eventName;

        init();
    }

    @Override
    public CustomTrigger clone() {
        try {
            return new CustomTrigger(event, getEventName(), triggerName, file, this.getScript());
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
        result = prime * result + ((triggerName == null) ? 0 : triggerName.hashCode());
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
        if (triggerName == null) {
            return other.triggerName == null;
        } else return triggerName.equals(other.triggerName);
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
