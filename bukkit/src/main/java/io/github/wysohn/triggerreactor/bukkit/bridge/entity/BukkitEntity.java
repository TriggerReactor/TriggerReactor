package io.github.wysohn.triggerreactor.bukkit.bridge.entity;

import io.github.wysohn.triggerreactor.core.bridge.entity.IEntity;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class BukkitEntity implements IEntity {
    protected final IWrapper wrapper;
    private final Entity entity;

    public BukkitEntity(IWrapper wrapper, Entity entity) {
        this.wrapper = wrapper;
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
