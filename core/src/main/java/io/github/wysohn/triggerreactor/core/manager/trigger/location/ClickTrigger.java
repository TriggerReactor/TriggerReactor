package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import java.util.Map;

public class ClickTrigger extends Trigger {
    private final ClickHandler handler;

    public ClickTrigger(TriggerInfo info, String script, ClickHandler handler) throws
            AbstractTriggerManager.TriggerInitFailedException {
        super(info, script);
        this.handler = handler;

        init();
    }

    @Override
    public boolean activate(Object e, Map<String, Object> scriptVars) {
        if (!scriptVars.containsKey(LocationBasedTriggerManager.KEY_CONTEXT_ACTIVITY))
            throw new RuntimeException("ClickTrigger: Context activity not found in script variables");

        Activity activity = (Activity) scriptVars.get(
                LocationBasedTriggerManager.KEY_CONTEXT_ACTIVITY);
        if (!handler.allow(activity))
            return true;

        return super.activate(e, scriptVars);
    }

    @Override
    public Trigger clone() {
        try {
            //TODO: using same handler will be safe?
            return new ClickTrigger(info, script, handler);
        } catch (AbstractTriggerManager.TriggerInitFailedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
