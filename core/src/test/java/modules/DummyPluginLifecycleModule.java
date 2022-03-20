package modules;

import dagger.Module;
import dagger.Provides;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Module
public abstract class DummyPluginLifecycleModule {
    @Provides
    static IPluginLifecycleController providePluginLifecycle(){
        IPluginLifecycleController controller = mock(IPluginLifecycleController.class);
        when(controller.isEnabled(anyString())).thenReturn(true);
        when(controller.getPlugin(anyString())).thenReturn(mock(Object.class));
        return controller;
    }
}
