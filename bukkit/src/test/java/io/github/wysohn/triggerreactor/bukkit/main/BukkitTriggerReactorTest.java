package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.components.BukkitPluginMainComponent;
import io.github.wysohn.triggerreactor.bukkit.components.DaggerBukkitPluginMainComponent;
import io.github.wysohn.triggerreactor.bukkit.modules.BukkitPluginMainModule;
import io.github.wysohn.triggerreactor.bukkit.tools.test.BukkitTestToolbox;
import io.github.wysohn.triggerreactor.core.components.PluginMainComponent;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandMapHandler;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptEngineManager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BukkitTriggerReactorTest {

    Server server;
    Plugin plugin;
    Map<String, Command> rawCommands = new HashMap<>();

    BukkitPluginMainModule module;
    BukkitPluginMainComponent component;

    @Before
    public void setUp() throws Exception {
        server = mock(Server.class);
        plugin = mock(Plugin.class);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        when(plugin.getLogger()).thenReturn(mock(Logger.class));
        when(plugin.getDataFolder()).thenReturn(new File("build/tmp/test"));

        module = new BukkitPluginMainModule(
                plugin,
                mock(IWrapper.class),
                mock(SelfReference.class),
                new ScriptEngineManager(),
                new HashMap<>(),
                mock(ICommandMapHandler.class));

        component = DaggerBukkitPluginMainComponent.builder()
                .pluginMainModule(module)
                .inject(server)
                .inject(mock(JavaPlugin.class))
                .inject(BukkitTestToolbox.createCommand(plugin))
                .inject(rawCommands)
                .commandName("test")
                .permission("test")
                .build();
    }

    @Test
    public void onEnable() throws Exception {
        PluginMainComponent mainComponent = component.getMainBuilder().build();
        TriggerReactor main = mainComponent.getMain();
        main.onEnable();
    }
}