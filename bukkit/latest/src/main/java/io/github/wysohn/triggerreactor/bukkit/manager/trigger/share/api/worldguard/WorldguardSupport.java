/*
 * Copyright (C) 2023. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.worldguard;

import com.google.inject.Injector;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.APISupportException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public class WorldguardSupport extends APISupport {
    static {
        addSharedVars("worldguard", WorldguardSupport.class);
    }

    private WorldGuard wg;

    public WorldguardSupport(Injector injector) {
        super(injector, "WorldGuard");
    }

    @Override
    public void init() throws APISupportException {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");

        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return;
        }

        wg = WorldGuard.getInstance();
    }

    private BlockVector3 toVector3(Location loc) {
        return BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * List all the names of regions that is covering the provided location
     *
     * @param loc location to check
     * @return set of region names. It may be empty but never be null.
     */
    public Set<String> getRegionNames(Location loc) {
        Set<String> names = new HashSet<>();

        RegionContainer container = wg.getPlatform().getRegionContainer();
        for (ProtectedRegion region : container.get(BukkitAdapter.adapt(loc.getWorld()))
                .getApplicableRegions(toVector3(loc)).getRegions()) {
            names.add(region.getId());
        }

        return names;
    }

    public boolean regionExists(World world, String regionName) {
        RegionContainer container = wg.getPlatform().getRegionContainer();
        return container.get(BukkitAdapter.adapt(world)).getRegion(regionName) != null;
    }

    /**
     * @param world
     * @param regionName
     * @return array of min/max locations. [0] for smallest, [1] for largest. Always length of 2.
     * returns null if provided region name doesn't exists.
     */
    public Location[] getRegion(World world, String regionName) {
        Location[] locs = new Location[2];

        RegionContainer container = wg.getPlatform().getRegionContainer();
        ProtectedRegion region = container.get(BukkitAdapter.adapt(world)).getRegion(regionName);
        if (region == null)
            return null;

        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        locs[0] = new Location(world, min.getX(), min.getY(), min.getZ());
        locs[1] = new Location(world, max.getX(), max.getY(), max.getZ());

        return locs;
    }
}
