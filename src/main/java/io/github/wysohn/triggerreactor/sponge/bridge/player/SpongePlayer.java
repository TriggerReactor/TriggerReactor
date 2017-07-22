package io.github.wysohn.triggerreactor.sponge.bridge.player;

import java.util.UUID;

import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;

import io.github.wysohn.triggerreactor.bridge.IInventory;
import io.github.wysohn.triggerreactor.bridge.IItemStack;
import io.github.wysohn.triggerreactor.bridge.ILocation;
import io.github.wysohn.triggerreactor.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeInventory;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeItemStack;
import io.github.wysohn.triggerreactor.sponge.bridge.SpongeLocation;

public class SpongePlayer implements IPlayer {
    private final Player player;

    public SpongePlayer(Player player) {
        super();
        this.player = player;
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
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
    public IInventory getInventory() {
        return new SpongeInventory(player.getInventory());
    }

    @Override
    public void openInventory(IInventory inventory) {
        player.openInventory(inventory.get(), Cause.builder().build());
    }

    @Override
    public SimpleChunkLocation getChunk() {
        World world = player.getLocation().getExtent();
        Vector3i vector = player.getLocation().getChunkPosition();
        return new SimpleChunkLocation(world.getName(), vector.getX(), vector.getZ());
    }

    @Override
    public IItemStack getItemInMainHand() {
        return new SpongeItemStack(player.getItemInHand(HandTypes.MAIN_HAND).get());
    }

    @Override
    public ILocation getLocation() {
        return new SpongeLocation(player.getLocation());
    }

    @Override
    public void setItemInMainHand(IItemStack iS) {
        player.setItemInHand(HandTypes.MAIN_HAND, iS.get());
    }

}
