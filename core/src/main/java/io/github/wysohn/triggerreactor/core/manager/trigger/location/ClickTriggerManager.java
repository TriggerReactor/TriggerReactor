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

import com.google.inject.Inject;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

@Singleton
public final class ClickTriggerManager extends LocationBasedTriggerManager<ClickTrigger> {
    @Inject
    private IClickTriggerFactory factory;

    @Inject
    private ClickTriggerManager(@Named("DataFolder") File folder,
                                @Named("ClickTriggerManagerFolder") String folderName) {
        super(new File(folder, folderName));
    }

    @Override
    public String getTriggerTypeName() {
        return "Click";
    }

    @Override
    protected ClickTrigger newInstance(TriggerInfo info, String script) throws TriggerInitFailedException {
        ClickTrigger clickTrigger = factory.create(info, script, ClickHandler.DEFAULT);
        clickTrigger.init();
        return clickTrigger;
    }
}
