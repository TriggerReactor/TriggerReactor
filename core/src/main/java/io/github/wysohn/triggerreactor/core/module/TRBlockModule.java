package io.github.wysohn.triggerreactor.core.module;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import io.github.wysohn.triggerreactor.trblock.main.JavalinWebService;
import io.github.wysohn.triggerreactor.trblock.main.TRBlockWebServiceImpl;

public class TRBlockModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TRBlockWebServiceImpl.class).to(JavalinWebService.class).in(Singleton.class);
    }

}
