package io.github.wysohn.triggerreactor.core.bridge.event;

import io.github.wysohn.triggerreactor.core.bridge.player.IPlayer;

public interface IPlayerEvent extends IEvent{
    IPlayer getIPlayer();
}
