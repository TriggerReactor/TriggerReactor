package io.github.wysohn.triggerreactor.bridge;

public interface IItemStack extends IMinecraftObject, Cloneable {

    int getTypeId();

    IItemStack clone();

}
