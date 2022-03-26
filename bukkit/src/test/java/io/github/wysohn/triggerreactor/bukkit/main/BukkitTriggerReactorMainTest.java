package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.components.BukkitPluginMainComponent;
import io.github.wysohn.triggerreactor.bukkit.components.DaggerBukkitPluginMainComponent;
import io.github.wysohn.triggerreactor.bukkit.modules.BukkitPluginMainModule;
import io.github.wysohn.triggerreactor.core.components.PluginMainComponent;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorMain;
import io.github.wysohn.triggerreactor.core.manager.IInventoryModifier;
import io.github.wysohn.triggerreactor.core.manager.IResourceProvider;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandMapHandler;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.IEventRegistry;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.IGUIOpenHelper;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptEngineManager;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BukkitTriggerReactorMainTest {

    Plugin plugin;

    BukkitPluginMainModule module;
    BukkitPluginMainComponent component;

    @Before
    public void setUp() throws Exception {
        plugin = mock(Plugin.class);
        when(plugin.getLogger()).thenReturn(mock(Logger.class));
        when(plugin.getDataFolder()).thenReturn(new File("build/tmp/test"));

        module = new BukkitPluginMainModule(
                plugin,
                mock(IWrapper.class),
                mock(SelfReference.class),
                mock(IGameController.class),
                mock(IInventoryModifier.class),
                mock(IGUIOpenHelper.class),
                mock(IPluginLifecycleController.class),
                mock(TaskSupervisor.class),
                new ScriptEngineManager(),
                mock(ICommandMapHandler.class),
                mock(IEventRegistry.class),
                mock(IResourceProvider.class));

        component = DaggerBukkitPluginMainComponent.builder()
                .pluginMainModule(module)
                .inject(createCommand(plugin))
                .build();
    }

    private PluginCommand createCommand(Plugin plugin) throws Exception {
        Constructor<PluginCommand> con = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        con.setAccessible(true);
        return con.newInstance("", plugin);
    }

    @Test
    public void onEnable() throws Exception {
        PluginMainComponent mainComponent = component.getMainBuilder().build();
        TriggerReactorMain main = mainComponent.getMain();
        main.onEnable();
    }
}