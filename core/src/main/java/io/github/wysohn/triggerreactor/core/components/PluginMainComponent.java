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

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorMain;
import io.github.wysohn.triggerreactor.core.manager.IInventoryModifier;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandMapHandler;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.IEventRegistry;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.IGUIOpenHelper;
import io.github.wysohn.triggerreactor.core.modules.*;
import io.github.wysohn.triggerreactor.core.scope.PluginLifetime;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

import javax.inject.Named;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.util.logging.Logger;

/**
 * The main component that injects all the dependencies.
 */
@Component(modules = {ConfigSourceFactoryModule.class,
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

    @Component.Builder
    interface Builder {
        PluginMainComponent build();

        // injects
        @BindsInstance
        Builder logger(Logger logger);

        @BindsInstance
        Builder dataFolder(@Named("DataFolder") File dataFolder);

        @BindsInstance
        Builder pluginInstance(@Named("PluginInstance") Object pluginInstance);

        @BindsInstance
        Builder scriptEngineManager(ScriptEngineManager scriptEngineManager);

        @BindsInstance
        Builder selfReference(SelfReference selfReference);

        @BindsInstance
        Builder wrapper(IWrapper wrapper);

        @BindsInstance
        Builder pluginLifecycle(IPluginLifecycleController pluginLifecycle);

        @BindsInstance
        Builder gameController(IGameController gameController);

        @BindsInstance
        Builder inventoryModifier(IInventoryModifier inventoryModifier);

        @BindsInstance
        Builder itemStackClass(@Named("ItemStack") Class<?> itemStackClass);

        @BindsInstance
        Builder guiOpenHandler(IGUIOpenHelper guiOpenHandler);

        @BindsInstance
        Builder taskSupervisor(TaskSupervisor taskSupervisor);

        @BindsInstance
        Builder scriptEngineProvider(IScriptEngineProvider scriptEngineProvider);

        @BindsInstance
        Builder commandMapHandler(ICommandMapHandler commandMapHandler);

        @BindsInstance
        Builder eventRegistry(IEventRegistry eventRegistry);
    }
}
