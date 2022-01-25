package modules;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineInitializer;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.manager.ScriptEngineProvider;

import javax.inject.Singleton;
import java.util.function.Function;

@Module
public abstract class TestScriptEngineModule {
    @Binds
    @Singleton
    abstract IScriptEngineProvider bindProvider(ScriptEngineProvider provider);

    @Provides
    @Singleton
    @IntoSet
    static IScriptEngineInitializer provideCharFn() {
        return (sem) -> sem.put("Char", (Function<String, Character>) t -> t.charAt(0));
    }
}
