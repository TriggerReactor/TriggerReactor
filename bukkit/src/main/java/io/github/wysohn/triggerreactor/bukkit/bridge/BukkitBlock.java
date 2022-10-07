package io.github.wysohn.triggerreactor.bukkit.bridge;

import io.github.wysohn.triggerreactor.core.bridge.IBlock;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import org.bukkit.block.Block;

public class BukkitBlock implements IBlock {
    private final Block block;

    public BukkitBlock(Block block) {
        this.block = block;
    }

    @Override
    public String getTypeName() {
        return block.getType().name();
    }

    @Override
    public ILocation getLocation() {
        return new BukkitLocation(block.getLocation());
    }

    @Override
    public <T> T get() {
        return (T) block;
    }
}
