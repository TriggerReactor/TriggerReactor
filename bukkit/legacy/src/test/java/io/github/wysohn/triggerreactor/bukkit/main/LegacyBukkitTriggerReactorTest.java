package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.components.BukkitPluginMainComponent;
import io.github.wysohn.triggerreactor.bukkit.components.DaggerBukkitPluginMainComponent;
import io.github.wysohn.triggerreactor.bukkit.tools.test.BukkitTestToolbox;
import io.github.wysohn.triggerreactor.core.components.PluginMainComponent;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LegacyBukkitTriggerReactorTest {
    BukkitPluginMainComponent bukkitPluginMainComponent;
    PluginMainComponent pluginMainComponent;

    @Before
    public void setUp() throws Exception {
        Server server = mock(Server.class);
        Plugin plugin = mock(Plugin.class);
        Map<String, Command> rawCommands = new HashMap<>();
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        when(plugin.getLogger()).thenReturn(mock(Logger.class));
        when(plugin.getDataFolder()).thenReturn(new File("build/tmp/test"));

        bukkitPluginMainComponent = DaggerBukkitPluginMainComponent.builder()
                .pluginMainModule(LegacyBukkitTriggerReactor.createModule(plugin, mock(CustomCommandHandle.class)))
                .inject(mock(JavaPlugin.class))
                .inject(server)
                .inject(BukkitTestToolbox.createCommand(plugin))
                .inject(rawCommands)
                .commandName("test")
                .permission("test.permission")
                .build();
        pluginMainComponent = bukkitPluginMainComponent.getMainBuilder().build();
    }

    @Test
    public void testOnEnable() throws Exception {
        TriggerReactor main = pluginMainComponent.getMain();

        main.onEnable();
    }
}