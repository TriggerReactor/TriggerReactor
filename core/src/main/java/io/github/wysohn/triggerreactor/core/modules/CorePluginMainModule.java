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
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.ITriggerReactorAPI;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorAPI;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

import javax.inject.Named;
import java.io.File;

@Module
public abstract class CorePluginMainModule {
    @Binds
    abstract ITriggerReactorAPI bindAPI(TriggerReactorAPI api);

    @Provides
    @Named("PluginInstance")
    static Object providePluginInstance() {
        throw new RuntimeException("Dependent must provide it");
    }

    @Provides
    @Named("DataFolder")
    static File provideDataFolder() {
        throw new RuntimeException("Dependent must provide it");
    }

    @Provides
    static IWrapper provideWrapper() {
        throw new RuntimeException("Dependent must provide it");
    }

    @Provides
    static IPluginLifecycleController provideLifecycleController() {
        throw new RuntimeException("Dependent must provide it");
    }

    @Provides
    static SelfReference provideSelfReference() {
        throw new RuntimeException("Dependent must provide it");
    }
}
