package modules;

import dagger.Module;
import dagger.Provides;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.scope.PluginScope;

import static org.mockito.Mockito.mock;

@Module
public abstract class DummyGameControllerModule {
    @Provides
    @PluginScope
    static IGameController provideGameController(){
        return mock(IGameController.class);
    }
}
