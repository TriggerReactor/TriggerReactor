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
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import io.github.wysohn.triggerreactor.core.manager.evaluable.JSExecutor;
import io.github.wysohn.triggerreactor.core.manager.js.IBackedMapProvider;
import io.github.wysohn.triggerreactor.core.manager.js.executor.ExecutorManager;
import io.github.wysohn.triggerreactor.core.manager.js.executor.IJSExecutorFactory;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;

public class ExecutorModule extends AbstractModule {
    @Override
    protected void configure() {
        // empty executor overrides
        MapBinder.newMapBinder(binder(), String.class, Executor.class);

        install(new FactoryModuleBuilder()
                .implement(Executor.class, JSExecutor.class)
                .build(IJSExecutorFactory.class));

        bind(Key.get(new TypeLiteral<IBackedMapProvider<Executor>>() {
        })).to(ExecutorManager.class);
    }
}
