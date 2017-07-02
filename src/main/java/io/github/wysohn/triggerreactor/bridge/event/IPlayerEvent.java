package io.github.wysohn.triggerreactor.bridge.event;

import io.github.wysohn.triggerreactor.bridge.player.IPlayer;

public interface IPlayerEvent extends IEvent{
    IPlayer getIPlayer();
}
