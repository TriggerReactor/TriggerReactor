package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import io.github.wysohn.triggerreactor.components.BukkitEventRegistryManagerTestComponent;
import io.github.wysohn.triggerreactor.components.DaggerBukkitEventRegistryManagerTestComponent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class BukkitEventRegistryManagerTest {
    @Test
    public void testSingleton(){
        PluginManager pluginManager = mock(PluginManager.class);
        Plugin plugin = mock(Plugin.class);

        BukkitEventRegistryManagerTestComponent component = DaggerBukkitEventRegistryManagerTestComponent.builder()
                .pluginManager(pluginManager)
                .pluginInstance(plugin)
                .build();

        assertSame(component.eventRegistry(), component.eventRegistry());
    }
}