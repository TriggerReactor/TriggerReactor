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

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.IEventRegistry;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class CustomTriggerLoader implements ITriggerLoader<CustomTrigger> {
    @Inject
    private ICustomTriggerFactory factory;
    @Inject
    private IEventRegistry registry;

    @Inject
    private CustomTriggerLoader() {

    }

    @Override
    public CustomTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        String eventName = info.get(TriggerConfigKey.KEY_TRIGGER_CUSTOM_EVENT, String.class)
                .filter(registry::eventExist)
                .orElseThrow(() -> new InvalidTrgConfigurationException(
                        "Couldn't find target Event or is not a valid Event",
                        info));

        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            return factory.create(info, script, registry.getEvent(eventName), eventName);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(CustomTrigger trigger) {
        try {
            FileUtil.writeToFile(trigger.getInfo().getSourceCodeFile(), trigger.getScript());

            trigger.getInfo().put(TriggerConfigKey.KEY_TRIGGER_CUSTOM_EVENT, trigger.getEventName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
