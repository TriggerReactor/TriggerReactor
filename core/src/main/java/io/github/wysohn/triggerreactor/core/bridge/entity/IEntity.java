package io.github.wysohn.triggerreactor.core.bridge.entity;

import io.github.wysohn.triggerreactor.core.bridge.IMinecraftObject;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;

import java.util.UUID;

public interface IEntity extends IMinecraftObject {

    UUID getUniqueId();

    boolean isDead();

    boolean isValid();

    SimpleLocation getLocation();
}
