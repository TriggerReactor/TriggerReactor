package io.github.wysohn.triggerreactor.bukkit.tools;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;

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
        int z= sloc.getZ();
        return new Location(world, x + 0.5, y, z + 0.5);
    }

    public static SimpleChunkLocation convertToSimpleChunkLocation(Chunk chunk) {
        return new SimpleChunkLocation(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static Chunk convertToBukkitChunk(SimpleChunkLocation scloc){
        return Bukkit.getWorld(scloc.getWorld()).getChunkAt(scloc.getI(), scloc.getJ());
    }
}
