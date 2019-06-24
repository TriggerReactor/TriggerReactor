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

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCustomTriggerManager extends AbstractTriggerManager {

    private static final String EVENT = "Event";
    private static final String SYNC = "Sync";
    private final Map<String, CustomTrigger> nameMap = new ConcurrentHashMap<>();

    @Override
    public void reload() {
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".yml");
            }
        };

        nameMap.clear();

        for (File ymlfile : folder.listFiles(filter)) {
            if (!ymlfile.isFile())
                continue;

            String triggerName = extractName(ymlfile);

            String eventName = null;
            boolean isSync = false;
            try {
                eventName = this.getData(ymlfile, EVENT);
                isSync = this.getData(ymlfile, SYNC, false);
            } catch (IOException e2) {
                e2.printStackTrace();
                continue;
            }

            if (eventName == null) {
                plugin.getLogger().warning("Could not find Event: for " + ymlfile);
                continue;
            }

            Class<?> event = null;
            try {
                event = getEventFromName(eventName);
            } catch (ClassNotFoundException e1) {
                plugin.getLogger().warning("Could not load " + ymlfile);
                plugin.getLogger().warning(e1.getMessage() + " does not exist.");
                continue;
            }

            File triggerFile = getTriggerFile(folder, triggerName, false);

            try {
                String read = FileUtil.readFromFile(triggerFile);

                //Set<CustomTrigger> triggers = getTriggerSetForEvent(event);

                try {
                    CustomTrigger trigger = new CustomTrigger(event, eventName, triggerName, triggerFile, read);
                    trigger.setSync(isSync);

                    nameMap.put(triggerName, trigger);

                    registerEvent(plugin, event, trigger);
                } catch (TriggerInitFailedException e) {
                    e.printStackTrace();
                    continue;
                }
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    @Override
    public void saveAll() {
        for (Entry<String, CustomTrigger> entry : nameMap.entrySet()) {
            CustomTrigger trigger = entry.getValue();

            File triggerfile = getTriggerFile(folder, trigger.getTriggerName(), true);
            File ymlfile = new File(folder, trigger.getTriggerName() + ".yml");
            try {
                if (!triggerfile.exists())
                    triggerfile.createNewFile();
                if (!ymlfile.exists())
                    ymlfile.createNewFile();
            } catch (IOException e2) {
                e2.printStackTrace();
            }

            try {
                this.setData(ymlfile, SYNC, trigger.isSync());
                this.setData(ymlfile, EVENT, trigger.getEventName());
            } catch (IOException e1) {
                e1.printStackTrace();
                continue;
            }

            try {
                FileUtil.writeToFile(triggerfile, trigger.getScript());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    /**
//     * Try to get set of Triggers associated with the event. It creates and puts new empty Set
//     *  if couldn't find existing one already, and register it so can handle the event.
//     * @param eventname full name of the event
//     * @return Set of CustomTriggers associated with the event
//     */
//    protected Set<CustomTrigger> getTriggerSetForEvent(String eventname) {
//        Class<?> event;
//        try {
//            event = Class.forName(eventname);
//        } catch (ClassNotFoundException e) {
//            return new HashSet<>();
//        }
//
//        return getTriggerSetForEvent(event);
//    }

    /**
     * First it tries to return Event in ABBREVIATIONS if such name exists; if
     * not exists, then it will try to find event by event name; if it fails
     * too, will look for full class name.
     * ex) 1. onJoin -> 2. PlayerJoinEvent -> 3. org.bukkit.event.player.PlayerJoinEvent
     *
     * @param name name of event to search
     * @return the event class
     * @throws ClassNotFoundException throws if search fails or the result event is
     *                                a event that cannot receive events.
     */
    protected abstract Class<?> getEventFromName(String name) throws ClassNotFoundException;

//    /**
//     * Try to get set of Triggers associated with the event. It creates and puts new empty Set
//     *  if couldn't find existing one already, and register it so can handle the event.
//     * @param event
//     * @return Set of CustomTriggers associated with the event
//     */
//    protected Set<CustomTrigger> getTriggerSetForEvent(Class<?> event) {
//        Set<CustomTrigger> triggers = triggerMap.get(event);
//        if(triggers == null){
//            //this will allow TriggerReactor to hook events from other plugins as well.
//            registerEvent(plugin, event, eventHook);
//
//            triggers = new HashSet<>();
//            triggerMap.put(event, triggers);
//        }
//        return triggers;
//    }

    /**
     * Hook event to handle it manually.
     *
     * @param plugin
     * @param clazz
     * @param eventHook
     */
    protected abstract void registerEvent(TriggerReactor plugin, Class<?> clazz, EventHook eventHook);

    protected abstract void unregisterEvent(TriggerReactor plugin, EventHook eventHook);

    /**
     * Create a new CustomTrigger.
     *
     * @param eventName the class name of the Event that this Custom Trigger will
     *                  handle.
     * @param name      name of trigger (unique)
     * @param script    the script
     * @return true if created; false if trigger with the 'name' already exists.
     * @throws ClassNotFoundException throws if className is not in abbreviation list, not a valid
     *                                class name, or the specified event is not a valid event to
     *                                handle.
     * @throws ParserException
     * @throws LexerException
     * @throws IOException
     */
    public boolean createCustomTrigger(String eventName, String name, String script)
            throws ClassNotFoundException, TriggerInitFailedException {
        if (nameMap.containsKey(name))
            return false;

        Class<?> event = this.getEventFromName(eventName);

        File triggerFile = getTriggerFile(folder, name, true);
        CustomTrigger trigger = new CustomTrigger(event, eventName, name, triggerFile, script);

        nameMap.put(name, trigger);

        this.registerEvent(plugin, event, trigger);

        return true;
    }

    /**
     * Find and return Custom Trigger with the 'name'
     *
     * @param name
     * @return null if no such trigger with that name; CustomTrigger if found
     */
    public CustomTrigger getTriggerForName(String name) {
        return nameMap.get(name);
    }

//    /**
//     * get set of triggers associated with 'className' Event
//     * @param eventName the abbreviation, simple event name, or full event name. See {@link #getEventFromName(String)}
//     * @return set of triggers; null if nothing is registered with the 'className'
//     * @throws ClassNotFoundException See {@link #getEventFromName(String)}
//     */
//    public Set<CustomTrigger> getTriggersForEvent(String eventName) throws ClassNotFoundException {
//        Class<?> clazz = this.getEventFromName(eventName);
//
//        return triggerMap.get(clazz);
//    }

    /**
     * Delete the Custom Trigger with 'name'
     *
     * @param name
     * @return false if no such trigger exists with 'name'; true if deleted
     */
    public boolean removeTriggerForName(String name) {
        if (!nameMap.containsKey(name))
            return false;

        CustomTrigger trigger = nameMap.remove(name);

        unregisterEvent(plugin, trigger);

        deleteInfo(trigger);

        return true;
    }

    @Override
    protected void deleteInfo(Trigger trigger) {
        FileUtil.delete(new File(trigger.file.getParent(), trigger.getTriggerName() + ".yml"));
        super.deleteInfo(trigger);
    }

    @Override
    protected Collection<? extends Trigger> getAllTriggers() {
        return nameMap.values();
    }

    public AbstractCustomTriggerManager(TriggerReactor plugin, SelfReference ref, File tirggerFolder) {
        super(plugin, ref, tirggerFolder);
    }

    public static class CustomTrigger extends Trigger implements EventHook {
        final Class<?> event;
        private final String eventName;

        /**
         * @param event
         * @param name
         * @param script
         * @throws IOException     {@link Trigger#init()}
         * @throws LexerException  {@link Trigger#init()}
         * @throws ParserException {@link Trigger#init()}
         */
        public CustomTrigger(Class<?> event, String eventName, String name, File file, String script) throws TriggerInitFailedException {
            super(name, file, script);
            this.event = event;
            this.eventName = eventName;

            init();
        }

        @Override
        public Trigger clone() {
            try {
                return new CustomTrigger(event, getEventName(), triggerName, file, this.getScript());
            } catch (TriggerInitFailedException e) {
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
                if (other.triggerName != null)
                    return false;
            } else if (!triggerName.equals(other.triggerName))
                return false;
            return true;
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

    @FunctionalInterface
    public interface EventHook {
        void onEvent(Object e);
    }
}