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

package io.github.wysohn.triggerreactor.core.main;

import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.modules.*;
import io.github.wysohn.triggerreactor.core.scope.PluginScope;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

import javax.inject.Named;
import java.io.File;
import java.util.Set;
import java.util.logging.Logger;

@Component(modules = {
        ConfigSourceFactoryModule.class,
        CorePluginMainModule.class,
        CoreManagerModule.class,
        CoreTriggerModule.class,
        CoreExternalAPIModule.class,
        CoreScriptEngineModule.class,
        CoreGameControllerModule.class,
        CoreUtilModule.class,
})
@PluginScope
public interface PluginMainComponent {
    ITriggerReactorAPI api();

    @Named("PluginInstance")
    Object pluginInstance();

    Logger logger();

    @Named("DataFolder")
    File dataFolder();

    IWrapper wrapper();

    TriggerReactorMain getMain();

    IPluginLifecycleController pluginLifecycleController();

    SelfReference selfReference();

    Set<Manager> managers();
}
