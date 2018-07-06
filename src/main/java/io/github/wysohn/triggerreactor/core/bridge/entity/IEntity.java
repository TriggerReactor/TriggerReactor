package io.github.wysohn.triggerreactor.core.bridge.entity;

import java.util.UUID;

import io.github.wysohn.triggerreactor.core.bridge.IMinecraftObject;

public interface IEntity extends IMinecraftObject {

    UUID getUniqueId();

}
