package js.modules;

import dagger.Binds;
import dagger.Module;
import io.github.wysohn.triggerreactor.core.manager.ExecutorManager;
import modules.DummyLoggerModule;
import modules.FakeDataFolderModule;

import javax.inject.Inject;

@Module(includes = {DummyLoggerModule.class, FakeDataFolderModule.class,})
public abstract class ExecutorTestModule {
    @Binds
    abstract ExecutorManager bindManager(DummyExecutorManager manager);

    static class DummyExecutorManager extends ExecutorManager {

        @Inject
        DummyExecutorManager() {

        }

        @Override
        public void onDisable() {

        }

        @Override
        public void onReload() throws RuntimeException {

        }

        @Override
        public void saveAll() {

        }
    }
}
