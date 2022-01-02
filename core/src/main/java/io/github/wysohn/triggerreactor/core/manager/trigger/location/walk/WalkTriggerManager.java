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
package io.github.wysohn.triggerreactor.core.manager.trigger.location.walk;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorMain;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class WalkTriggerManager extends AbstractLocationBasedTriggerManager<WalkTrigger> {
    @Inject
    WalkTriggerFactory factory;

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
        return factory.create(info, script);
    }

    @Override
    public WalkTrigger load(TriggerInfo info) throws InvalidTrgConfigurationException {
        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            return factory.create(info, script);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onMove(Object event,
                       Object player,
                       SimpleLocation from,
                       SimpleLocation to,
                       Object block,
                       SimpleLocation bottomLoc) {
        WalkTrigger trigger = getTriggerForLocation(bottomLoc);
        if (trigger == null)
            return;

        Map<String, Object> varMap = new HashMap<>();
        varMap.put(Trigger.VAR_NAME_EVENT, event);
        varMap.put("player", player);
        varMap.put("from", from);
        varMap.put("to", to);
        varMap.put("block", block);

        trigger.activate(varMap);
    }

    public Trigger get(SimpleLocation clickedLoc) {
        return getTriggerForLocation(clickedLoc);
    }
}
