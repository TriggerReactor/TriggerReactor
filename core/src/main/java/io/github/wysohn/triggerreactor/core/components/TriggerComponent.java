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

package io.github.wysohn.triggerreactor.core.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.click.ClickTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.walk.WalkTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerManager;
import io.github.wysohn.triggerreactor.core.modules.CoreTriggerModule;
import io.github.wysohn.triggerreactor.core.scope.ManagerScope;

@Component(modules = CoreTriggerModule.class)
@ManagerScope
public interface TriggerComponent {
    AreaTriggerManager areaTrigger();

    AbstractLocationBasedTriggerManager<ClickTrigger> clickTrigger();

    CommandTriggerManager commandTrigger();

    CustomTriggerManager customTrigger();

    InventoryTriggerManager inventoryTrigger();

    NamedTriggerManager namedTrigger();

    RepeatingTriggerManager repeatingTrigger();

    AbstractLocationBasedTriggerManager<WalkTrigger> walkTrigger();

    @Component.Builder
    interface Builder {
        TriggerComponent build();

        // injects
        @BindsInstance
        Builder areaTrigger(AreaTriggerManager manager);

        @BindsInstance
        Builder clickTrigger(AbstractLocationBasedTriggerManager<ClickTrigger> manager);

        @BindsInstance
        Builder commandTrigger(CommandTriggerManager manager);

        @BindsInstance
        Builder customTrigger(CustomTriggerManager manager);

        @BindsInstance
        Builder inventoryTrigger(InventoryTriggerManager manager);

        @BindsInstance
        Builder namedTrigger(NamedTriggerManager manager);

        @BindsInstance
        Builder repeatingTrigger(RepeatingTriggerManager manager);

        @BindsInstance
        Builder walkTrigger(AbstractLocationBasedTriggerManager<WalkTrigger> manager);
    }
}
