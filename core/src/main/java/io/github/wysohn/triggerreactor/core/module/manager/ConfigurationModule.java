package io.github.wysohn.triggerreactor.core.module.manager;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.triggerreactor.core.config.source.GsonConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSourceFactory;

public class ConfigurationModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(IConfigSource.class, GsonConfigSource.class)
                .build(IConfigSourceFactory.class));
    }
}
