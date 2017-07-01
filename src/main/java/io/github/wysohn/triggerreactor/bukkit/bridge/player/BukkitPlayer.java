package io.github.wysohn.triggerreactor.bukkit.bridge.player;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import io.github.wysohn.triggerreactor.bridge.IInventory;
import io.github.wysohn.triggerreactor.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitInventory;

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
        player.sendMessage(message);
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

}
