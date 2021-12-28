package io.github.wysohn.triggerreactor.core.modules;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import io.github.wysohn.triggerreactor.core.manager.ExternalAPIManager;
import io.github.wysohn.triggerreactor.core.manager.GlobalVariableManager;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.PluginConfigManager;
import io.github.wysohn.triggerreactor.core.scope.ManagerScope;

@Module
public abstract class CoreManagerModule {
    @Binds
    @IntoSet
    @ManagerScope
    abstract Manager bindIntoSetGlobalVariableManager(GlobalVariableManager manager);

    @Binds
    @IntoSet
    @ManagerScope
    abstract Manager bindIntoSetPluginConfigManager(PluginConfigManager manager);

    @Binds
    @IntoSet
    @ManagerScope
    abstract Manager bindIntoSetExternalAPIManager(ExternalAPIManager manager);
}
