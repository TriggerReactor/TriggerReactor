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
import io.github.wysohn.triggerreactor.core.manager.trigger.named.INamedTriggerFactory;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;

import javax.inject.Named;

public class NamedTriggerModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(NamedTrigger.class, NamedTrigger.class)
                .build(INamedTriggerFactory.class));

        binder().bind(new TypeLiteral<AbstractTriggerManager<NamedTrigger>>() {
                })
                .to(NamedTriggerManager.class);
        binder().bind(new TypeLiteral<ITriggerLoader<NamedTrigger>>() {
                })
                .to(NamedTriggerLoader.class);
    }

    @Provides
    @Named("NamedTriggerManagerFolder")
    public String provideNamedTriggerManagerFolder() {
        return "NamedTriggers";
    }
}
