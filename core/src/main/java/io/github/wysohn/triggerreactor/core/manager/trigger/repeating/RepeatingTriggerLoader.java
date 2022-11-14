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

package io.github.wysohn.triggerreactor.core.manager.trigger.repeating;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.inject.Inject;
import java.io.IOException;

public class RepeatingTriggerLoader implements ITriggerLoader<RepeatingTrigger> {
    @Inject
    private RepeatingTriggerLoader(){

    }

    @Override
    public RepeatingTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        boolean autoStart = info.get(TriggerConfigKey.KEY_TRIGGER_REPEATING_AUTOSTART, Boolean.class)
                .orElse(false);
        int interval = info.get(TriggerConfigKey.KEY_TRIGGER_REPEATING_INTERVAL, Integer.class).orElse(1000);

        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            RepeatingTrigger trigger = new RepeatingTrigger(info, script);
            trigger.setAutoStart(autoStart);
            trigger.setInterval(interval);
            return trigger;
        } catch (AbstractTriggerManager.TriggerInitFailedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(RepeatingTrigger trigger) {
        try {
            FileUtil.writeToFile(trigger.getInfo().getSourceCodeFile(), trigger.getScript());

            trigger.getInfo().put(TriggerConfigKey.KEY_TRIGGER_REPEATING_AUTOSTART, trigger.isAutoStart());
            trigger.getInfo().put(TriggerConfigKey.KEY_TRIGGER_REPEATING_INTERVAL, trigger.getInterval());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
