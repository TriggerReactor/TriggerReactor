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
package io.github.wysohn.triggerreactor.sponge.manager.trigger;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.sponge.bridge.entity.SpongePlayer;
import io.github.wysohn.triggerreactor.sponge.tools.LocationUtil;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.util.*;

public abstract class LocationBasedTriggerManager<T extends Trigger> extends AbstractLocationBasedTriggerManager<T> {
    public static final ItemType INSPECTION_TOOL = ItemTypes.BONE;
    public static final ItemType CUT_TOOL = ItemTypes.SHEARS;
    public static final ItemType COPY_TOOL = ItemTypes.PAPER;

    public LocationBasedTriggerManager(TriggerReactorCore plugin, String folderName, ITriggerLoader<T> loader) {
        super(plugin, new File(plugin.getDataFolder(), folderName), loader);
    }

    @Listener(order = Order.LATE)
    @Exclude({InteractBlockEvent.Primary.OffHand.class, InteractBlockEvent.Secondary.OffHand.class})
    public void onClick(InteractBlockEvent e) {
        Player player = e.getCause().first(Player.class).orElse(null);
        //maybe something other than a player can interact with a block?
        if (player == null)
            return;

        ItemStack IS = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
        BlockSnapshot clicked = e.getTargetBlock();
        if (clicked == null)
            return;

        Location<World> loc = clicked.getLocation().orElse(null);
        if (loc == null)
            return;

        T trigger = getTriggerForLocation(loc);

        if (IS != null
                && !e.isCancelled()
                && player.hasPermission("triggerreactor.admin")) {

            if (IS.getType() == INSPECTION_TOOL) {
                if (trigger != null && e instanceof InteractBlockEvent.Primary) {
                    removeTriggerForLocation(loc);

                    player.sendMessage(Text.builder("A trigger has deleted.").color(TextColors.GREEN).build());
                    e.setCancelled(true);
                } else if (trigger != null && e instanceof InteractBlockEvent.Secondary) {
                    if (player.get(Keys.IS_SNEAKING).orElse(false)) {
                        handleScriptEdit(player, trigger);
                        e.setCancelled(true);
                    } else {
                        this.showTriggerInfo(new SpongePlayer(player), clicked);
                        e.setCancelled(true);
                    }
                }
            } else if (IS.getType() == CUT_TOOL) {
                if (e instanceof InteractBlockEvent.Primary) {
                    if (pasteTrigger(player, loc)) {
                        player.sendMessage(Text.builder("Successfully pasted the trigger!").color(TextColors.GREEN).build());
                        this.showTriggerInfo(new SpongePlayer(player), clicked);
                        e.setCancelled(true);
                    }
                } else if (trigger != null && e instanceof InteractBlockEvent.Secondary) {
                    if (cutTrigger(player, loc)) {
                        player.sendMessage(Text.builder("Cut Complete!").color(TextColors.GREEN).build());
                        player.sendMessage(Text.builder("Now you can paste it by left click on any block!").color(TextColors.GREEN).build());
                        e.setCancelled(true);
                    }
                }
            } else if (IS.getType() == COPY_TOOL) {
                if (e instanceof InteractBlockEvent.Primary) {
                    if (pasteTrigger(player, loc)) {
                        player.sendMessage(Text.builder("Successfully pasted the trigger!").color(TextColors.GREEN).build());
                        this.showTriggerInfo(new SpongePlayer(player), clicked);
                        e.setCancelled(true);
                    }
                } else if (e instanceof InteractBlockEvent.Secondary) {
                    if (trigger != null && copyTrigger(player, loc)) {
                        player.sendMessage(Text.builder("Copy Complete!").color(TextColors.GREEN).build());
                        player.sendMessage(Text.builder("Now you can paste it by left click on any block!").color(TextColors.GREEN).build());
                        e.setCancelled(true);
                    }
                }
            }
        }

        if (!e.isCancelled() && isLocationSetting(new SpongePlayer(player))) {
            handleLocationSetting(clicked, player);
            e.setCancelled(true);
        }
    }

    private void handleLocationSetting(BlockSnapshot clicked, Player p) {
        IPlayer player = new SpongePlayer(p);

        Location<World> loc = clicked.getLocation().orElse(null);
        if (loc == null)
            return;

        T trigger = getTriggerForLocation(loc);
        if (trigger != null) {
            p.sendMessage(Text.builder("Another trigger is set at there!").color(TextColors.RED).build());
            showTriggerInfo(player, clicked);
            return;
        }

        String script = getSettingLocationScript(player);
        if (script == null) {
            p.sendMessage(Text.builder("Could not find script... but how?").color(TextColors.RED).build());
            return;
        }

        File file = getTriggerFile(folder, LocationUtil.convertToSimpleLocation(loc).toString(), true);
        try {
            String name = TriggerInfo.extractName(file);
            IConfigSource config = configSourceFactory.create(folder, name);
            TriggerInfo info = TriggerInfo.defaultInfo(file, config);
            trigger = newTrigger(info, script);
        } catch (Exception e1) {
            p.sendMessage(Text.builder("Encounterd an error!").color(TextColors.RED).build());
            p.sendMessage(Text.builder(e1.getMessage()).color(TextColors.RED).build());
            p.sendMessage(Text.builder("If you are an administrator, check console to see details.").color(TextColors.RED).build());
            e1.printStackTrace();

            stopLocationSet(player);
            return;
        }

        setTriggerForLocation(loc, trigger);

        showTriggerInfo(player, clicked);

        stopLocationSet(player);

        plugin.saveAsynchronously(this);
    }

    private void handleScriptEdit(Player player, T trigger) {

        plugin.getScriptEditManager().startEdit(new SpongePlayer(player), trigger.getInfo().getTriggerName(), trigger.getScript(),
                new SaveHandler() {
                    @Override
                    public void onSave(String script) {
                        try {
                            trigger.setScript(script);
                        } catch (TriggerInitFailedException e) {
                            plugin.handleException(new SpongePlayer(player), e);
                        }

                        plugin.saveAsynchronously(LocationBasedTriggerManager.this);
                    }

                });
    }

    @Listener(order = Order.LATE)
    public void onSignBreak(ChangeBlockEvent.Break e) {
        if (e.isCancelled())
            return;

        for (Transaction<BlockSnapshot> transaction : e.getTransactions()) {
            BlockSnapshot snapshot = transaction.getOriginal();
            Location<World> loc = snapshot.getLocation().orElse(null);

            BlockState state = snapshot.getState();
            BlockState above = loc.getExtent().getBlock(0, 1, 0);

            if (above.getType() != BlockTypes.STANDING_SIGN && above.getType() != BlockTypes.WALL_SIGN)
                return;

            ChangeBlockEvent.Break bbe = new ChangeBlockEvent.Break() {
                private boolean cancelled = false;

                @SuppressWarnings("serial")
                @Override
                public List<Transaction<BlockSnapshot>> getTransactions() {
                    return new ArrayList<Transaction<BlockSnapshot>>() {{
                        add(transaction);
                    }};
                }

                @Override
                public Cause getCause() {
                    return e.getCause();
                }

                @Override
                public boolean isCancelled() {
                    return cancelled;
                }

                @Override
                public void setCancelled(boolean cancel) {
                    this.cancelled = cancel;
                }

            };
            onBreak(bbe);
            e.setCancelled(bbe.isCancelled());
        }
    }

    @Listener
    public void onBreak(ChangeBlockEvent.Break e) {
        for (Transaction<BlockSnapshot> transaction : e.getTransactions()) {
            BlockSnapshot block = transaction.getOriginal();

            Location<World> loc = block.getLocation().orElse(null);
            if (loc == null)
                return;

            T trigger = getTriggerForLocation(loc);
            if (trigger == null)
                return;

            Player player = e.getCause().first(Player.class).orElse(null);

            player.sendMessage(Text.builder("Cannot break trigger block.").color(TextColors.GRAY).build());
            player.sendMessage(Text.builder("To remove trigger, hold inspection tool ").color(TextColors.GRAY).build());
            e.setCancelled(true);
        }
    }

    @Listener
    public void onTnTBreaK(ExplosionEvent.Detonate e) {
        for (Iterator<Location<World>> iter = e.getAffectedLocations().iterator(); iter.hasNext(); ) {
            T trigger = getTriggerForLocation(iter.next());
            if (trigger == null)
                continue;

            iter.remove();
        }
    }

    @Listener
    public void onItemSwap(ChangeInventoryEvent.Held e) {
        onItemSwap(new SpongePlayer(e.getCause().first(Player.class).orElse(null)));
    }

    protected T getTriggerForLocation(Location<World> loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return getTriggerForLocation(sloc);
    }

    protected void setTriggerForLocation(Location<World> loc, T trigger) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        setLocationCache(sloc, trigger);
    }

    protected T removeTriggerForLocation(Location<World> loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return removeLocationCache(sloc);
    }

    protected void showTriggerInfo(ICommandSender sender, BlockSnapshot clicked) {
        Trigger trigger = getTriggerForLocation(LocationUtil.convertToSimpleLocation(clicked.getLocation().orElse(null)));
        if (trigger == null) {
            return;
        }

        Location<World> loc = clicked.getLocation().orElse(null);
        sender.sendMessage("- - - - - - - - - - - - - -");
        sender.sendMessage("Trigger: " + getTriggerTypeName());
        sender.sendMessage("Block Type: " + clicked.getState().getType().getName());
        sender.sendMessage("Location: " + loc.getExtent().getName() + "@" + loc.getBlockX() + ","
                + loc.getBlockY() + "," + loc.getBlockZ());
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

        Location<World> loc = LocationUtil.convertToBukkitLocation(sloc);
        BlockState clicked = loc.getBlock();

        sender.sendMessage("- - - - - - - - - - - - - -");
        sender.sendMessage("Trigger: " + getTriggerTypeName());
        sender.sendMessage("Block Type: " + clicked.getType().getName());
        sender.sendMessage("Location: " + loc.getExtent().getName() + "@" + loc.getBlockX() + ","
                + loc.getBlockY() + "," + loc.getBlockZ());
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
    protected boolean cutTrigger(Player player, Location<World> loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return cutTrigger(new SpongePlayer(player), sloc);
    }

    /**
     * @param player
     * @param loc
     * @return true if copy ready; false if no trigger found at the location
     */
    protected boolean copyTrigger(Player player, Location<World> loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return copyTrigger(new SpongePlayer(player), sloc);
    }

    /**
     * @param player
     * @param loc
     * @return true if pasted; false if nothing in the clipboard
     */
    protected boolean pasteTrigger(Player player, Location<World> loc) {
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        return pasteTrigger(new SpongePlayer(player), sloc);
    }

    protected Set<Map.Entry<SimpleLocation, Trigger>> getTriggersInChunk(Chunk chunk) {
        SimpleChunkLocation scloc = LocationUtil.convertToSimpleChunkLocation(chunk);
        return getTriggersInChunk(scloc);
    }
}
