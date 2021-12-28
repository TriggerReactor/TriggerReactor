package modules;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.SampleAPISupport;

@Module
public abstract class SampleExternalAPIProtoModule {
    @Provides
    @IntoMap
    @StringKey("SamplePlugin")
    static Class<? extends AbstractAPISupport> provideSample(){
        return SampleAPISupport.class;
    }
}
