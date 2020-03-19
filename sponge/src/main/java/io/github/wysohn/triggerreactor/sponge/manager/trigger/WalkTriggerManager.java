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
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.sponge.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.sponge.tools.LocationUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WalkTriggerManager extends LocationBasedTriggerManager<AbstractLocationBasedTriggerManager.WalkTrigger> {
    public WalkTriggerManager(TriggerReactorCore plugin) {
        super(plugin, "WalkTrigger");
    }

    @Override
    protected WalkTrigger constructTrigger(String slocstr, String script) throws TriggerInitFailedException {
        File triggerFile = getTriggerFile(folder, slocstr, true);
        return new WalkTrigger(slocstr, triggerFile, script);
    }

    @Listener(order = Order.POST)
    public void onMove(PlayerBlockLocationEvent e) {
        handleWalk(e, e.getTo());
    }

    private void handleWalk(PlayerBlockLocationEvent e, SimpleLocation to) {
        Player player = e.getTargetEntity();
        SimpleLocation bottomLoc = to.clone();
        bottomLoc.add(0, -1, 0);

        WalkTrigger trigger = getTriggerForLocation(bottomLoc);
        if (trigger == null)
            return;

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", player);
        varMap.put("from", e.getFrom());
        varMap.put("to", e.getTo());
        varMap.put("block", LocationUtil.convertToBukkitLocation(bottomLoc));

        trigger.activate(e, varMap);
    }

    @Override
    protected String getTriggerTypeName() {
        return "Walk";
    }
}
