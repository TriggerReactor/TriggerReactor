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
package io.github.wysohn.triggerreactor.bukkit.manager.location;

public class SimpleChunkLocation {
    String world;
    int i,j;
    public SimpleChunkLocation(String world, int i, int j) {
        super();
        this.world = world;
        this.i = i;
        this.j = j;
    }
    public SimpleChunkLocation(SimpleLocation sloc) {
        super();
        this.world = sloc.world;
        this.i = sloc.x >> 4;
        this.j = sloc.z >> 4;
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
            if (other.world != null)
                return false;
        } else if (!world.equals(other.world))
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "SimpleChunkLocation [world=" + world + ", i=" + i + ", j=" + j + "]";
    }

}
