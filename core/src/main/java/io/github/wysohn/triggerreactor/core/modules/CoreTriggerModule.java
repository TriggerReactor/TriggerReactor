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

package io.github.wysohn.triggerreactor.core.modules;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.click.ClickTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.walk.WalkTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerManager;

@Module
public abstract class CoreTriggerModule {
    @Binds
    @IntoSet
    abstract Manager bindAreaTriggerIntoSet(AreaTriggerManager manager);

    @Binds
    @IntoSet
    abstract Manager bindClickTriggerIntoSet(AbstractLocationBasedTriggerManager<ClickTrigger> manager);

    @Binds
    @IntoSet
    abstract Manager bindCommandTriggerIntoSet(CommandTriggerManager manager);

    @Binds
    @IntoSet
    abstract Manager bindCustomTriggerIntoSet(CustomTriggerManager manager);

    @Binds
    @IntoSet
    abstract Manager bindInventoryTriggerIntoSet(InventoryTriggerManager manager);

    @Binds
    @IntoSet
    abstract Manager bindNamedTriggerIntoSet(NamedTriggerManager manager);

    @Binds
    @IntoSet
    abstract Manager bindRepeatingTriggerIntoSet(RepeatingTriggerManager manager);

    @Binds
    @IntoSet
    abstract Manager bindWalkTriggerIntoSet(AbstractLocationBasedTriggerManager<WalkTrigger> manager);
}
