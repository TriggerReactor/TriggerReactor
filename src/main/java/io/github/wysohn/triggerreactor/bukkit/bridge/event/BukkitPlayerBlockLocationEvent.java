package io.github.wysohn.triggerreactor.bukkit.bridge.event;

import io.github.wysohn.triggerreactor.bridge.event.IPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.bukkit.bridge.player.BukkitPlayer;
import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;

public class BukkitPlayerBlockLocationEvent implements IPlayerBlockLocationEvent {
    private final PlayerBlockLocationEvent event;

    public BukkitPlayerBlockLocationEvent(PlayerBlockLocationEvent playerBlockLocationEvent) {
        super();
        event = playerBlockLocationEvent;
    }

    @Override
    public IPlayer getIPlayer() {
        return new BukkitPlayer(event.getPlayer());
    }

    @Override
    public SimpleLocation getFrom() {
        return event.getFrom();
    }

    @Override
    public SimpleLocation getTo() {
        return event.getTo();
    }

    @Override
    public <T> T get() {
        return (T) event;
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean b) {
        event.setCancelled(true);
    }

}
