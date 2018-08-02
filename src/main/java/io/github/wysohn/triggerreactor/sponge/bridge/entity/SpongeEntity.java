package io.github.wysohn.triggerreactor.sponge.bridge.entity;

import java.util.UUID;

import org.spongepowered.api.entity.Entity;

import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;

public class SpongeEntity implements IEntity {
    private final Entity entity;

    public SpongeEntity(Entity entity) {
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
