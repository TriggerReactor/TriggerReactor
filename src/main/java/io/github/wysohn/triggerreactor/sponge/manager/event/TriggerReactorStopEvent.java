package io.github.wysohn.triggerreactor.sponge.manager.event;

import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.impl.AbstractEvent;

public class TriggerReactorStopEvent extends AbstractEvent {
	private final Cause cause;

	public TriggerReactorStopEvent() {
		super();
		EventContext context = EventContext.builder().build();
		this.cause = Cause.builder().build(context);
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
