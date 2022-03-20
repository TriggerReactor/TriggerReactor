package io.github.wysohn.triggerreactor.bukkit.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitTriggerReactor;
import io.github.wysohn.triggerreactor.bukkit.modules.*;
import io.github.wysohn.triggerreactor.core.main.CommandHandler;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.modules.*;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Singleton;
import java.util.Map;

@Component(modules = {CommandModule.class,
                      CoreUtilModule.class,
                      CoreScriptEngineInitializerModule.class,
                      CoreExternalAPIModule.class,
                      CoreTabCompleterModule.class,
                      BukkitPluginLifecycleModule.class,
                      BukkitGameControllerModule.class,
                      BukkitPluginMainModule.class,
                      BukkitManagerModule.class,
                      BukkitScriptEngineModule.class,
                      BukkitUtilModule.class,
                      ConfigSourceFactoryModule.class,
                      ConstantsModule.class})
@Singleton
public interface BukkitTriggerReactorComponent {
    BukkitTriggerReactor bukkitTriggerReactor();

    CommandHandler commandHandler();

    IWrapper wrapper();

    @Component.Builder
    interface Builder{
        BukkitTriggerReactorComponent build();

        @BindsInstance
        Builder inject(IWrapper wrapper);

        @BindsInstance
        Builder inject(SelfReference selfReference);

        @BindsInstance
        Builder inject(JavaPlugin plugin);

        @BindsInstance
        Builder inject(PluginCommand command);

        @BindsInstance
        Builder inject(CommandExecutor executor);

        @BindsInstance
        Builder inject(Map<String, Command> rawCommands);
    }
}
