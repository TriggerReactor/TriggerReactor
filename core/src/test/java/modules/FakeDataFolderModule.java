package modules;

import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import java.io.File;

@Module
public abstract class FakeDataFolderModule {
    @Provides
    @Named("DataFolder")
    static File provideDataFolder() {
        return new File("");
    }
}
