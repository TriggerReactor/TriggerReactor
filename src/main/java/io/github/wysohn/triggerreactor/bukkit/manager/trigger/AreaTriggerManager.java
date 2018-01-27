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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.CommonFunctions;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.tools.FileUtil;

public class AreaTriggerManager extends AbstractAreaTriggerManager implements BukkitTriggerManager{
    public AreaTriggerManager(TriggerReactor plugin) {
        super(plugin, new CommonFunctions(plugin), new File(plugin.getDataFolder(), "AreaTrigger"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLocationChange(PlayerBlockLocationEvent e){
        Entry<Area, AreaTrigger> from = getAreaForLocation(e.getFrom());
        Entry<Area, AreaTrigger> to = getAreaForLocation(e.getTo());

        if(from == null && to == null)
            return;

        if(from != null && to != null && from.getKey().equals(to.getKey()))
            return;

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", e.getPlayer());
        varMap.put("from", e.getFrom());
        varMap.put("to", e.getTo());

        if(from != null){
            from.getValue().activate(e, varMap, EventType.EXIT);
        }

        if(to != null){
            to.getValue().activate(e, varMap, EventType.ENTER);
        }
    }

    @Override
    protected void deleteInfo(Trigger trigger) {
        File areafile = new File(folder, trigger.getTriggerName()+".yml");
        FileUtil.delete(areafile);
        File areafolder = new File(folder, trigger.getTriggerName());
        FileUtil.delete(areafolder);
    }
}
