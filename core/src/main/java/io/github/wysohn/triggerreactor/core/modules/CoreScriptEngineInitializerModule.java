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

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorAPI;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.scope.ScriptEngineInitScope;

import java.util.function.Function;

@Module
public abstract class CoreScriptEngineInitializerModule {
    @Provides
    @IntoSet
    @ScriptEngineInitScope
    static IScriptEngineInitializer provideAPI() {
        return (sem) -> sem.put("api", TriggerReactorAPI.class);
    }

    @Provides
    @IntoSet
    @ScriptEngineInitScope
    static IScriptEngineInitializer provideCharFn() {
        return (sem) -> sem.put("Char", (Function<String, Character>) t -> t.charAt(0));
    }
}