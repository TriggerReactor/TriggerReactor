package modules;

import dagger.Module;
import dagger.Provides;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.scope.PluginScope;

import static org.mockito.Mockito.mock;

@Module
public abstract class DummyWrapperModule {
    @Provides
    @PluginScope
    static IWrapper provideWrapper(){
        return mock(IWrapper.class);
    }
}
