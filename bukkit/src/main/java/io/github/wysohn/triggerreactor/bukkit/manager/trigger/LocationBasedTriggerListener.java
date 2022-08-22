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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitLocation;
import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitPlayer;
import io.github.wysohn.triggerreactor.bukkit.main.BukkitTriggerReactorCore;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.LocationBasedTriggerManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;

abstract class LocationBasedTriggerListener<T extends Trigger, M extends LocationBasedTriggerManager<T>>
        implements BukkitTriggerManager {
    public static final Material INSPECTION_TOOL = Material.BONE;
    public static final Material CUT_TOOL = Material.SHEARS;
    public static final Material COPY_TOOL = Material.PAPER;

    final M manager;

    public LocationBasedTriggerListener(M manager) {
        this.manager = manager;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(PlayerInteractEvent e) {
        if (!BukkitUtil.isLeftHandClick(e))
            return;

        Player player = e.getPlayer();
        ItemStack IS = player.getInventory().getItemInHand();
        Block clicked = e.getClickedBlock();
        if (clicked == null)
            return;

        BukkitLocation locationWrapped = new BukkitLocation(clicked.getLocation());
        BukkitPlayer playerWrapped = new BukkitPlayer(player);

        T trigger = manager.getTriggerForLocation(locationWrapped);
        IPlayer bukkitPlayer = BukkitTriggerReactorCore.getWrapper().wrap(player);

        if (IS != null && !e.isCancelled() && player.hasPermission("triggerreactor.admin")) {

            if (IS.getType() == INSPECTION_TOOL) {
                if (trigger != null && e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    manager.removeTriggerForLocation(locationWrapped);

                    player.sendMessage(ChatColor.GREEN + "A trigger has deleted.");
                    e.setCancelled(true);
                } else if (trigger != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (e.getPlayer().isSneaking()) {
                        manager.handleScriptEdit(playerWrapped, trigger);
                        e.setCancelled(true);
                    } else {
                        manager.showTriggerInfo(bukkitPlayer, locationWrapped);
                        e.setCancelled(true);
                    }
                }
            } else {
                if (IS.getType() == CUT_TOOL) {
                    if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                        if (manager.pasteTrigger(playerWrapped, locationWrapped)) {
                            player.sendMessage(ChatColor.GREEN + "Successfully pasted the trigger!");
                            manager.showTriggerInfo(bukkitPlayer, locationWrapped);
                            e.setCancelled(true);
                        }
                    } else if (trigger != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (manager.cutTrigger(playerWrapped, locationWrapped)) {
                            player.sendMessage(ChatColor.GREEN + "Cut Complete!");
                            player.sendMessage(ChatColor.GREEN + "Now you can paste it by left click on any block!");
                            e.setCancelled(true);
                        }
                    }
                } else if (IS.getType() == COPY_TOOL) {
                    if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                        if (manager.pasteTrigger(playerWrapped, locationWrapped)) {
                            player.sendMessage(ChatColor.GREEN + "Successfully pasted the trigger!");
                            manager.showTriggerInfo(bukkitPlayer, locationWrapped);
                            e.setCancelled(true);
                        }
                    } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (trigger != null && manager.copyTrigger(playerWrapped,
                                                                   locationWrapped)) {
                            player.sendMessage(ChatColor.GREEN + "Copy Complete!");
                            player.sendMessage(ChatColor.GREEN + "Now you can paste it by left click on any block!");
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }

        if (!e.isCancelled() && manager.isLocationSetting(bukkitPlayer)) {
            manager.handleLocationSetting(locationWrapped, playerWrapped);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onWallSignBreak(BlockBreakEvent e) {
        if (e.isCancelled())
            return;

        Block block = e.getBlock();
        SimpleLocation sloc = new SimpleLocation(block.getWorld().getName(),
                                                 block.getX(),
                                                 block.getY(),
                                                 block.getZ());
        for (SimpleLocation surrounding : manager.getSurroundingBlocks(sloc, this::isWallSign)) {
            World world = block.getWorld();
            Block target = world.getBlockAt(surrounding.getX(), surrounding.getY(), surrounding.getZ());
            BlockBreakEvent bbe = new BlockBreakEvent(target, e.getPlayer());
            onBreak(bbe);

            // at least one sign is not breakable, so cancel the event and stop iteration
            if (bbe.isCancelled()) {
                e.setCancelled(true);
                break;
            }
        }
    }

    private boolean isWallSign(SimpleLocation sloc) {
        World world = Bukkit.getWorld(sloc.getWorld());
        Block block = world.getBlockAt(sloc.getX(), sloc.getY(), sloc.getZ());
        return block.getType() == Material.WALL_SIGN;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSignBreak(BlockBreakEvent e) {
        if (e.isCancelled())
            return;

        Block block = e.getBlock();
        Block above = block.getRelative(BlockFace.UP);

        // check if this break event of the block
        // will cause the destruction of sign above it
        if (!above.getType().name().equals("SIGN_POST")
                && !above.getType().name().endsWith("_SIGN"))
            return;

        BlockBreakEvent bbe = new BlockBreakEvent(above, e.getPlayer());
        onBreak(bbe);
        e.setCancelled(bbe.isCancelled());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Block block = e.getBlock();

        T trigger = manager.getTriggerForLocation(new BukkitLocation(block.getLocation()));
        if (trigger == null)
            return;

        Player player = e.getPlayer();

        player.sendMessage(ChatColor.GRAY + "Cannot break trigger block.");
        player.sendMessage(ChatColor.GRAY + "To remove trigger, hold inspection tool " + INSPECTION_TOOL.name());
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemSwap(PlayerItemHeldEvent e) {
        manager.onItemSwap(new BukkitPlayer(e.getPlayer()));
    }

    protected Set<Map.Entry<SimpleLocation, Trigger>> getTriggersInChunk(Chunk chunk) {
        SimpleChunkLocation scLoc = new SimpleChunkLocation(chunk.getWorld().getName(),
                                                            chunk.getX(),
                                                            chunk.getZ());
        return manager.getTriggersInChunk(scLoc);
    }

    public static LocationBasedTriggerManager.Activity toActivity(Action action) {
        switch (action) {
            case LEFT_CLICK_AIR:
                return LocationBasedTriggerManager.Activity.LEFT_CLICK_AIR;
            case LEFT_CLICK_BLOCK:
                return LocationBasedTriggerManager.Activity.LEFT_CLICK_BLOCK;
            case RIGHT_CLICK_AIR:
                return LocationBasedTriggerManager.Activity.RIGHT_CLICK_AIR;
            case RIGHT_CLICK_BLOCK:
                return LocationBasedTriggerManager.Activity.RIGHT_CLICK_BLOCK;
            default:
                return LocationBasedTriggerManager.Activity.NONE;
        }
    }
}
