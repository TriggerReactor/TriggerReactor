package io.github.wysohn.triggerreactor.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.command.ITriggerCommand;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.selection.AreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.selection.LocationSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.click.ClickTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.walk.WalkTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerManager;
import io.github.wysohn.triggerreactor.core.modules.CommandModule;
import io.github.wysohn.triggerreactor.core.modules.ConfigSourceFactoryModule;
import io.github.wysohn.triggerreactor.core.modules.CoreUtilModule;
import io.github.wysohn.triggerreactor.core.scope.APIScope;
import io.github.wysohn.triggerreactor.core.scope.ManagerScope;
import io.github.wysohn.triggerreactor.core.scope.PluginScope;
import io.github.wysohn.triggerreactor.core.scope.UtilityScope;
import modules.DummyGameControllerModule;
import modules.DummyLoggerModule;
import modules.DummyPluginLifecycleModule;
import modules.FakeDataFolderModule;

import javax.inject.Singleton;

@Component(modules = {CommandModule.class,
                      DummyLoggerModule.class,
                      DummyPluginLifecycleModule.class,
                      DummyGameControllerModule.class,
                      CoreUtilModule.class,
                      FakeDataFolderModule.class,
                      ConfigSourceFactoryModule.class})
@Singleton
@ManagerScope
@APIScope
@PluginScope
@UtilityScope
public interface TriggerCommandTestComponent {
    ITriggerCommand triggerCommand();

    @Component.Builder
    interface Builder{
        TriggerCommandTestComponent build();

        @BindsInstance
        Builder manager(ScriptEditManager manager);
        @BindsInstance
        Builder manager(GlobalVariableManager manager);
        @BindsInstance
        Builder manager(InventoryEditManager manager);
        @BindsInstance
        Builder manager(AreaSelectionManager manager);
        @BindsInstance
        Builder manager(ExecutorManager manager);
        @BindsInstance
        Builder manager(PlaceholderManager manager);
        @BindsInstance
        Builder manager(LocationSelectionManager manager);
        @BindsInstance
        Builder manager(ClickTriggerManager clickManager);
        @BindsInstance
        Builder manager(WalkTriggerManager walkManager);
        @BindsInstance
        Builder manager(CommandTriggerManager cmdManager);
        @BindsInstance
        Builder manager(NamedTriggerManager namedTriggerManager);
        @BindsInstance
        Builder manager(InventoryTriggerManager invManager);
        @BindsInstance
        Builder manager(AreaTriggerManager areaManager);
        @BindsInstance
        Builder manager(CustomTriggerManager customManager);
        @BindsInstance
        Builder manager(RepeatingTriggerManager repeatManager);
    }
}
