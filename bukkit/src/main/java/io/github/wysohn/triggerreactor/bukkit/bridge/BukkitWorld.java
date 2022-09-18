package io.github.wysohn.triggerreactor.bukkit.bridge;

import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitEntity;
import io.github.wysohn.triggerreactor.core.bridge.IBlock;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.bukkit.World;

import java.util.stream.Collectors;

public class BukkitWorld implements IWorld {
    private final World world;

    public BukkitWorld(World world) {
        this.world = world;
    }

    @Override
    public Iterable<IEntity> getEntities() {
        return world.getEntities().stream()
                .map(BukkitEntity::new)
                .collect(Collectors.toList());
    }

    @Override
    public IBlock getBlock(SimpleLocation clicked) {
        return new BukkitBlock(world.getBlockAt(clicked.getX(), clicked.getY(), clicked.getZ()));
    }

    @Override
    public IBlock getBlock(ILocation location) {
        return getBlock(location.toSimpleLocation());
    }
}
