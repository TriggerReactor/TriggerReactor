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

package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import com.google.inject.assistedinject.Assisted;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import javax.inject.Inject;
import java.util.Map;

public class ClickTrigger extends Trigger {
    private final ClickHandler handler;

    @Inject
    private ClickTrigger(@Assisted TriggerInfo info,
                         @Assisted String script,
                         @Assisted ClickHandler handler) throws
            AbstractTriggerManager.TriggerInitFailedException {
        super(info, script);
        this.handler = handler;
    }

    @Override
    public boolean activate(Object e, Map<String, Object> scriptVars) {
        if (!scriptVars.containsKey(LocationBasedTriggerManager.KEY_CONTEXT_ACTIVITY))
            throw new RuntimeException("ClickTrigger: Context activity not found in script variables");

        Activity activity = (Activity) scriptVars.get(
                LocationBasedTriggerManager.KEY_CONTEXT_ACTIVITY);
        if (!handler.allow(activity))
            return true;

        return super.activate(e, scriptVars);
    }

    @Override
    public Trigger clone() {
        try {
            //TODO: using same handler will be safe?
            return new ClickTrigger(info, script, handler);
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
