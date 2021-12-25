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

import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.ITriggerReactorAPI;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorMain;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.WalkTrigger;
import io.github.wysohn.triggerreactor.tools.FileUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class WalkTriggerManager extends LocationBasedTriggerManager<WalkTrigger> {
    @Inject
    ITriggerReactorAPI api;

    @Inject
    public WalkTriggerManager(TriggerReactorMain plugin) {
        super("WalkTrigger");
    }

    @Override
    protected String getTriggerTypeName() {
        return "Walk";
    }

    @Override
    protected WalkTrigger newTrigger(TriggerInfo info, String script) throws TriggerInitFailedException {
        return new WalkTrigger(api, info, script);
    }

    @Override
    public WalkTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            WalkTrigger trigger = new WalkTrigger(api, info, script);
            return trigger;
        } catch (TriggerInitFailedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(WalkTrigger trigger) {
        try {
            FileUtil.writeToFile(trigger.getInfo().getSourceCodeFile(), trigger.getScript());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleWalk(PlayerBlockLocationEvent e, SimpleLocation to) {
        Player player = e.getPlayer();
        SimpleLocation bottomLoc = to.clone();
        bottomLoc.add(0, -1, 0);

        WalkTrigger trigger = getTriggerForLocation(bottomLoc);
        if (trigger == null) return;

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", player);
        varMap.put("from", e.getFrom());
        varMap.put("to", e.getTo());
        varMap.put("block", LocationUtil.convertToBukkitLocation(bottomLoc).getBlock());

        trigger.activate(e, varMap);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerBlockLocationEvent e) {
        handleWalk(e, e.getTo());
    }
}
