package io.github.wysohn.triggerreactor.sponge.bridge;

import org.spongepowered.api.world.Location;

import io.github.wysohn.triggerreactor.core.bridge.ILocation;

public class SpongeLocation implements ILocation {
    private final Location location;

    public SpongeLocation(Location location) {
        super();
        this.location = location;
    }

    @Override
    public <T> T get() {
        return (T) location;
    }

}
