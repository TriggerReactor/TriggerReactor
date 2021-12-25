/*
 *     Copyright (C) 2021 wysohn and contributors
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import io.github.wysohn.triggerreactor.core.main.ITriggerReactorAPI;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import java.util.Map;

public class ClickTrigger extends Trigger {
    private final AbstractLocationBasedTriggerManager.ClickHandler handler;

    public ClickTrigger(ITriggerReactorAPI api,
                        TriggerInfo info,
                        String script,
                        AbstractLocationBasedTriggerManager.ClickHandler handler) throws AbstractTriggerManager.TriggerInitFailedException {
        super(api, info, script);
        this.handler = handler;

        init();
    }

    @Override
    public boolean activate(Object e, Map<String, Object> scriptVars) {
        if (!handler.allow(e))
            return true;

        return super.activate(e, scriptVars);
    }

    @Override
    public Trigger clone() {
        try {
            //TODO: using same handler will be safe?
            return new ClickTrigger(api, info, script,
                    handler);
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
