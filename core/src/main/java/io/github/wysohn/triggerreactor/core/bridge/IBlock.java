package io.github.wysohn.triggerreactor.core.bridge;

public interface IBlock extends IMinecraftObject {
    String getTypeName();

    ILocation getLocation();
}
