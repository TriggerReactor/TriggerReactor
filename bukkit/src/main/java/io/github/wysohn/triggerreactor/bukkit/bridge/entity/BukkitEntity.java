package io.github.wysohn.triggerreactor.bukkit.bridge.entity;

import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class BukkitEntity implements IEntity {
    private final Entity entity;

    public BukkitEntity(Entity entity) {
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
    public SimpleLocation getLocation() {
        Location location = entity.getLocation();
        return new SimpleLocation(location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getPitch(),
                location.getYaw());
    }


}
