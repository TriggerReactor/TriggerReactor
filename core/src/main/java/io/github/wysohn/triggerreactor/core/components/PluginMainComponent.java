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

import dagger.Component;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactories;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.main.command.ITriggerCommand;
import io.github.wysohn.triggerreactor.core.manager.IInventoryModifier;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.DynamicTabCompleter;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandMapHandler;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.IEventRegistry;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.IGUIOpenHelper;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.click.ClickTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.walk.WalkTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.scope.PluginLifetime;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;

import javax.inject.Named;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@Component(dependencies = {PluginLifecycleComponent.class,
                           ConfigurationComponent.class,
                           ManagerComponent.class,
                           TriggerComponent.class,
                           BootstrapComponent.class,
                           ScriptEngineInitializerComponent.class,
                           ExternalAPIComponent.class,
                           TabCompleterComponent.class,
                           InventoryUtilComponent.class,
                           UtilComponent.class})
@PluginLifetime
public interface PluginMainComponent {
    // PluginLifecycleComponent
    IPluginLifecycleController lifecycleController();

    // ConfigurationComponent
    @Named("DefaultConfigType")
    String defaultType();

    ConfigSourceFactories factories();

    // ManagerComponent
    Set<Manager> managers();

    // TriggerComponent
    AbstractLocationBasedTriggerManager<ClickTrigger> clickTrigger();

    AbstractLocationBasedTriggerManager<WalkTrigger> walkTrigger();

    // BootstrapComponent
    Logger logger();

    @Named("DataFolder")
    File dataFolder();

    ITriggerCommand command();

    IGameController gameController();

    TaskSupervisor taskSupervisor();

    IScriptEngineProvider scriptEngineProvider();

    ICommandMapHandler commandMapHandler();

    IEventRegistry eventRegistry();

    ScriptEngineManager scriptEngineManager();

    // ScriptEngineInitializerComponent
    Set<IScriptEngineInitializer> scriptEngineInitializers();

    // ExternalAPIComponent
    Map<String, Class<? extends AbstractAPISupport>> externalAPIs();

    // TabCompleterComponent
    Map<String, DynamicTabCompleter> tabCompleters();

    // InventoryUtilComponent
    IInventoryModifier inventoryModifier();

    @Named("ItemStack")
    Class<?> itemStack();

    IGUIOpenHelper guiOpenHelper();

    // UtilComponent
    IThrowableHandler throwableHandler();

    @Component.Builder
    interface Builder {
        PluginMainComponent build();

        Builder pluginLifecycleComponent(PluginLifecycleComponent component);

        Builder configurationComponent(ConfigurationComponent component);

        Builder managerComponent(ManagerComponent component);

        Builder triggerComponent(TriggerComponent component);

        Builder bootstrapComponent(BootstrapComponent component);

        Builder scriptEngineInitializerComponent(ScriptEngineInitializerComponent component);

        Builder externalAPIComponent(ExternalAPIComponent component);

        Builder tabCompleterComponent(TabCompleterComponent component);

        Builder inventoryUtilComponent(InventoryUtilComponent component);

        Builder utilComponent(UtilComponent component);
    }
}
