package io.github.wysohn.triggerreactor.bridge.player;

import java.util.UUID;

import io.github.wysohn.triggerreactor.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.bridge.IInventory;

public interface IPlayer extends ICommandSender{

    @Override
    <T> T get();

    UUID getUniqueId();

    IInventory getInventory();

    void openInventory(IInventory inventory);

}
