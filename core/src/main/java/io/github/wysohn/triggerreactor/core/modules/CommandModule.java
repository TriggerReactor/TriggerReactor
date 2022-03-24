package io.github.wysohn.triggerreactor.core.modules;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.Multibinds;
import io.github.wysohn.triggerreactor.core.main.command.ITriggerCommand;
import io.github.wysohn.triggerreactor.core.main.command.TriggerCommand;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.DynamicTabCompleter;

import javax.inject.Singleton;
import java.util.Map;

@Module
public abstract class CommandModule {
    @Provides
    @Singleton
    static ITriggerCommand bindCommand(TriggerCommand commandImpl){
        return commandImpl.createCommand();
    }

    @Multibinds
    abstract Map<String, DynamicTabCompleter> emptyDynamicTabCompleterProtoMap();
}
