package io.github.wysohn.triggerreactor.core.bridge;

public interface IWrapper {
    default <T, R extends IMinecraftObject> R wrap(T obj) {
        throw new RuntimeException(obj + " is not defined in ObjectFactory.");
    }
}
