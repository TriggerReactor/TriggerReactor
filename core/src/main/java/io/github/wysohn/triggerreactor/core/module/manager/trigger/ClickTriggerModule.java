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
import io.github.wysohn.triggerreactor.core.manager.trigger.location.ClickTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.ClickTriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.ClickTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.IClickTriggerFactory;

import javax.inject.Named;

public class ClickTriggerModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(ClickTrigger.class, ClickTrigger.class)
                .build(IClickTriggerFactory.class));

        binder().bind(new TypeLiteral<ITriggerLoader<ClickTrigger>>() {
                })
                .to(ClickTriggerLoader.class);
        binder().bind(new TypeLiteral<AbstractTriggerManager<ClickTrigger>>() {
                })
                .to(ClickTriggerManager.class);
    }

    @Provides
    @Named("ClickTriggerManagerFolder")
    public String provideClickTriggerName() {
        return "ClickTrigger";
    }
}
