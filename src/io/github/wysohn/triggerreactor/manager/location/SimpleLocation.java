package io.github.wysohn.triggerreactor.manager.location;

import org.bukkit.Location;

public class SimpleLocation implements Cloneable{
    String world;
    int x, y, z;
    public SimpleLocation(String world, int x, int y, int z) {
        super();
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public SimpleLocation(Location loc) {
        super();
        this.world = loc.getWorld().getName();
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
    }
    public String getWorld() {
        return world;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getZ() {
        return z;
    }
    public void add(int x, int y, int z){
        this.x += x;
        this.y += y;
        this.z += z;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((world == null) ? 0 : world.hashCode());
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleLocation other = (SimpleLocation) obj;
        if (world == null) {
            if (other.world != null)
                return false;
        } else if (!world.equals(other.world))
            return false;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        if (z != other.z)
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "SimpleLocation [world=" + world + ", x=" + x + ", y=" + y + ", z=" + z + "]";
    }

    @Override
    public SimpleLocation clone() {
        return new SimpleLocation(world, x, y, z);
    }
}