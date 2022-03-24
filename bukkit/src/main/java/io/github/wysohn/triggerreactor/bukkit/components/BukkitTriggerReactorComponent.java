package io.github.wysohn.triggerreactor.bukkit.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitTriggerReactor;
import io.github.wysohn.triggerreactor.bukkit.modules.BukkitPluginMainModule;
import io.github.wysohn.triggerreactor.bukkit.scope.BukkitPluginLifetime;
import io.github.wysohn.triggerreactor.core.components.PluginMainComponent;
import io.github.wysohn.triggerreactor.core.main.CommandHandler;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.modules.ConstantsModule;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

@Component(modules = {BukkitPluginMainModule.class,
                      ConstantsModule.class},
           dependencies = {PluginMainComponent.class})
@BukkitPluginLifetime
public interface BukkitTriggerReactorComponent {
    BukkitTriggerReactor bukkitTriggerReactor();

    CommandHandler commandHandler();

    IWrapper wrapper();

    @Component.Builder
    interface Builder {
        BukkitTriggerReactorComponent build();

        Builder mainComponent(PluginMainComponent component);

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
