package io.github.wysohn.triggerreactor.core.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

import javax.inject.Named;
import java.io.File;
import java.util.logging.Logger;

@Component
public interface BootstrapComponent {
    Logger logger();

    @Named("PluginInstance")
    Object pluginInstance();

    @Named("DataFolder")
    File dataFolder();

    SelfReference selfReference();

    IWrapper wrapper();

    @Component.Builder
    interface Builder {
        BootstrapComponent build();

        // injects
        @BindsInstance
        Builder logger(Logger logger);

        @BindsInstance
        Builder dataFolder(@Named("DataFolder") File dataFolder);

        @BindsInstance
        Builder pluginInstance(@Named("PluginInstance") Object pluginInstance);

        @BindsInstance
        Builder selfReference(SelfReference selfReference);

        @BindsInstance
        Builder wrapper(IWrapper wrapper);
    }
}
