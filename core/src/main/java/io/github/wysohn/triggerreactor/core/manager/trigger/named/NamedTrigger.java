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

package io.github.wysohn.triggerreactor.core.manager.trigger.named;

import com.google.inject.assistedinject.Assisted;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import javax.inject.Inject;

public class NamedTrigger extends Trigger {
    @Inject
    private INamedTriggerFactory factory;

    @Inject
    private NamedTrigger(@Assisted TriggerInfo info,
                         @Assisted String script) {
        super(info, script);
    }

    @Override
    public NamedTrigger clone() {
        return factory.create(info, script);
    }

}
