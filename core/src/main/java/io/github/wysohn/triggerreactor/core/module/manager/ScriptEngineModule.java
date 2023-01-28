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

package io.github.wysohn.triggerreactor.core.module.manager;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.multibindings.ProvidesIntoSet;
import io.github.wysohn.triggerreactor.core.manager.ScriptEngineInitializer;

import javax.inject.Named;
import javax.script.ScriptEngineManager;

public class ScriptEngineModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    public ScriptEngineManager provideScriptEngineManager(@Named("PluginClassLoader") ClassLoader classLoader) {
        return new ScriptEngineManager(classLoader);
    }

    @ProvidesIntoSet
    public ScriptEngineInitializer provideJavaScriptInitializer() {
        return ScriptEngineInitializer.DEFAULT;
    }

    @ProvidesIntoSet
    public ScriptEngineInitializer provideInjectorToJavaScriptInitializer(Injector injector) {
        return (sem) -> sem.put("injector", injector);
    }
}
