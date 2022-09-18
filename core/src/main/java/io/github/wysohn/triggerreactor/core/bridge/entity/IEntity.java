package io.github.wysohn.triggerreactor.core.bridge.entity;

import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.IMinecraftObject;

import java.util.UUID;

public interface IEntity extends IMinecraftObject {

    UUID getUniqueId();
    boolean isDead();

    boolean isValid();

    ILocation getLocation();
}
