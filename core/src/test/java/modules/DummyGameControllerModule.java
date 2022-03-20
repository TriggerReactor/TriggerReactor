package modules;

import dagger.Module;
import dagger.Provides;
import io.github.wysohn.triggerreactor.core.main.IGameController;

import static org.mockito.Mockito.mock;

@Module
public abstract class DummyGameControllerModule {
    @Provides
    static IGameController provideGameController(){
        return mock(IGameController.class);
    }
}
