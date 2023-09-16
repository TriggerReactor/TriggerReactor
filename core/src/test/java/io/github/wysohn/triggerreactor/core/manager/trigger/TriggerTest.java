package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.manager.annotation.TriggerRuntimeDependency;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.AreaTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.EnterTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.area.ExitTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.CommandTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.CustomTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.ClickTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.WalkTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.named.NamedTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTrigger;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;

public class TriggerTest {
    private Set<Class<? extends Trigger>> triggerClasses = new HashSet<>();

    @Before
    public void setUpClass() {
        triggerClasses.add(ClickTrigger.class);
        triggerClasses.add(WalkTrigger.class);
        triggerClasses.add(CommandTrigger.class);
        triggerClasses.add(AreaTrigger.class);
        triggerClasses.add(EnterTrigger.class);
        triggerClasses.add(ExitTrigger.class);
        triggerClasses.add(NamedTrigger.class);
        triggerClasses.add(CustomTrigger.class);
        triggerClasses.add(InventoryTrigger.class);
        triggerClasses.add(RepeatingTrigger.class);
    }

    /**
     * Check if the trigger has any mutable fields, which is not intended,
     * and should use TriggerInfo instead.
     */
    @Test
    public void testMutableStates() {
        for (Class<? extends Trigger> triggerClass : triggerClasses) {
            Field[] fields = triggerClass.getDeclaredFields();
            for (Field field : fields) {
                // skip static fields
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                    continue;

                // skip final fields
                if (java.lang.reflect.Modifier.isFinal(field.getModifiers()))
                    continue;

                // skip injected fields
                if (field.isAnnotationPresent(Inject.class)
                        || field.isAnnotationPresent(com.google.inject.Inject.class)
                        || field.isAnnotationPresent(TriggerRuntimeDependency.class))
                    continue;

                fail(String.format("Trigger %s has mutable field %s", triggerClass.getName(), field.getName()));
            }
        }
    }
}
