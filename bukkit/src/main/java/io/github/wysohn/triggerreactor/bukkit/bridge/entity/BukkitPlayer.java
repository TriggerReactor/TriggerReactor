package io.github.wysohn.triggerreactor.bukkit.bridge.entity;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitInventory;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitItemStack;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class BukkitPlayer extends BukkitEntity implements IPlayer {
    private final Player player;
    public BukkitPlayer(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public void sendMessage(String message) {
        entity.sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return entity.hasPermission(permission);
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public IInventory getInventory() {
        return new BukkitInventory(player.getInventory());
    }

    @Override
    public void openInventory(IInventory inventory) {
        player.openInventory((Inventory) inventory.get());
    }

    @Override
    public SimpleChunkLocation getChunk() {
        Location location = player.getLocation();
        return new SimpleChunkLocation(location.getWorld().getName(),
                                       location.getBlockX() >> 4,
                                       location.getBlockZ() >> 4);
    }

    @Override
    public IItemStack getItemInMainHand() {
        return new BukkitItemStack(player.getInventory().getItemInMainHand());
    }

    @Override
    public void setItemInMainHand(IItemStack iS) {
        player.getInventory().setItemInMainHand(iS.get());
    }
}
