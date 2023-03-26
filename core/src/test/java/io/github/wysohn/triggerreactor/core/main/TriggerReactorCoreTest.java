package io.github.wysohn.triggerreactor.core.main;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import io.github.wysohn.triggerreactor.core.main.command.ICommandHandler;
import io.github.wysohn.triggerreactor.core.module.CorePluginModule;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.inject.Named;
import java.io.File;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

public class TriggerReactorCoreTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final Logger logger = mock(Logger.class);
    private final Object pluginObject = mock(Object.class);

    private final IPluginManagement pluginManagement = mock(IPluginManagement.class);
    private final IGameManagement gameManagement = mock(IGameManagement.class);
    private final IEventManagement eventManagement = mock(IEventManagement.class);
    private final TaskSupervisor taskSupervisor = mock(TaskSupervisor.class);

    private final ICommandHandler commandHandler = mock(ICommandHandler.class);
    private final IEventRegistry eventRegistry = mock(IEventRegistry.class);
    private final IInventoryHandle inventoryHandle = mock(IInventoryHandle.class);

    @Test
    public void initialize() {
        TriggerReactorCore triggerReactorCore = Guice.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {

                    }

                    @Provides
                    @Named("DataFolder")
                    public File providePluginFolder() {
                        return folder.getRoot();
                    }

                    @Provides
                    @Named("PluginLogger")
                    public Logger provideLogger() {
                        return logger;
                    }

                    @Provides
                    @Named("PluginClassLoader")
                    public ClassLoader provideClassLoader() {
                        return getClass().getClassLoader();
                    }

                    @Provides
                    @Named("Plugin")
                    public Object providePluginObject() {
                        return pluginObject;
                    }

                    @Provides
                    public IPluginManagement providePluginManagement() {
                        return pluginManagement;
                    }

                    @Provides
                    public IGameManagement provideGameManagement() {
                        return gameManagement;
                    }

                    @Provides
                    public IEventManagement provideEventManagement() {
                        return eventManagement;
                    }

                    @Provides
                    public TaskSupervisor provideTaskSupervisor() {
                        return taskSupervisor;
                    }

                    @Provides
                    public ICommandHandler provideCommandHandler() {
                        return commandHandler;
                    }

                    @Provides
                    public IEventRegistry provideEventRegistry() {
                        return eventRegistry;
                    }

                    @Provides
                    public IInventoryHandle provideInventoryHandle() {
                        return inventoryHandle;
                    }
                },
                new CorePluginModule()
        ).getInstance(TriggerReactorCore.class);

        triggerReactorCore.initialize();
    }

    @Test
    public void reload() {
    }

    @Test
    public void shutdown() {
    }
}