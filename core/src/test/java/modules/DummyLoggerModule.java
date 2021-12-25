package modules;

import dagger.Module;
import dagger.Provides;

import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

@Module
public abstract class DummyLoggerModule {
    @Provides
    static Logger provideLogger(){
        return mock(Logger.class);
    }
}
