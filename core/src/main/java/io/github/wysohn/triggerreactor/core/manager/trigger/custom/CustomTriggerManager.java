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
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactories;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class CustomTriggerManager extends AbstractTriggerManager<CustomTrigger> {
    @Inject
    CustomTriggerFactory factory;
    @Inject
    IEventRegistry eventRegistry;
    @Inject
    ConfigSourceFactories configSourceFactories;

    @Inject
    public CustomTriggerManager() {
        super("CustomTrigger");
    }

    @Override
    public CustomTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        String eventName = info.getConfig()
                .get(EVENT, String.class)
                .filter(eventRegistry::eventExist)
                .orElseThrow(
                        () -> new InvalidTrgConfigurationException("Couldn't find target Event or is not a valid Event",
                                info.getConfig()));

        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            return factory.create(info, script, eventRegistry.getEvent(eventName), eventName);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onReload() {
        super.onReload();

        eventRegistry.unregisterAll();
        for (CustomTrigger trigger : getAllTriggers()) {
            eventRegistry.registerEvent(trigger.event, trigger);
        }
    }

    @Override
    public CustomTrigger remove(String name) {
        CustomTrigger remove = super.remove(name);
        eventRegistry.unregisterEvent(remove);
        return remove;
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
    public boolean createCustomTrigger(String eventName, String name, String script) throws ClassNotFoundException {
        if (has(name))
            return false;

        Class<?> event = eventRegistry.getEvent(eventName);
        File file = getTriggerFile(folder, name, true);
        IConfigSource config = configSourceFactories.create(folder, name);
        TriggerInfo info = TriggerInfo.defaultInfo(file, config);
        CustomTrigger trigger = factory.create(info, script, event, eventName);
        put(name, trigger);

        eventRegistry.registerEvent(event, trigger);

        return true;
    }

    public Collection<String> getAbbreviations() {
        return eventRegistry.getAbbreviations();
    }

    private static final String EVENT = "Event";

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
    private static final String SYNC = "Sync";

    @FunctionalInterface
    public interface EventHook {
        void onEvent(Object e);
    }

}