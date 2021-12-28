package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.components.DaggerExternalAPITestComponent;
import io.github.wysohn.triggerreactor.components.DaggerPluginLifecycleTestComponent;
import io.github.wysohn.triggerreactor.components.ExternalAPITestComponent;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ExternalAPIManagerTest {
    ExternalAPITestComponent component;
    UUID uuid = UUID.randomUUID();

    @Before
    public void init() {
        component = DaggerExternalAPITestComponent.builder()
                .pluginLifecycleTestComponent(DaggerPluginLifecycleTestComponent.create())
                .build();
    }

    @Test
    public void testSingleton(){
        assertSame(component.manager(), component.manager());
    }

    @Test
    public void onDisable() {
    }

    @Test
    public void onEnable() throws Exception {
        ExternalAPIManager manager = component.manager();
        manager.onEnable();

        assertTrue(manager.getExternalAPIMap().containsKey("sample"));
    }

    @Test
    public void onReload() {
    }

    @Test
    public void getExternalAPIMap() {
    }
}