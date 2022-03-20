package io.github.wysohn.triggerreactor.bukkit.bridge.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CommandSenderEvent extends Event {
    public final CommandSender sender;

    public CommandSenderEvent(CommandSender sender) {
        super();
        this.sender = sender;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }

}
