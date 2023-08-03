package io.github.wysohn.triggerreactor.core.module.manager;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;

import javax.inject.Named;
import java.io.File;

public class ConfigurationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ConfigSourceFactory.class);
    }

    @Provides
    @Named("GlobalVariable")
    public IConfigSource provideConfigSource(ConfigSourceFactory factory, @Named("DataFolder") File dataFolder) {
        return factory.create(dataFolder, "var");
    }

    @Provides
    @Named("PluginConfig")
    public IConfigSource providePluginConfigSource(ConfigSourceFactory factory, @Named("DataFolder") File dataFolder) {
        return factory.create(dataFolder, "config");
    }
}
