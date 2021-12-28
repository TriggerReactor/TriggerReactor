package io.github.wysohn.triggerreactor.bukkit.listeners.trigger;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitItemStack;
import io.github.wysohn.triggerreactor.bukkit.listeners.AbstractBukkitListener;
import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.click.ClickTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.walk.WalkTriggerManager;
import org.bukkit.ChatColor;
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

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

public final class LocationTriggerListener extends AbstractBukkitListener {
    @Inject
    IWrapper wrapper;
    @Inject
    ClickTriggerManager clickTriggerManager;
    @Inject
    WalkTriggerManager walkTriggerManager;

    @Inject
    LocationTriggerListener(){

    }

    /// shared

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClickForClickTrigger(PlayerInteractEvent e) {
        onClick(e, clickTriggerManager);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClickForWalkTrigger(PlayerInteractEvent e) {
        onClick(e, walkTriggerManager);
    }

    private void onClick(PlayerInteractEvent e, AbstractLocationBasedTriggerManager<?> manager) {
        if (!BukkitUtil.isLeftHandClick(e))
            return;

        Player player = e.getPlayer();
        Block clicked = e.getClickedBlock();
        if (clicked == null)
            return;

        if(e.isCancelled())
            return;

        AbstractLocationBasedTriggerManager.ClickType type;
        if(e.getAction() == Action.LEFT_CLICK_BLOCK)
            type = AbstractLocationBasedTriggerManager.ClickType.LEFT_CLICK;
        else if(e.getAction() == Action.RIGHT_CLICK_BLOCK)
            type = AbstractLocationBasedTriggerManager.ClickType.RIGHT_CLICK;
        else
            return;

        e.setCancelled(
                !manager.onClick(LocationUtil.convertToSimpleLocation(clicked.getLocation()), wrapper.wrap(player),
                        type));
    }

    @EventHandler
    public void onItemSwapClickTrigger(PlayerItemHeldEvent e) {
        clickTriggerManager.onItemSwap(wrapper.wrap(e.getPlayer()));
    }

    @EventHandler
    public void onItemSwapWalkTrigger(PlayerItemHeldEvent e) {
        walkTriggerManager.onItemSwap(wrapper.wrap(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSignBreakClickTrigger(BlockBreakEvent e) {
        if (e.isCancelled())
            return;

        onSignBreak(e, clickTriggerManager);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSignBreakWalkTrigger(BlockBreakEvent e) {
        if (e.isCancelled())
            return;

        onSignBreak(e, walkTriggerManager);
    }

    public void onSignBreak(BlockBreakEvent e, AbstractLocationBasedTriggerManager<?> manager) {
        Block block = e.getBlock();
        Block above = block.getRelative(BlockFace.UP);

        // check if this break event of the block
        // will cause the destruction of sign above it
        if (!above.getType().name().equals("SIGN_POST") && !above.getType().name().endsWith("_SIGN"))
            return;

        BlockBreakEvent bbe = new BlockBreakEvent(above, e.getPlayer());
        onBreak(bbe, manager);
        e.setCancelled(bbe.isCancelled());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onWallSignBreakClick(BlockBreakEvent e) {
        if (e.isCancelled())
            return;

        onWallSignBreak(e, clickTriggerManager);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onWallSignBreakWalk(BlockBreakEvent e) {
        if (e.isCancelled())
            return;

        onWallSignBreak(e, walkTriggerManager);
    }

    public void onWallSignBreak(BlockBreakEvent e, AbstractLocationBasedTriggerManager<?> manager) {
        Block block = e.getBlock();
        for (Block surrounding : getSurroundingBlocks(block, (b) -> b.getType() == Material.WALL_SIGN)) {
            BlockBreakEvent bbe = new BlockBreakEvent(surrounding, e.getPlayer());
            onBreak(bbe, manager);

            // at least one sign is not breakable, so cancel the event and stop iteration
            if (bbe.isCancelled()) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onBreakClickTrigger(BlockBreakEvent e) {
        onBreak(e, clickTriggerManager);
    }

    @EventHandler
    public void onBreakWalkTrigger(BlockBreakEvent e) {
        onBreak(e, walkTriggerManager);
    }

    public void onBreak(BlockBreakEvent e, AbstractLocationBasedTriggerManager<?> manager) {
        if (e.isCancelled())
            return;

        if (!manager.isTrigger(LocationUtil.convertToSimpleLocation(e.getBlock().getLocation())))
            return;

        Player player = e.getPlayer();

        player.sendMessage(ChatColor.GRAY + "Cannot break trigger block.");
        player.sendMessage(ChatColor.GRAY + "To remove trigger, hold inspection tool " + BukkitItemStack.INSPECTION_TOOL.name());
        e.setCancelled(true);
    }

    private Collection<Block> getSurroundingBlocks(Block block, Predicate<Block> pred) {
        Collection<Block> blocks = new ArrayList<>();
        Predicate<Block> notNull = Objects::nonNull;

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

    /// click trigger
    @EventHandler(ignoreCancelled = true)
    public void onClickTrigger(PlayerInteractEvent e) {
        if (!BukkitUtil.isLeftHandClick(e))
            return;

        Player player = e.getPlayer();
        Block clicked = e.getClickedBlock();

        if (clicked == null)
            return;

        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(clicked.getLocation());
        AbstractLocationBasedTriggerManager.ClickType type;
        switch (e.getAction()){
            case LEFT_CLICK_BLOCK:
                type = AbstractLocationBasedTriggerManager.ClickType.LEFT_CLICK;
                break;
            case LEFT_CLICK_AIR:
                type = AbstractLocationBasedTriggerManager.ClickType.LEFT_CLICK_AIR;
                break;
            case RIGHT_CLICK_BLOCK:
                type = AbstractLocationBasedTriggerManager.ClickType.RIGHT_CLICK;
                break;
            case RIGHT_CLICK_AIR:
                type = AbstractLocationBasedTriggerManager.ClickType.RIGHT_CLICK_AIR;
                break;
            default:
                type = AbstractLocationBasedTriggerManager.ClickType.UNKNOWN;
                break;
        }

        clickTriggerManager.onClickTrigger(e, player, clicked, e.getItem(), sloc, type);
    }

    /// walk trigger
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerBlockLocationEvent e) {
        Player player = e.getPlayer();
        SimpleLocation bottomLoc = e.getTo().clone();
        bottomLoc.add(0, -1, 0);
        Block block = LocationUtil.convertToBukkitLocation(bottomLoc).getBlock();

        walkTriggerManager.onMove(e, player, e.getFrom(), e.getTo(), block, bottomLoc);
    }
}
