package io.github.wysohn.triggerreactor.core.modules;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import io.github.wysohn.triggerreactor.core.manager.ExternalAPIManager;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.PluginConfigManager;

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
}
