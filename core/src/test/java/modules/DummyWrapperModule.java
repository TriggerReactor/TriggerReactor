package modules;

import dagger.Module;
import dagger.Provides;
import io.github.wysohn.triggerreactor.core.main.IWrapper;

import static org.mockito.Mockito.mock;

@Module
public abstract class DummyWrapperModule {
    @Provides
    static IWrapper provideWrapper(){
        return mock(IWrapper.class);
    }
}
