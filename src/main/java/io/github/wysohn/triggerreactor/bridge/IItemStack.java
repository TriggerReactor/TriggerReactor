package io.github.wysohn.triggerreactor.bridge;

public interface IItemStack extends IMinecraftObject, Cloneable {

    /**
     * Returns the item type. It varies on the platform. (Material for Bukkit API for example)
     * @return
     */
    <T> T getType();

    IItemStack clone();

}
