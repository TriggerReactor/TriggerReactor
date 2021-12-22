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
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.AbstractCommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.AbstractCustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.AbstractInventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.ClickTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.WalkTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.AbstractNamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.AbstractRepeatingTriggerManager;

@Module
public abstract class CoreTriggerModule {
    @Provides
    static AbstractLocationBasedTriggerManager<ClickTrigger> provideClickTriggerManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindClickTriggerIntoSet(AbstractLocationBasedTriggerManager<ClickTrigger> manager);

    @Provides
    static AbstractLocationBasedTriggerManager<WalkTrigger> provideWalkTriggerManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindWalkTriggerIntoSet(AbstractLocationBasedTriggerManager<WalkTrigger> manager);

    @Provides
    static AbstractCommandTriggerManager provideCommandTriggerManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindCommandTriggerIntoSet(AbstractCommandTriggerManager manager);

    @Provides
    static AbstractAreaTriggerManager provideAreaTriggerManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindAreaTriggerIntoSet(AbstractAreaTriggerManager manager);

    @Provides
    static AbstractNamedTriggerManager provideNamedTriggerManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindNamedTriggerIntoSet(AbstractNamedTriggerManager manager);

    @Provides
    static AbstractCustomTriggerManager provideCustomTriggerManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindCustomTriggerIntoSet(AbstractCustomTriggerManager manager);

    @Provides
    static AbstractInventoryTriggerManager<?> provideInventoryTriggerManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindInventoryTriggerIntoSet(AbstractInventoryTriggerManager<?> manager);

    @Provides
    static AbstractRepeatingTriggerManager provideRepeatingTriggerManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindRepeatingTriggerIntoSet(AbstractRepeatingTriggerManager manager);
}
