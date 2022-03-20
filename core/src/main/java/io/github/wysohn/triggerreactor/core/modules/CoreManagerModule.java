package io.github.wysohn.triggerreactor.core.modules;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.selection.AreaSelectionManager;

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
}
