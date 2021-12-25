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
import io.github.wysohn.triggerreactor.core.manager.*;

@Module
public abstract class CoreManagerModule {
    @Binds
    @IntoSet
    abstract Manager bindGlobalVariableManagerIntoSet(GlobalVariableManager manager);

    @Binds
    @IntoSet
    abstract Manager bindPluginConfigManagerIntoSet(PluginConfigManager manager);

    @Provides
    static AbstractExternalAPIManager bindExternalAPIManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindExternalAPIManagerIntoSet(AbstractExternalAPIManager manager);

    @Provides
    static AbstractScriptEditManager bindScriptEditManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindScriptEditManagerIntoSet(AbstractScriptEditManager manager);

    @Provides
    static AbstractPlayerLocationManager bindPlayerLocationManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindPlayerLocationManagerIntoSet(AbstractPlayerLocationManager manager);

    @Provides
    static AbstractAreaSelectionManager bindAreaSelectionManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindAreaSelectionManagerIntoSet(AbstractAreaSelectionManager manager);

    @Provides
    static AbstractPermissionManager bindPermissionManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindPermissionManagerIntoSet(AbstractPermissionManager manager);

    @Provides
    static AbstractInventoryEditManager bindInventoryEditManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindInventoryEditManagerIntoSet(AbstractInventoryEditManager manager);

    @Provides
    static AbstractExecutorManager bindExecutorManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindExecutorManagerIntoSet(AbstractExecutorManager manager);

    @Provides
    static AbstractPlaceholderManager bindPlaceholderManager(){
        throw new RuntimeException("Must be provided by dependant.");
    }

    @Binds
    @IntoSet
    abstract Manager bindPlaceholderManagerIntoSet(AbstractPlaceholderManager manager);
}
