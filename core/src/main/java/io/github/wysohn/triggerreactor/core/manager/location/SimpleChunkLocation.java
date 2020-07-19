/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
package io.github.wysohn.triggerreactor.core.manager.location;

public class SimpleChunkLocation {
    String world;
    int i, j;

    public SimpleChunkLocation(String world, int i, int j) {
        super();
        this.world = world;
        this.i = i;
        this.j = j;
    }

    public SimpleChunkLocation(String world, int x, int y, int z) {
        super();
        this.world = world;
        this.i = x >> 4;
        this.j = z >> 4;
    }

    public SimpleChunkLocation(String world, double x, double y, double z) {
        super();
        this.world = world;
        this.i = (int) x >> 4;
        this.j = (int) z >> 4;
    }

    public SimpleChunkLocation(SimpleLocation sloc) {
        super();
        this.world = sloc.world;
        this.i = sloc.x >> 4;
        this.j = sloc.z >> 4;
    }

    public static SimpleChunkLocation valueOf(String value) {
        String[] splitw = value.split("@", 2);
        if (splitw.length != 2)
            throw new SimpleChunkLocationFormatException(value);

        String world = splitw[0];

        String[] splitc = splitw[1].split(",", 2);
        if (splitc.length != 2)
            throw new SimpleChunkLocationFormatException(value);

        return new SimpleChunkLocation(world, Integer.parseInt(splitc[0]), Integer.parseInt(splitc[1]));
    }

    public String getWorld() {
        return world;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    /**
     * SimpleChunkLocation is a consistent class, so this method will return new
     * instance that added provided value to this instance.
     *
     * @param i x axis chunk coordinate to add
     * @param j z axis chunk coordinate to add
     * @return
     */
    public SimpleChunkLocation add(int i, int j) {
        return new SimpleChunkLocation(world, this.i + i, this.j + j);
    }

    public SimpleChunkLocation add(Vector dir) {
        return add((int) dir.getX(), (int) dir.getZ());
    }

    public double distance(SimpleChunkLocation other) {
        return Math.sqrt(i * other.i + j * other.j);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + i;
        result = prime * result + j;
        result = prime * result + ((world == null) ? 0 : world.hashCode());
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
        SimpleChunkLocation other = (SimpleChunkLocation) obj;
        if (i != other.i)
            return false;
        if (j != other.j)
            return false;
        if (world == null) {
            return other.world == null;
        } else return world.equals(other.world);
    }

    @Override
    public String toString() {
        return world + "@" + i + "," + j;
    }

    @SuppressWarnings("serial")
    public static class SimpleChunkLocationFormatException extends RuntimeException {

        public SimpleChunkLocationFormatException(String message) {
            super(message);
        }

    }
}