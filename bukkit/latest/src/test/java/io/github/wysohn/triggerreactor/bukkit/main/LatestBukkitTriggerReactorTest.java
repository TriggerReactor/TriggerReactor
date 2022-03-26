package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.bukkit.components.BukkitPluginMainComponent;
import io.github.wysohn.triggerreactor.bukkit.components.DaggerBukkitPluginMainComponent;
import org.junit.Before;
import org.junit.Test;

public class LatestBukkitTriggerReactorTest {
    BukkitPluginMainComponent mainComponent;

    @Before
    public void setUp() throws Exception {
        mainComponent = DaggerBukkitPluginMainComponent.builder().build();
    }

    @Test
    public void testOnEnable(){
        BukkitTriggerReactorMain main = mainComponent.bukkitTriggerReactor();
        
        main.onEnable();
    }
}