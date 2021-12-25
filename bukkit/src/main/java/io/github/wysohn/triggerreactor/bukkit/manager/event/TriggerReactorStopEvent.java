package io.github.wysohn.triggerreactor.bukkit.manager.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TriggerReactorStopEvent extends Event {
    public HandlerList getHandlers() {
        return handlers;
    }
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

}