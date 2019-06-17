package io.github.wysohn.triggerreactor.sponge.manager.event;

import io.github.wysohn.triggerreactor.sponge.main.TriggerReactor;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.impl.AbstractEvent;

public class TriggerReactorStopEvent extends AbstractEvent {
    private final Cause cause;

    public TriggerReactorStopEvent(TriggerReactor root) {
        super();
        EventContext context = EventContext.builder().build();
        this.cause = Cause.builder().append(root).build(context);
    }

    public TriggerReactorStopEvent(Cause cause) {
        super();
        this.cause = cause;
    }

    @Override
    public Cause getCause() {
        return cause;
    }

}
