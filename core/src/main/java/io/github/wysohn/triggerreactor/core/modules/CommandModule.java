package io.github.wysohn.triggerreactor.core.modules;

import dagger.Module;
import dagger.Provides;
import io.github.wysohn.triggerreactor.core.main.command.ITriggerCommand;
import io.github.wysohn.triggerreactor.core.main.command.TriggerCommand;

import javax.inject.Singleton;

@Module
public abstract class CommandModule {
    @Provides
    @Singleton
    static ITriggerCommand bindCommand(TriggerCommand commandImpl){
        return commandImpl.createCommand();
    }
}
