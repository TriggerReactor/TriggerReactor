package modules;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.manager.ScriptEngineProvider;

import javax.inject.Singleton;
import javax.script.ScriptEngineManager;

@Module
public abstract class TestScriptEngineModule {
    @Binds
    @Singleton
    abstract IScriptEngineProvider bindProvider(ScriptEngineProvider provider);

    @Provides
    @Singleton
    static ScriptEngineManager provideManager(){
        return new ScriptEngineManager();
    }
}
