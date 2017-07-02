package io.github.wysohn.triggerreactor.bukkit.bridge;

import org.bukkit.Location;

import io.github.wysohn.triggerreactor.bridge.ILocation;

public class BukkitLocation implements ILocation {
    private final Location location;
    public BukkitLocation(Location location) {
        this.location = location;
    }

    @Override
    public <T> T get() {
        return (T) location;
    }

}
