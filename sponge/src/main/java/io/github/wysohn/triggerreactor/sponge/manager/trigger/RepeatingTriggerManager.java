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
package io.github.wysohn.triggerreactor.sponge.manager.trigger;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.AbstractRepeatingTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTrigger;
import io.github.wysohn.triggerreactor.sponge.tools.ConfigurationUtil;
import io.github.wysohn.triggerreactor.tools.FileUtil;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.File;
import java.io.IOException;

public class RepeatingTriggerManager extends AbstractRepeatingTriggerManager implements SpongeConfigurationFileIO {
    public RepeatingTriggerManager(TriggerReactorCore plugin) {
        super(plugin, new File(plugin.getDataFolder(), "RepeatTrigger"));
    }

    @Override
    protected void saveInfo(RepeatingTrigger trigger) throws IOException {

        ConfigurationLoader<ConfigurationNode> loader = YAMLConfigurationLoader
                .builder()
                .setPath(new File(folder, trigger.getTriggerName()).toPath())
                .build();
        ConfigurationNode yaml = loader.load();

        ConfigurationUtil.getNodeByKeyString(yaml, "AutoStart").setValue(false);
        ConfigurationUtil.getNodeByKeyString(yaml, "Interval").setValue(trigger.getInterval());
        loader.save(yaml);

        FileUtil.writeToFile(new File(folder, trigger.getTriggerName()), trigger.getScript());
    }
}
