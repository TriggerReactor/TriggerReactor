package io.github.wysohn.triggerreactor.bukkit.manager.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TriggerReactorStartEvent extends Event{
	private static final HandlerList handlers = new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}