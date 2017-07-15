package io.github.wysohn.triggerreactor.bukkit.bridge.player;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import io.github.wysohn.triggerreactor.bridge.IInventory;
import io.github.wysohn.triggerreactor.bridge.IItemStack;
import io.github.wysohn.triggerreactor.bridge.ILocation;
import io.github.wysohn.triggerreactor.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitInventory;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitItemStack;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitLocation;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import net.md_5.bungee.api.ChatColor;

public class BukkitPlayer implements IPlayer {
    private final Player player;

    public BukkitPlayer(Player player) {
        super();
        this.player = player;
    }

    @Override
    public <T> T get() {
        return (T) player;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public IInventory getInventory() {
        return new BukkitInventory(player.getInventory());
    }

    @Override
    public void openInventory(IInventory inventory) {
        Inventory inv = inventory.get();
        player.openInventory(inv);
    }

    @Override
    public SimpleChunkLocation getChunk() {
        return LocationUtil.convertToSimpleChunkLocation(player.getLocation().getChunk());
    }

    @Override
    public IItemStack getItemInMainHand() {
        return new BukkitItemStack(player.getInventory().getItemInMainHand());
    }

    @Override
    public ILocation getLocation() {
        return new BukkitLocation(player.getLocation());
    }

    @Override
    public void setItemInMainHand(IItemStack iS) {
        player.getInventory().setItemInMainHand(iS.get());
    }

}
