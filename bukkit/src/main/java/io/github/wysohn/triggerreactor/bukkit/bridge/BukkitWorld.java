package io.github.wysohn.triggerreactor.bukkit.bridge;

import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitEntity;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
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
}
