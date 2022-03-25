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
package io.github.wysohn.triggerreactor.core.manager.trigger.location.click;

import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.selection.ClickType;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClickTriggerManager extends AbstractLocationBasedTriggerManager<ClickTrigger> {
    @Inject
    ClickTriggerFactory factory;

    @Inject
    public ClickTriggerManager() {
        super("ClickTrigger");
    }

    @Override
    protected String getTriggerTypeName() {
        return "Click";
    }

    @Override
    protected ClickTrigger newTrigger(TriggerInfo info, String script) throws TriggerInitFailedException {
        return getTrigger(info, script);
    }

    private ClickTrigger getTrigger(TriggerInfo info, String script) throws TriggerInitFailedException {
        return factory.create(info, script);
    }

    @Override
    public ClickTrigger load(TriggerInfo info) {
        try {
            String script = FileUtil.readFromFile(info.getSourceCodeFile());
            return getTrigger(info, script);
        } catch (TriggerInitFailedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onClickTrigger(Object event,
                               Object player,
                               Object clicked,
                               Object item,
                               SimpleLocation sloc,
                               ClickType type) {
        ClickTrigger trigger = getTriggerForLocation(sloc);
        if (trigger == null)
            return;

        Map<String, Object> varMap = new HashMap<>();
        varMap.put(Trigger.VAR_NAME_EVENT, event);
        varMap.put("player", player);
        varMap.put("block", clicked);
        varMap.put("item", item);
        varMap.put("click", type.getVariable());

        trigger.activate(varMap);
    }

    public Trigger get(SimpleLocation sloc) {
        return getTriggerForLocation(sloc);
    }
}
