package io.github.wysohn.triggerreactor.core.module.manager;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.SaveWorker;
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;

import javax.inject.Named;
import java.io.File;

public class ConfigurationModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
            .implement(IConfigSource.class, GsonConfigSource.class)
            .build(IConfigSourceFactory.class));
    }

    @Provides
    @Named("PluginConfig")
    public IConfigSource providePluginConfigSource(IConfigSourceFactory factory,
                                                   @Named("DataFolder") File dataFolder,
                                                   IExceptionHandle exceptionHandle) {
        return factory.create(new SaveWorker(5, (ex) -> exceptionHandle.handleException(null, ex)),
            dataFolder,
            "config");
    }
}
