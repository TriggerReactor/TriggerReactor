package io.github.wysohn.triggerreactor.core.manager.trigger.named;

import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

public class NamedTrigger extends Trigger {
    public NamedTrigger(IThrowableHandler throwableHandler,
                        IGameController gameController,
                        TaskSupervisor taskSupervisor,
                        SelfReference selfReference,
                        TriggerInfo info,
                        String script) throws AbstractTriggerManager.TriggerInitFailedException {
        super(throwableHandler, gameController, taskSupervisor, selfReference, info, script);

        init();
    }

    @Override
    public NamedTrigger clone() {
        try {
            return new NamedTrigger(throwableHandler, gameController, taskSupervisor, selfReference, info, script);
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
