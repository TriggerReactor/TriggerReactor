package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.components.DaggerGlobalVariableManagerTestComponent;
import io.github.wysohn.triggerreactor.components.GlobalVariableManagerTestComponent;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class GlobalVariableManagerTest {
    @Test
    public void testSingleton(){
        GlobalVariableManagerTestComponent component = DaggerGlobalVariableManagerTestComponent.builder()
                .build();

        assertSame(component.getGlobalVariableManager(), component.getGlobalVariableManager());
    }
}