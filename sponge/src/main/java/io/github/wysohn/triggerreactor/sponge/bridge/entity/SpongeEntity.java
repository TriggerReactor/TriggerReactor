package io.github.wysohn.triggerreactor.sponge.bridge.entity;

import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import org.spongepowered.api.entity.Entity;

import java.util.UUID;

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
