package io.github.wysohn.triggerreactor.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.CommandHandler;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.command.ITriggerCommand;

import javax.inject.Named;

@Component
public interface CommandHandlerTestComponent {
    CommandHandler getCommandHandler();

    @Component.Builder
    interface Builder {
        CommandHandlerTestComponent build();

        @BindsInstance
        Builder commandName(@Named("CommandName") String commandName);

        @BindsInstance
        Builder permission(@Named("Permission") String permission);

        @BindsInstance
        Builder triggerCommand(ITriggerCommand triggerCommand);

        @BindsInstance
        Builder pluginLifecycle(IPluginLifecycleController pluginLifecycle);
    }
}
