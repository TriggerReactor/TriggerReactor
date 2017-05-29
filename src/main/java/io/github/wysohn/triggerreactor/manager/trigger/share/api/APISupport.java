package io.github.wysohn.triggerreactor.manager.trigger.share.api;

import io.github.wysohn.triggerreactor.main.TriggerReactor;

public abstract class APISupport {
    protected final TriggerReactor plugin;

    public APISupport(TriggerReactor plugin) {
        super();
        this.plugin = plugin;
    }

}
