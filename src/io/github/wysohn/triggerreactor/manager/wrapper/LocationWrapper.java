package io.github.wysohn.triggerreactor.manager.wrapper;

import org.bukkit.Location;

public class LocationWrapper extends Wrapper<Location> {
    String world;
    double x, y, z;
    LocationWrapper(Location target) {
        super(target);

        world = target.getWorld().getName();
        x = target.getX();
        y = target.getY();
        z = target.getZ();
    }

}
