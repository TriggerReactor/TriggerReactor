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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.bukkit.tools.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractRepeatingTriggerManager;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;
import java.io.IOException;

public class RepeatingTriggerManager extends AbstractRepeatingTriggerManager implements BukkitTriggerManager {
    public RepeatingTriggerManager(TriggerReactor plugin) {
        super(plugin, new CommonFunctions(plugin), new File(plugin.getDataFolder(), "RepeatTrigger"));
    }

    @Override
    protected void saveInfo(RepeatingTrigger trigger) throws IOException {
        Utf8YamlConfiguration yaml = new Utf8YamlConfiguration();
        yaml.set("AutoStart", false);
        yaml.set("Interval", trigger.getInterval());
        yaml.save(new File(folder, trigger.getTriggerName() + ".yml"));

        FileUtil.writeToFile(new File(folder, trigger.getTriggerName()), trigger.getScript());
    }
}
