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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.worldguard;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.APISupportException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public class WorldguardSupport extends APISupport {
    private WorldGuardPlugin wg;

    public WorldguardSupport(TriggerReactorCore plugin) {
        super(plugin, "WorldGuard");
    }

    @Override
    public void init() throws APISupportException {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");

        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return;
        }

        wg = (WorldGuardPlugin) plugin;
    }

    /**
     * List all the names of regions that is covering the provided location
     *
     * @param loc location to check
     * @return set of region names. It may be empty but never be null.
     */
    public Set<String> getRegionNames(Location loc) {
        Set<String> names = new HashSet<>();

        for (ProtectedRegion region : wg.getRegionManager(loc.getWorld()).getApplicableRegions(loc).getRegions()) {
            names.add(region.getId());
        }

        return names;
    }

    public boolean regionExists(World world, String regionName) {
        return wg.getRegionManager(world).getRegion(regionName) != null;
    }

    /**
     * @param world
     * @param regionName
     * @return array of min/max locations. [0] for smallest, [1] for largest. Always length of 2.
     * returns null if provided region name doesn't exists.
     */
    public Location[] getRegion(World world, String regionName) {
        Location[] locs = new Location[2];

        ProtectedRegion region = wg.getRegionManager(world).getRegion(regionName);
        if (region == null)
            return null;

        BlockVector min = region.getMinimumPoint();
        BlockVector max = region.getMaximumPoint();

        locs[0] = new Location(world, min.getX(), min.getY(), min.getZ());
        locs[1] = new Location(world, max.getX(), max.getY(), max.getZ());

        return locs;
    }
}
