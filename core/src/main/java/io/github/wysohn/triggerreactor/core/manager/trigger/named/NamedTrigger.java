package io.github.wysohn.triggerreactor.core.manager.trigger.named;


import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

public class NamedTrigger extends Trigger {
    @AssistedInject
    NamedTrigger(@Assisted TriggerInfo info, @Assisted String script) {
        super(info, script);
    }

    public NamedTrigger(Trigger o) {
        super(o);
        ValidationUtil.assertTrue(o, v -> v instanceof NamedTrigger);
    }
}
