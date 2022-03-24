package io.github.wysohn.triggerreactor.core.modules;

import dagger.Module;
import dagger.Provides;

import javax.inject.Named;

@Module
public class ConstantsModule {
    @Provides
    @Named("CommandName")
    static String provideCommandName(){
        return "triggerreactor";
    }

    @Provides
    @Named("Permission")
    static String providePermission(){
        return "triggerreactor.admin";
    }
}
