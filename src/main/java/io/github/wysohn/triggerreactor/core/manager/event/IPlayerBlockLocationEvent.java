package io.github.wysohn.triggerreactor.core.manager.event;

import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;

public interface IPlayerBlockLocationEvent extends IPlayerEvent{

    public SimpleLocation getFrom();

    public SimpleLocation getTo();

}