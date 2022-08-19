package io.github.wysohn.triggerreactor.core.manager.location; /*******************************************************************************
 *     Copyright (C) 2017 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

import java.util.function.Function;

public class SimpleLocation implements Cloneable {
    String world;
    int x, y, z;
    float pitch, yaw;

    public SimpleLocation(String world, int x, int y, int z) {
        super();
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SimpleLocation(String world, double x, double y, double z) {
        super();
        this.world = world;
        this.x = (int) x;
        this.y = (int) y;
        this.z = (int) z;
    }

    public SimpleLocation(String world, int x, int y, int z, float pitch, float yaw) {
        super();
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public static SimpleLocation valueOf(String str) {
        String[] splitw = str.split("@", 2);
        if (splitw.length != 2)
            throw new SimpleLocationFormatException(str);

        String world = splitw[0];

        String[] splitl = splitw[1].split(",", 3);
        if (splitl.length != 3)
            throw new SimpleLocationFormatException(str);

        return new SimpleLocation(world, Integer.parseInt(splitl[0]), Integer.parseInt(splitl[1]),
                                  Integer.parseInt(splitl[2]));
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

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public SimpleLocation add(int x, int y, int z) {
        return new SimpleLocation(this.world, this.x + x, this.y + y, this.z + z);
    }

    public SimpleLocation add(Vector vector) {
        return add((int) vector.getX(), (int) vector.getY(), (int) vector.getZ());
    }

    public <T> T transform(Function<SimpleLocation, T> fn) {
        return fn.apply(this.clone());
    }

    public SimpleLocation add(SimpleLocation r) {
        return add(r.x, r.y, r.z);
    }

    public SimpleLocation sub(int x, int y, int z) {
        return new SimpleLocation(this.world, this.x - x, this.y - y, this.z - z);
    }

    public SimpleLocation sub(SimpleLocation r) {
        return sub(r.x, r.y, r.z);
    }

    public int coordinatesSum() {
        return x + y + z;
    }

    public SimpleChunkLocation toChunk() {
        return new SimpleChunkLocation(this);
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
        return z == other.z;
    }

    @Override
    public String toString() {
        return world + "@" + x + "," + y + "," + z;
    }

    @Override
    public SimpleLocation clone() {
        return new SimpleLocation(world, x, y, z);
    }

    @SuppressWarnings("serial")
    public static class SimpleLocationFormatException extends RuntimeException {
        public SimpleLocationFormatException(String message) {
            super(message);
        }
    }
}