/*
 * Copyright (C) 2023. TriggerReactor Team
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

package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import com.google.inject.assistedinject.Assisted;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.StringUtils;

import javax.inject.Inject;

public class ExitTrigger extends Trigger {
    @Inject
    private IExitTriggerFactory factory;

    private final AreaTrigger areaTrigger;

    @Inject
    private ExitTrigger(@Assisted TriggerInfo info,
                        @Assisted String script,
                        @Assisted AreaTrigger areaTrigger) throws
            AbstractTriggerManager.TriggerInitFailedException {
        super(info, script);
        this.areaTrigger = areaTrigger;
    }

    @Override
    protected String getTimingId() {
        return StringUtils.dottedPath(areaTrigger.getTimingId(), "Exit");
    }

    @Override
    public Trigger clone() {
        return factory.create(getInfo(), getScript(), areaTrigger);
    }

}
