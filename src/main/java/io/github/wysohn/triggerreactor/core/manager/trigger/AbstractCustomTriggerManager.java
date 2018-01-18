/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.TriggerManager;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;

public abstract class AbstractCustomTriggerManager extends TriggerManager {

    protected final Map<Class<?>, Set<CustomTrigger>> triggerMap = new ConcurrentHashMap<>();

    protected abstract void handleEvent(Object e);

    protected abstract Class<?> getEventFromName(String name) throws ClassNotFoundException;

    protected final Map<String, CustomTrigger> nameMap = new ConcurrentHashMap<>();

    public static class CustomTrigger extends Trigger{
        final Class<?> event;
        private final String eventName;

        /**
         *
         * @param event
         * @param name
         * @param script
         * @throws IOException {@link Trigger#init()}
         * @throws LexerException {@link Trigger#init()}
         * @throws ParserException {@link Trigger#init()}
         */
        public CustomTrigger(Class<?> event, String eventName, String name, String script) throws TriggerInitFailedException {
            super(name, script);
            this.event = event;
            this.eventName = eventName;

            init();
        }

        @Override
        public Trigger clone() {
            try {
                return new CustomTrigger(event, getEventName(), triggerName, this.getScript());
            } catch (TriggerInitFailedException e) {
                e.printStackTrace();
            }
            return null;
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
                if (other.triggerName != null)
                    return false;
            } else if (!triggerName.equals(other.triggerName))
                return false;
            return true;
        }

        public String getEventName() {
            return eventName;
        }
    }

    /**
     * Try to get set of Triggers associated with the event. It creates and puts new empty Set
     *  if couldn't find existing one already, and register it so can handle the event.
     * @param event any event that extends Event (and HandlerList of course)
     * @return Set of CustomTriggers associated with the event
     */
    protected Set<CustomTrigger> getTriggerSetForEvent(Class<?> event) {
        Set<CustomTrigger> triggers = triggerMap.get(event);
        if(triggers == null){
            //this will allow TriggerReactor to hook events from other plugins as well.
            registerEvent(plugin, event);

            triggers = new HashSet<>();
            triggerMap.put(event, triggers);
        }
        return triggers;
    }

    protected abstract void registerEvent(TriggerReactor plugin, Class<?> clazz);

    /**
     * Create a new CustomTrigger.
     *
     * @param eventName
     *            the class name of the Event that this Custom Trigger will
     *            handle.
     * @param name name of trigger (unique)
     * @param script the script
     * @return true if created; false if trigger with the 'name' already exists.
     * @throws ClassNotFoundException
     *             throws if className is not in abbreviation list, not a valid
     *             class name, or the specified event is not a valid event to handle.
     * @throws ParserException
     * @throws LexerException
     * @throws IOException
     */
    public boolean createCustomTrigger(String eventName, String name, String script)
            throws ClassNotFoundException, TriggerInitFailedException {
                if(nameMap.containsKey(name))
                    return false;

                Class<?> event = this.getEventFromName(eventName);

                Set<CustomTrigger> triggers = this.getTriggerSetForEvent(event);

                CustomTrigger trigger = new CustomTrigger(event, eventName, name, script);

                triggers.add(trigger);
                nameMap.put(name, trigger);

                return true;
            }

    /**
     * Find and return Custom Trigger with the 'name'
     * @param name
     * @return null if no such trigger with that name; CustomTrigger if found
     */
    public CustomTrigger getTriggerForName(String name) {
        return nameMap.get(name);
    }

    /**
     * get set of triggers associated with 'className' Event
     * @param eventName the abbreviation, simple event name, or full event name. See {@link #getEventFromName(String)}
     * @return set of triggers; null if nothing is registered with the 'className'
     * @throws ClassNotFoundException See {@link #getEventFromName(String)}
     */
    public Set<CustomTrigger> getTriggersForEvent(String eventName) throws ClassNotFoundException {
        Class<?> clazz = this.getEventFromName(eventName);

        return triggerMap.get(clazz);
    }

    /**
     * Delete the Custom Trigger with 'name'
     * @param name
     * @return false if no such trigger exists with 'name'; true if deleted
     */
    public boolean removeTriggerForName(String name) {
        if(!nameMap.containsKey(name))
            return false;

        CustomTrigger trigger = nameMap.remove(name);
        Set<CustomTrigger> triggers = triggerMap.get(trigger.event);
        if(triggers != null){
            triggers.remove(trigger);
        }

        deleteInfo(trigger);

        return true;
    }

    public AbstractCustomTriggerManager(TriggerReactor plugin) {
        super(plugin);
    }

}