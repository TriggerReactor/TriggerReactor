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

import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager.Trigger;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public abstract class LocationBasedTriggerManager<T extends Trigger> extends AbstractLocationBasedTriggerManager<T>
        implements BukkitTriggerManager {
    public static final Material INSPECTION_TOOL = Material.BONE;
    public static final Material CUT_TOOL = Material.SHEARS;
    public static final Material COPY_TOOL = Material.PAPER;

    public LocationBasedTriggerManager(TriggerReactorCore plugin, String folderName) {
        super(plugin, new File(plugin.getDataFolder(), folderName));
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

        T trigger = getTriggerForLocation(clicked.getLocation());
        IPlayer bukkitPlayer = plugin.getWrapper().wrap(player);
        
        if (IS != null && !e.isCancelled() && player.hasPermission("triggerreactor.admin")) {

            if (IS.getType() == INSPECTION_TOOL) {
                if (trigger != null && e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    removeTriggerForLocation(clicked.getLocation());

                    player.sendMessage(ChatColor.GREEN + "A trigger has deleted.");
                    e.setCancelled(true);
                } else if (trigger != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (e.getPlayer().isSneaking()) {
                        handleScriptEdit(player, trigger);
                        e.setCancelled(true);
                    } else {
                        this.showTriggerInfo(bukkitPlayer, clicked);
                        e.setCancelled(true);
                    }
                }
            } else if (IS.getType() == CUT_TOOL) {
                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if (pasteTrigger(player, clicked.getLocation())) {
                        player.sendMessage(ChatColor.GREEN + "Successfully pasted the trigger!");
                        this.showTriggerInfo(bukkitPlayer, clicked);
                        e.setCancelled(true);
                    }
                } else if (trigger != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (cutTrigger(player, clicked.getLocation())) {
                        player.sendMessage(ChatColor.GREEN + "Cut Complete!");
                        player.sendMessage(ChatColor.GREEN + "Now you can paste it by left click on any block!");
                        e.setCancelled(true);
                    }
                }
            } else if (IS.getType() == COPY_TOOL) {
                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if (pasteTrigger(player, clicked.getLocation())) {
                        player.sendMessage(ChatColor.GREEN + "Successfully pasted the trigger!");
                        this.showTriggerInfo(bukkitPlayer, clicked);
                        e.setCancelled(true);
                    }
                } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (trigger != null && copyTrigger(player, clicked.getLocation())) {
                        player.sendMessage(ChatColor.GREEN + "Copy Complete!");
                        player.sendMessage(ChatColor.GREEN + "Now you can paste it by left click on any block!");
                        e.setCancelled(true);
                    }
                }
            }
        }

        if (!e.isCancelled() && isLocationSetting(bukkitPlayer)) {
            handleLocationSetting(clicked, player);
            e.setCancelled(true);
        }
    }

    private void handleLocationSetting(Block clicked, Player p) {
    	IPlayer bukkitPlayer = plugin.getWrapper().wrap(p);

        Location loc = clicked.getLocation();
        T trigger = getTriggerForLocation(loc);
        if (trigger != null) {
            bukkitPlayer.sendMessage(ChatColor.RED + "Another trigger is set at there!");
            showTriggerInfo(bukkitPlayer, clicked);
            return;
        }

        String script = getSettingLocationScript(bukkitPlayer);
        if (script == null) {
            bukkitPlayer.sendMessage(ChatColor.RED + "Could not find script... but how?");
            return;
        }

        try {
            trigger = constructTrigger(LocationUtil.convertToSimpleLocation(loc), script);
        } catch (TriggerInitFailedException e1) {
            bukkitPlayer.sendMessage(ChatColor.RED + "Encounterd an error!");
            bukkitPlayer.sendMessage(ChatColor.RED + e1.getMessage());
            bukkitPlayer.sendMessage(ChatColor.RED + "If you are an administrator, check console to see details.");
            e1.printStackTrace();

            stopLocationSet(bukkitPlayer);
            return;
        }

        setTriggerForLocation(loc, trigger);

        showTriggerInfo(bukkitPlayer, clicked);

        stopLocationSet(bukkitPlayer);

        plugin.saveAsynchronously(this);
    }

    private void handleScriptEdit(Player player, T trigger) {
    	IPlayer bukkitPlayer = plugin.getWrapper().wrap(player);
    	
        plugin.getScriptEditManager().startEdit(bukkitPlayer, trigger.getTriggerName(), trigger.getScript(),
                new SaveHandler() {
                    @Override
                    public void onSave(String script) {
                        try {
                            trigger.setScript(script);
                        } catch (TriggerInitFailedException e) {
                            plugin.handleException(bukkitPlayer, e);
                        }

                        plugin.saveAsynchronously(LocationBasedTriggerManager.this);
                    }

                });
    }

    private Collection<Block> getSurroundingBlocks(Block block, Predicate<Block> pred) {
        Collection<Block> blocks = new ArrayList<>();
        Predicate<Block> notNull = b -> b != null;

        Block relative = null;

        relative = block.getRelative(BlockFace.NORTH);
        if (notNull.and(pred).test(relative)) {
            blocks.add(relative);
        }

        relative = block.getRelative(BlockFace.SOUTH);
        if (notNull.and(pred).test(relative)) {
            blocks.add(relative);
        }

        relative = block.getRelative(BlockFace.EAST);
        if (notNull.and(pred).test(relative)) {
            blocks.add(relative);
        }

        relative = block.getRelative(BlockFace.WEST);
        if (notNull.and(pred).test(relative)) {
            blocks.add(relative);
        }

        return blocks;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onWallSignBreak(BlockBreakEvent e) {
        if (e.isCancelled())
            return;

        Block block = e.getBlock();
        for (Block surronding : getSurroundingBlocks(block, (b) -> b.getType() == Material.WALL_SIGN)) {
            BlockBreakEvent bbe = new BlockBreakEvent(surronding, e.getPlayer());
            onBreak(bbe);

            // at least one sign is not breakable, so cancel the event and stop iteration
            if (bbe.isCancelled()) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSignBreak(BlockBreakEvent e) {
        if (e.isCancelled())
            return;

        Block block = e.getBlock();
        Block above = block.getRelative(BlockFace.UP);

        // check if this break event of the block
        // will cause the destruction of sign above it
        if (above.getType() != Material.SIGN_POST)
            return;

        BlockBreakEvent bbe = new BlockBreakEvent(above, e.getPlayer());
        onBreak(bbe);
        e.setCancelled(bbe.isCancelled());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Block block = e.getBlock();

        T trigger = getTriggerForLocation(block.getLocation());
        if (trigger == null)
            return;

        Player player = e.getPlayer();

        player.sendMessage(ChatColor.GRAY + "Cannot break trigger block.");
        player.sendMessage(ChatColor.GRAY + "To remove trigger, hold inspection tool " + INSPECTION_TOOL.name());
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemSwap(PlayerItemHeldEvent e) {
        onItemSwap((IPlayer) plugin.getWrapper().wrap(e.getPlayer()));
    }

    protected T getTriggerForLocation(Location loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return getTriggerForLocation(sloc);
    }

    protected void setTriggerForLocation(Location loc, T trigger) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        setTriggerForLocation(sloc, trigger);
    }

    protected T removeTriggerForLocation(Location loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return removeTriggerForLocation(sloc);
    }

    protected void showTriggerInfo(ICommandSender sender, Block clicked) {
        Trigger trigger = getTriggerForLocation(LocationUtil.convertToSimpleLocation(clicked.getLocation()));
        if (trigger == null) {
            return;
        }

        sender.sendMessage("- - - - - - - - - - - - - -");
        sender.sendMessage("Trigger: " + getTriggerTypeName());
        sender.sendMessage("Block Type: " + clicked.getType().name());
        sender.sendMessage("Location: " + clicked.getWorld().getName() + "@" + clicked.getLocation().getBlockX() + ","
                + clicked.getLocation().getBlockY() + "," + clicked.getLocation().getBlockZ());
        sender.sendMessage("");
        sender.sendMessage("Script:");
        sender.sendMessage(trigger.getScript());
        sender.sendMessage("- - - - - - - - - - - - - -");
    }

    @Override
    protected void showTriggerInfo(ICommandSender sender, SimpleLocation sloc) {
        Trigger trigger = getTriggerForLocation(sloc);
        if (trigger == null) {
            return;
        }

        Location loc = LocationUtil.convertToBukkitLocation(sloc);
        Block clicked = loc.getBlock();

        sender.sendMessage("- - - - - - - - - - - - - -");
        sender.sendMessage("Trigger: " + getTriggerTypeName());
        sender.sendMessage("Block Type: " + clicked.getType().name());
        sender.sendMessage("Location: " + clicked.getWorld().getName() + "@" + clicked.getLocation().getBlockX() + ","
                + clicked.getLocation().getBlockY() + "," + clicked.getLocation().getBlockZ());
        sender.sendMessage("");
        sender.sendMessage("Script:");
        sender.sendMessage(trigger.getScript());
        sender.sendMessage("- - - - - - - - - - - - - -");
    }

    /**
     * @param player
     * @param loc
     * @return true if cut ready; false if no trigger found at the location
     */
    protected boolean cutTrigger(Player player, Location loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return cutTrigger((IPlayer) plugin.getWrapper().wrap(player), sloc);
    }

    /**
     * @param player
     * @param loc
     * @return true if copy ready; false if no trigger found at the location
     */
    protected boolean copyTrigger(Player player, Location loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return copyTrigger((IPlayer) plugin.getWrapper().wrap(player), sloc);
    }

    /**
     * @param player
     * @param loc
     * @return true if pasted; false if nothing in the clipboard
     */
    protected boolean pasteTrigger(Player player, Location loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return pasteTrigger((IPlayer) plugin.getWrapper().wrap(player), sloc);
    }

    protected Set<Map.Entry<SimpleLocation, Trigger>> getTriggersInChunk(Chunk chunk) {
        SimpleChunkLocation scloc = LocationUtil.convertToSimpleChunkLocation(chunk);
        return getTriggersInChunk(scloc);
    }
}
