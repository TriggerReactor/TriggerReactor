package io.github.wysohn.triggerreactor.bukkit.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitBungeeCordHelper;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitMysqlSupport;
import io.github.wysohn.triggerreactor.bukkit.modules.BukkitPluginMainModule;
import io.github.wysohn.triggerreactor.bukkit.scope.JavaPluginLifetime;
import io.github.wysohn.triggerreactor.core.components.PluginMainComponent;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Named;
import java.util.Map;

@Component(modules = {BukkitPluginMainModule.class})
@JavaPluginLifetime
public interface BukkitPluginMainComponent {
    PluginMainComponent.Builder getMainBuilder();

    BukkitBungeeCordHelper getBungeeCordHelper();

    BukkitMysqlSupport getMysqlSupport();

    @Component.Builder
    interface Builder {
        BukkitPluginMainComponent build();

        Builder pluginMainModule(BukkitPluginMainModule module);

        @BindsInstance
        Builder inject(Server server);

        @BindsInstance
        Builder inject(JavaPlugin plugin);

        @BindsInstance
        Builder inject(PluginCommand command);

        @BindsInstance
        Builder inject(Map<String, Command> rawCommands);

        @BindsInstance
        Builder commandName(@Named("CommandName") String commandName);

        @BindsInstance
        Builder permission(@Named("Permission") String permission);
    }
}
