package io.github.wysohn.triggerreactor.bukkit.bridge.entity;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitLocation;
import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class BukkitEntity implements IEntity {
    private final Entity entity;

    public BukkitEntity(Entity entity) {
        super();
        this.entity = entity;
    }

    @Override
    public <T> T get() {
        return (T) entity;
    }

    @Override
    public UUID getUniqueId() {
        return entity.getUniqueId();
    }

    @Override
    public boolean isDead() {
        return entity.isDead();
    }

    @Override
    public boolean isValid() {
        return entity.isValid();
    }

    @Override
    public ILocation getLocation() {
        return new BukkitLocation(entity.getLocation());
    }

}
