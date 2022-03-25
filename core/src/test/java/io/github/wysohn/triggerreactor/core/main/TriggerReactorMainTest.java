package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.core.components.DaggerPluginMainComponent;
import io.github.wysohn.triggerreactor.core.components.PluginMainComponent;
import org.junit.Before;
import org.junit.Test;

public class TriggerReactorMainTest {

    PluginMainComponent component;

    @Before
    public void init(){
        component = DaggerPluginMainComponent.builder()

                .build();
    }

    @Test
    public void onEnable() {
        TriggerReactorMain main = component.getMain();
    }
}