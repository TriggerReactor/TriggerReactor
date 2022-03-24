package io.github.wysohn.triggerreactor.core.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.main.command.ITriggerCommand;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandMapHandler;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.IEventRegistry;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.util.logging.Logger;

/**
 * Component responsible for providing all the platform-specific dependencies.
 * <p>
 * For example, different platform has different ways to schedule synchronous tasks,
 * different ways to modify the items, etc. which are different per platform. These
 * all has to be provided by the responsible class, such as JavaPlugin in Bukkit API,
 * so the underlying components can rely on the platform-specific implementation.
 */
@Component
@Singleton
public interface BootstrapComponent {
    Logger logger();

    @Named("PluginInstance")
    Object pluginInstance();

    @Named("DataFolder")
    File dataFolder();

    ITriggerCommand triggerCommand();

    ScriptEngineManager scriptEngineManager();

    SelfReference selfReference();

    IWrapper wrapper();

    IGameController gameController();

    TaskSupervisor taskSupervisor();

    IScriptEngineProvider scriptEngineProvider();

    ICommandMapHandler commandMapHandler();

    IEventRegistry eventRegistry();

    @Component.Builder
    interface Builder {
        BootstrapComponent build();

        // injects
        @BindsInstance
        Builder logger(Logger logger);

        @BindsInstance
        Builder dataFolder(@Named("DataFolder") File dataFolder);

        @BindsInstance
        Builder pluginInstance(@Named("PluginInstance") Object pluginInstance);

        @BindsInstance
        Builder commandInstance(ITriggerCommand commandInstance);

        @BindsInstance
        Builder scriptEngineManager(ScriptEngineManager scriptEngineManager);

        @BindsInstance
        Builder selfReference(SelfReference selfReference);

        @BindsInstance
        Builder wrapper(IWrapper wrapper);

        @BindsInstance
        Builder gameController(IGameController gameController);

        @BindsInstance
        Builder taskSupervisor(TaskSupervisor taskSupervisor);

        @BindsInstance
        Builder scriptEngineProvider(IScriptEngineProvider scriptEngineProvider);

        @BindsInstance
        Builder commandMapHandler(ICommandMapHandler commandMapHandler);

        @BindsInstance
        Builder eventRegistry(IEventRegistry eventRegistry);
    }
}
