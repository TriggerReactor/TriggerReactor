package io.github.wysohn.triggerreactor.bridge.event;

import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;

public interface IPlayerBlockLocationEvent extends IPlayerEvent{

    public SimpleLocation getFrom();

    public SimpleLocation getTo();

    public boolean isCancelled();

    public void setCancelled(boolean b);

}