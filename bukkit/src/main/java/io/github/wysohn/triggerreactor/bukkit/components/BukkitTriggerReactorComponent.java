package io.github.wysohn.triggerreactor.bukkit.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitTriggerReactor;
import io.github.wysohn.triggerreactor.bukkit.modules.BukkitPluginMainModule;
import io.github.wysohn.triggerreactor.bukkit.scope.BukkitPluginChildLifetime;
import io.github.wysohn.triggerreactor.core.components.PluginMainComponent;
import io.github.wysohn.triggerreactor.core.main.CommandHandler;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

@Component(modules = {BukkitPluginMainModule.class},
           dependencies = {PluginMainComponent.class,
                           CommandComponent.class,
                           TabCompleterComponent.class,
                           ConstantsComponent.class})
@BukkitPluginChildLifetime
public interface BukkitTriggerReactorComponent {
    BukkitTriggerReactor bukkitTriggerReactor();

    // PluginMainComponent
    CommandHandler commandHandler();

    IWrapper wrapper();

    SelfReference selfReference();

    JavaPlugin plugin();

    PluginCommand pluginCommand();

    Map<String, Command> commands();

    @Component.Builder
    interface Builder {
        BukkitTriggerReactorComponent build();

        Builder pluginMainComponent(PluginMainComponent pluginMainComponent);

        Builder commandComponent(CommandComponent commandComponent);

        Builder tabCompleterComponent(TabCompleterComponent tabCompleterComponent);

        Builder constantsComponent(ConstantsComponent constantsComponent);

        @BindsInstance
        Builder pluginCommand(PluginCommand pluginCommand);

        @BindsInstance
        Builder rawCommands(Map<String, Command> commands);
    }
}
