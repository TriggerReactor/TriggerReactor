package js.modules;

import dagger.Binds;
import dagger.Module;
import io.github.wysohn.triggerreactor.core.manager.PlaceholderManager;
import modules.DummyLoggerModule;
import modules.FakeDataFolderModule;

import javax.inject.Inject;

@Module(includes = {DummyLoggerModule.class, FakeDataFolderModule.class,})
public abstract class PlaceholderTestModule {
    @Binds
    abstract PlaceholderManager bindManager(DummyPlaceholderManager manager);

    static class DummyPlaceholderManager extends PlaceholderManager {

        @Inject
        DummyPlaceholderManager() {

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
