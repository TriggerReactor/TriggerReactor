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
package io.github.wysohn.triggerreactor.bukkit.tools;

import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtil {
    public static SimpleLocation convertToSimpleLocation(Location loc) {
        return new SimpleLocation(loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ(),
                loc.getPitch(),
                loc.getYaw());
    }

    public static Location convertToBukkitLocation(SimpleLocation sloc) {
        World world = Bukkit.getWorld(sloc.getWorld());
        int x = sloc.getX();
        int y = sloc.getY();
        int z = sloc.getZ();
        return new Location(world, x + 0.5, y, z + 0.5);
    }

    public static SimpleChunkLocation convertToSimpleChunkLocation(Chunk chunk) {
        return new SimpleChunkLocation(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static Chunk convertToBukkitChunk(SimpleChunkLocation scloc) {
        return Bukkit.getWorld(scloc.getWorld()).getChunkAt(scloc.getI(), scloc.getJ());
    }
}
