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
package io.github.wysohn.triggerreactor.core.manager.trigger.custom;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorMain;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

public abstract class AbstractCustomTriggerManager extends AbstractTriggerManager<CustomTrigger> {
    @Inject
    protected EventRegistry registry;

    private static final String EVENT = "Event";
    private static final String SYNC = "Sync";

    public AbstractCustomTriggerManager(String folderName) {
        super(folderName);
    }

    @Override
    public CustomTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        String eventName = info.getConfig().get(EVENT, String.class)
                .filter(registry::eventExist)
                .orElseThrow(() -> new InvalidTrgConfigurationException("Couldn't find target Event or is not a valid Event", info.getConfig()));
        boolean isSync = info.getConfig().get(SYNC, Boolean.class).orElse(false);

        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            return new CustomTrigger(throwableHandler, gameController, taskSupervisor, selfReference,
                    info, script, registry.getEvent(eventName), eventName);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(CustomTrigger trigger) {
        try {
            FileUtil.writeToFile(trigger.getInfo().getSourceCodeFile(), trigger.getScript());

            trigger.getInfo().getConfig().put(EVENT, trigger.getEventName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReload() {
        super.onReload();

        for (CustomTrigger trigger : getAllTriggers()) {
            registerEvent(main, trigger.event, trigger);
        }
    }

    /**
     * Hook event to handle it manually.
     *
     * @param plugin
     * @param clazz
     * @param eventHook
     */
    protected abstract void registerEvent(TriggerReactorMain plugin, Class<?> clazz, EventHook eventHook);

    protected abstract void unregisterEvent(TriggerReactorMain plugin, EventHook eventHook);

    public Collection<String> getAbbreviations(){
        return registry.getAbbreviations();
    }

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
        if (has(name))
            return false;

        Class<?> event = registry.getEvent(eventName);
        File file = getTriggerFile(folder, name, true);
        IConfigSource config = configSourceFactories.create(folder, name);
        TriggerInfo info = TriggerInfo.defaultInfo(file, config);
        CustomTrigger trigger = new CustomTrigger(throwableHandler, gameController, taskSupervisor, selfReference,
                info, script, event, eventName);

        put(name, trigger);

        this.registerEvent(main, event, trigger);

        return true;
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


    @Override
    public CustomTrigger remove(String name) {
        CustomTrigger remove = super.remove(name);
        unregisterEvent(main, remove);
        return remove;
    }

    @FunctionalInterface
    public interface EventHook {
        void onEvent(Object e);
    }

    public interface EventRegistry {

        boolean eventExist(String eventStr);

        /**
         * First it tries to return Event in ABBREVIATIONS if such name exists. If it wasn't found, then it simply
         * treat the eventStr as full class name and try to get the Event using {@link Class#forName(String)} method.
         * ex) 1. onJoin -> 2. org.bukkit.event.player.PlayerJoinEvent
         *
         * @param eventStr name of event to search
         * @return the event class
         * @throws ClassNotFoundException throws if search fails or the result event is
         *                                a event that cannot receive events (abstract events).
         */
        Class<?> getEvent(String eventStr) throws ClassNotFoundException;

        Collection<String> getAbbreviations();
    }
}