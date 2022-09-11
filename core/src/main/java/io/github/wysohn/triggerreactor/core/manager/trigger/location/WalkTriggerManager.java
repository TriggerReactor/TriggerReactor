package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

public final class WalkTriggerManager extends LocationBasedTriggerManager<WalkTrigger> {

    public WalkTriggerManager(TriggerReactorCore plugin) {
        super(plugin,
              concatPath(plugin.getDataFolder(), "WalkTrigger"),
              new WalkTriggerLoader());
    }

    @Override
    public String getTriggerTypeName() {
        return "Walk";
    }

    @Override
    protected WalkTrigger newTrigger(TriggerInfo info, String script) throws TriggerInitFailedException {
        return new WalkTrigger(info, script);
    }

}
