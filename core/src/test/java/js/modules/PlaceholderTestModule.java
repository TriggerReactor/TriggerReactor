package js.modules;

import dagger.Binds;
import dagger.Module;
import io.github.wysohn.triggerreactor.core.manager.AbstractPlaceholderManager;
import modules.DummyLoggerModule;
import modules.FakeDataFolderModule;

import javax.inject.Inject;

@Module(includes = {DefaultTestModule.class, DummyLoggerModule.class, FakeDataFolderModule.class,})
public abstract class PlaceholderTestModule {
    @Binds
    abstract AbstractPlaceholderManager bindManager(DummyPlaceholderManager manager);

    static class DummyPlaceholderManager extends AbstractPlaceholderManager {

        @Inject
        DummyPlaceholderManager() {

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
