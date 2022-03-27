package io.github.wysohn.triggerreactor.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.components.PluginMainComponent;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.IInventoryModifier;
import io.github.wysohn.triggerreactor.core.manager.IResourceProvider;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandMapHandler;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.IEventRegistry;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.IGUIOpenHelper;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import io.github.wysohn.triggerreactor.modules.TriggerReactorMainTestModule;
import io.github.wysohn.triggerreactor.scope.TestLifetime;

import javax.inject.Named;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.util.logging.Logger;

@Component(modules = TriggerReactorMainTestModule.class)
@TestLifetime
public interface TriggerReactorMainTestComponent {
    PluginMainComponent.Builder getMainBuilder();

    @Component.Builder
    interface Builder {
        TriggerReactorMainTestComponent build();

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
        Builder commandMapHandler(ICommandMapHandler commandMapHandler);

        @BindsInstance
        Builder eventRegistry(IEventRegistry eventRegistry);

        @BindsInstance
        Builder resourceManager(IResourceProvider resourceProvider);

        @BindsInstance
        Builder commandName(@Named("CommandName") String commandName);

        @BindsInstance
        Builder permission(@Named("Permission") String permission);
    }
}
