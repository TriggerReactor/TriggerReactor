package io.github.wysohn.triggerreactor.manager.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 
 * @author wysohn
 * @deprecated just for copy
 */
public class EventBase extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
