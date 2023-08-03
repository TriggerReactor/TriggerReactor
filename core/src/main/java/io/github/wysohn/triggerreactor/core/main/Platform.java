package io.github.wysohn.triggerreactor.core.main;

public enum Platform {

    CraftBukkit,
    Spigot,
    Paper,
    Sponge,
    Unknown;

    public boolean isBukkit() {
        return this == CraftBukkit || this == Spigot || this == Paper;
    }

    public boolean isSponge() {
        return this == Sponge;
    }

    public boolean isKnown() {
        return this != Unknown;
    }

    public boolean supports(final Dependency dependency) {
        return dependency.supportsBy(this) || dependency.supports();
    }
}
