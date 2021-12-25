package io.github.wysohn.triggerreactor.core.script.interpreter;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSourceFactory;

import javax.inject.Named;

import static org.mockito.Mockito.mock;

@Module
public abstract class GlobalVariableSettingsModule {
    @Provides
    @Named("DefaultConfigType")
    static String provideType(){
        return "gson";
    }

    @Provides
    @IntoMap
    @StringKey("gson")
    static IConfigSourceFactory provideFactory(){
        return mock(IConfigSourceFactory.class);
    }
}
