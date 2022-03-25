package io.github.wysohn.triggerreactor.core.modules;

import dagger.Module;
import dagger.Provides;
import io.github.wysohn.triggerreactor.core.scope.PluginLifetime;

import javax.inject.Named;

@Module
public class ConstantsModule {
    @Provides
    @Named("CommandName")
    @PluginLifetime
    static String provideCommandName(){
        return "triggerreactor";
    }

    @Provides
    @Named("Permission")
    @PluginLifetime
    static String providePermission(){
        return "triggerreactor.admin";
    }
}
