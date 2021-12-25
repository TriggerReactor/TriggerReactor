package js.modules;

import dagger.Binds;
import dagger.Module;
import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager;
import modules.DummyLoggerModule;
import modules.FakeDataFolderModule;

import javax.inject.Inject;

@Module(includes = {DefaultTestModule.class, DummyLoggerModule.class, FakeDataFolderModule.class,})
public abstract class ExecutorTestModule {
    @Binds
    abstract AbstractExecutorManager bindManager(DummyExecutorManager manager);

    static class DummyExecutorManager extends AbstractExecutorManager {

        @Inject
        DummyExecutorManager() {

        }

        @Override
        public void onDisable() {

        }

        @Override
        public void onEnable() throws Exception {

        }

        @Override
        public void onReload() throws RuntimeException {

        }

        @Override
        public void saveAll() {

        }
    }
}
