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

import dagger.Subcomponent;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorMain;
import io.github.wysohn.triggerreactor.core.modules.*;
import io.github.wysohn.triggerreactor.core.scope.PluginLifetime;

/**
 * The main component that injects all the dependencies.
 */
@Subcomponent(modules = {ConfigSourceFactoryModule.class,
                         ConstantsModule.class,
                         CommandModule.class,
                         CoreTriggerModule.class,
                         CoreManagerModule.class,
                         CoreScriptEngineInitializerModule.class,
                         CoreTabCompleterModule.class,
                         CoreExternalAPIModule.class,
                         CoreUtilModule.class})
@PluginLifetime
public interface PluginMainComponent {
    TriggerReactorMain getMain();

    @Subcomponent.Builder
    interface Builder{
        PluginMainComponent build();
    }
}
