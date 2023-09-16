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

import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.IEventRegistry;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

@Singleton
public final class CustomTriggerManager extends AbstractTriggerManager<CustomTrigger> {
    @Inject
    private ICustomTriggerFactory factory;
    @Inject
    private IEventRegistry registry;

    @Inject
    public CustomTriggerManager(@Named("DataFolder") File dataFolder,
                                @Named("CustomTriggerManagerFolder") String folderName) {
        super(new File(dataFolder, folderName));
    }

    @Override
    public void initialize() {

    }

    @Override
    public void reload() {
        registry.unregisterAll();

        super.reload();

        for (CustomTrigger trigger : getAllTriggers()) {
            registry.registerEvent(trigger.getEvent(), trigger);
        }
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
        IConfigSource config = getConfigSource(folder, name);
        TriggerInfo info = TriggerInfo.defaultInfo(file, config);
        CustomTrigger trigger = factory.create(info, script, event, eventName);
        trigger.init();

        put(name, trigger);

        registry.registerEvent(event, trigger);

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
        registry.unregisterEvent(remove);
        return remove;
    }

}
