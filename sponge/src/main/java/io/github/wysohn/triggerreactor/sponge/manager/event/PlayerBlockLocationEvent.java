package io.github.wysohn.triggerreactor.sponge.manager.event;

import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent;
import org.spongepowered.api.event.impl.AbstractEvent;

public class PlayerBlockLocationEvent extends AbstractEvent implements TargetPlayerEvent, Cancellable {

    private final Cause cause;
    private final Player player;
    private final SimpleLocation from;
    private final SimpleLocation to;

    private boolean cancelled = false;

    public PlayerBlockLocationEvent(Cause cause, Player player, SimpleLocation from, SimpleLocation to) {
        this.cause = cause;
        this.player = player;
        this.from = from;
        this.to = to;
    }

    public PlayerBlockLocationEvent(Player player, SimpleLocation from, SimpleLocation to) {
        EventContext context = EventContext.builder().add(EventContextKeys.PLAYER, player).build();
        this.cause = Cause.builder().append(player).build(context);
        this.player = player;
        this.from = from;
        this.to = to;
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    public SimpleLocation getFrom() {
        return from;
    }

    @Override
    public Player getTargetEntity() {
        return player;
    }

    public SimpleLocation getTo() {
        return to;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
