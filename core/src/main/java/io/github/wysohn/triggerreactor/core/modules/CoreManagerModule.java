package io.github.wysohn.triggerreactor.core.modules;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.selection.AreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.click.ClickTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.click.ClickTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.walk.WalkTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.walk.WalkTriggerManager;

@Module
public abstract class CoreManagerModule {
    @Binds
    @IntoSet
    abstract Manager bindIntoSetGlobalVariableManager(GlobalVariableManager manager);

    @Binds
    @IntoSet
    abstract Manager bindIntoSetPluginConfigManager(PluginConfigManager manager);

    @Binds
    @IntoSet
    abstract Manager bindIntoSetExternalAPIManager(ExternalAPIManager manager);

    @Binds
    @IntoSet
    abstract Manager bindIntoSetAreaSelectionManager(AreaSelectionManager manager);

    @Binds
    @IntoSet
    abstract Manager bindIntoSetExecutorManager(ExecutorManager manager);

    @Binds
    @IntoSet
    abstract Manager bindIntoSetInventoryEditManager(InventoryEditManager manager);

    @Binds
    @IntoSet
    abstract Manager bindIntoSetPlaceholderManager(PlaceholderManager manager);

    @Binds
    @IntoSet
    abstract Manager bindIntoSetPlayerLocationManager(PlayerLocationManager manager);

    @Binds
    @IntoSet
    abstract Manager bindIntoSetScriptEditManager(ScriptEditManager manager);

    @Binds
    abstract AbstractLocationBasedTriggerManager<ClickTrigger> bindClickTriggerManager(ClickTriggerManager manager);

    @Binds
    @IntoSet
    abstract Manager bindIntoSetClickTriggerManager(ClickTriggerManager manager);

    @Binds
    abstract AbstractLocationBasedTriggerManager<WalkTrigger> bindWalkTriggerManager(WalkTriggerManager manager);

    @Binds
    @IntoSet
    abstract Manager bindIntoSetWalkTriggerManager(WalkTriggerManager manager);
}
