package io.github.wysohn.triggerreactor.core.module;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;

public class APISupportModule extends AbstractModule {
    @Override
    protected void configure() {
        // empty api specs
        MapBinder.newMapBinder(binder(), new TypeLiteral<String>() {
        }, new TypeLiteral<Class<? extends AbstractAPISupport>>() {
        });
    }
}
