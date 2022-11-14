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

package io.github.wysohn.triggerreactor.core.module.manager.trigger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.IRepeatingTriggerFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerManager;

import javax.inject.Named;

public class RepeatingTriggerModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                        .implement(RepeatingTrigger.class, RepeatingTrigger.class)
                        .build(IRepeatingTriggerFactory.class));

        binder().bind(new TypeLiteral<AbstractTriggerManager<RepeatingTrigger>>() {
                })
                .to(RepeatingTriggerManager.class);
        binder().bind(new TypeLiteral<ITriggerLoader<RepeatingTrigger>>() {
                })
                .to(RepeatingTriggerLoader.class);
    }

    @Provides
    @Named("RepeatingTriggerManagerFolder")
    public String provideFolderName() {
        return "RepeatTrigger";
    }
}
