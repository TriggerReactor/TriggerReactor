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

import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

@Singleton
public final class WalkTriggerManager extends LocationBasedTriggerManager<WalkTrigger> {

    @Inject
    private WalkTriggerManager(@Named("DataFolder") File folder,
                               @Named("WalkTriggerManagerFolder") String folderName) {
        super(new File(folder, folderName));
    }

    @Override
    public String getTriggerTypeName() {
        return "Walk";
    }

    @Override
    protected WalkTrigger newTrigger(TriggerInfo info, String script) throws TriggerInitFailedException {
        return new WalkTrigger(info, script);
    }

}
