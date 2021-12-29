package io.github.wysohn.triggerreactor.core.modules;

import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class ConstantsModule {
    @Provides
    @Singleton
    @Named("CommandName")
    static String provideCommandName(){
        return "triggerreactor";
    }

    @Provides
    @Singleton
    @Named("Permission")
    static String providePermission(){
        return "triggerreactor.admin";
    }
}
