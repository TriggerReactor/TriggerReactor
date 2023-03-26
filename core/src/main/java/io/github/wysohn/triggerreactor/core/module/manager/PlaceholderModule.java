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

package io.github.wysohn.triggerreactor.core.module.manager;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import io.github.wysohn.triggerreactor.core.manager.evaluable.JSPlaceholder;
import io.github.wysohn.triggerreactor.core.manager.js.placeholder.IJSPlaceholderFactory;
import io.github.wysohn.triggerreactor.core.script.interpreter.Placeholder;

public class PlaceholderModule extends AbstractModule {
    @Override
    protected void configure() {
        // empty placeholder overrides
        MapBinder.newMapBinder(binder(), String.class, Placeholder.class);

        install(new FactoryModuleBuilder()
                .implement(Placeholder.class, JSPlaceholder.class)
                .build(IJSPlaceholderFactory.class));
    }
}
