package io.github.wysohn.triggerreactor.core.module.manager;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;

import javax.inject.Named;
import java.io.File;

public class ConfigurationModule extends AbstractModule {
    @Provides
    @Named("GlobalVariable")
    public IConfigSource provideConfigSource(@Named("DataFolder") File dataFolder) {
        return ConfigSourceFactory.instance().create(dataFolder, "var");
    }

    @Provides
    @Named("PluginConfig")
    public IConfigSource providePluginConfigSource(@Named("DataFolder") File dataFolder) {
        return ConfigSourceFactory.instance().create(dataFolder, "config");
    }
}
