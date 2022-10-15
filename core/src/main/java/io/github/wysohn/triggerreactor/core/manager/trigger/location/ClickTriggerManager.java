package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

public final class ClickTriggerManager extends LocationBasedTriggerManager<ClickTrigger> {

    public ClickTriggerManager(TriggerReactorCore plugin, ITriggerLoader<ClickTrigger> loader) {
        super(plugin,
              concatPath(plugin.getDataFolder(), "ClickTrigger"),
              loader);
    }

    public ClickTriggerManager(TriggerReactorCore plugin){
        this(plugin, new ClickTriggerLoader());
    }

    @Override
    public String getTriggerTypeName() {
        return "Click";
    }

    @Override
    protected ClickTrigger newTrigger(TriggerInfo info, String script) throws TriggerInitFailedException {
        return ClickTriggerLoader.newInstance(info, script);
    }

}
