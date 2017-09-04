package io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.worldguard;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.generallib.pluginbase.PluginAPISupport.APISupport;
import org.generallib.pluginbase.PluginBase;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldguardSupport extends APISupport {
    private WorldGuardPlugin wg;
    public WorldguardSupport(PluginBase base) {
        super(base);
    }

    @Override
    public void init() throws Exception {
        Plugin plugin = base.getServer().getPluginManager().getPlugin("WorldGuard");

        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return;
        }

        wg = (WorldGuardPlugin) plugin;
    }

    /**
     * List all the names of regions that is covering the provided location
     * @param loc location to check
     * @return set of region names. It may be empty but never be null.
     */
    public Set<String> getRegionNames(Location loc){
        Set<String> names = new HashSet<>();

        for(ProtectedRegion region : wg.getRegionManager(loc.getWorld()).getApplicableRegions(loc).getRegions()){
            names.add(region.getId());
        }

        return names;
    }

    public boolean regionExists(World world, String regionName){
        return wg.getRegionManager(world).getRegion(regionName) != null;
    }

    /**
     *
     * @param world
     * @param regionName
     * @return array of min/max locations. [0] for smallest, [1] for largest. Always length of 2.
     * returns null if provided region name doesn't exists.
     */
    public Location[] getRegion(World world, String regionName){
        Location[] locs = new Location[2];

        ProtectedRegion region = wg.getRegionManager(world).getRegion(regionName);

        BlockVector min = region.getMinimumPoint();
        BlockVector max = region.getMaximumPoint();

        locs[0] = new Location(world, min.getX(), min.getY(), min.getZ());
        locs[1] = new Location(world, max.getX(), max.getY(), max.getZ());

        return locs;
    }
}
