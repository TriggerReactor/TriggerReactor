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
package io.github.wysohn.triggerreactor.sponge.tools;

import com.flowpowered.math.vector.Vector3i;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class LocationUtil {
    public static SimpleLocation convertToSimpleLocation(Location<World> loc) {
        return new SimpleLocation(loc.getExtent().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 0, 0);
    }

    public static Location<World> convertToBukkitLocation(SimpleLocation sloc) {
        World world = Sponge.getServer().getWorld(sloc.getWorld()).get();
        if (world == null)
            return null;

        int x = sloc.getX();
        int y = sloc.getY();
        int z = sloc.getZ();
        return world.getLocation(x + 0.5, y, z + 0.5);
    }

    public static SimpleChunkLocation convertToSimpleChunkLocation(Chunk chunk) {
        Vector3i pos = chunk.getPosition();
        return new SimpleChunkLocation(chunk.getWorld().getName(), pos.getX(), pos.getZ());
    }

    public static Chunk convertToBukkitChunk(SimpleChunkLocation scloc) {
        World world = Sponge.getServer().getWorld(scloc.getWorld()).get();
        if (world == null)
            return null;

        return world.getChunkAtBlock(scloc.getI(), 0, scloc.getJ()).get();
    }
}
