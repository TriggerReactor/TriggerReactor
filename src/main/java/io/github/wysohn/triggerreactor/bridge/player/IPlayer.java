package io.github.wysohn.triggerreactor.bridge.player;

import java.util.UUID;

import io.github.wysohn.triggerreactor.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.bridge.IInventory;
import io.github.wysohn.triggerreactor.bridge.IItemStack;
import io.github.wysohn.triggerreactor.bridge.ILocation;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleChunkLocation;

public interface IPlayer extends ICommandSender{

    @Override
    <T> T get();

    UUID getUniqueId();

    IInventory getInventory();

    void openInventory(IInventory inventory);

    SimpleChunkLocation getChunk();

    IItemStack getItemInMainHand();

    ILocation getLocation();

    void setItemInMainHand(IItemStack iS);

}
