package io.github.wysohn.triggerreactor.bukkit.bridge.entity;

import java.util.UUID;

import org.bukkit.entity.Entity;

import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;

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

}
