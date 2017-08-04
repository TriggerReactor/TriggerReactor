package io.github.wysohn.triggerreactor.core.bridge.player;

import java.util.UUID;

import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;

public interface IPlayer extends ICommandSender{

    UUID getUniqueId();

    IInventory getInventory();

    void openInventory(IInventory inventory);

    SimpleChunkLocation getChunk();

    /**
     * Get item in this player's main hand.
     * @return IItemStack in the main hand; null if holding anything.
     */
    IItemStack getItemInMainHand();

    ILocation getLocation();

    void setItemInMainHand(IItemStack iS);

}
