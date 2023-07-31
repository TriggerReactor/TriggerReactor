/*
 * Copyright (C) 2022. TriggerReactor Team
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
package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import io.github.wysohn.triggerreactor.core.bridge.*;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.IExceptionHandle;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.ScriptEditManager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTaggedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerLoader;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import javax.inject.Inject;
import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Logger;

public abstract class LocationBasedTriggerManager<T extends Trigger> extends AbstractTaggedTriggerManager<T> {
    protected final Map<SimpleChunkLocation, Map<SimpleLocation, T>> chunkMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> settingLocation = new HashMap<>();
    private final Map<UUID, ClipBoard> clipboard = new HashMap<>();

    @Inject
    private Logger logger;
    @Inject
    private IPluginManagement pluginManagement;
    @Inject
    private IExceptionHandle exceptionHandle;
    @Inject
    private ScriptEditManager scriptEditManager;
    @Inject
    private ITriggerLoader<T> loader;

    public LocationBasedTriggerManager(File folder) {
        super(folder);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void reload() {
        super.reload();

        chunkMap.clear();

        for (T trigger : getAllTriggers()) {
            SimpleLocation sloc = null;
            try {
                sloc = SimpleLocation.valueOf(trigger.getInfo().getTriggerName());
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

            Map<SimpleLocation, T> locationMap = chunkMap.get(scloc);
            if (!chunkMap.containsKey(scloc)) {
                locationMap = new ConcurrentHashMap<>();
                chunkMap.put(scloc, locationMap);
            }

            if (locationMap.containsKey(sloc)) {
                Trigger previous = locationMap.get(sloc);
                logger.warning("Found a duplicating " + trigger.getClass().getSimpleName());
                logger.warning("Existing: " + previous.getInfo().getSourceCodeFile().getAbsolutePath());
                logger.warning("Skipped: " + trigger.getInfo().getSourceCodeFile().getAbsolutePath());
            } else {
                locationMap.put(sloc, trigger);
            }
        }
    }

    public abstract String getTriggerTypeName();

    public T getTriggerForLocation(ILocation loc) {
        SimpleLocation sloc = loc.toSimpleLocation();
        return getTriggerForLocation(sloc);
    }

    public T getTriggerForLocation(SimpleLocation sloc) {
        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

        if (!chunkMap.containsKey(scloc))
            return null;

        Map<SimpleLocation, T> locationMap = chunkMap.get(scloc);
        if (!locationMap.containsKey(sloc))
            return null;

        return locationMap.get(sloc);
    }

    protected void setLocationCache(ILocation loc, T trigger) {
        SimpleLocation sloc = loc.toSimpleLocation();
        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

        Map<SimpleLocation, T> locationMap = chunkMap.get(scloc);
        if (!chunkMap.containsKey(scloc)) {
            locationMap = new ConcurrentHashMap<>();
            chunkMap.put(scloc, locationMap);
        }

        locationMap.put(sloc, trigger);
        put(sloc.toString(), trigger);
    }

    protected T removeLocationCache(ILocation loc) {
        SimpleLocation sloc = loc.toSimpleLocation();
        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);

        Map<SimpleLocation, T> locationMap = chunkMap.get(scloc);
        if (!chunkMap.containsKey(scloc)) {
            return null;
        }

        T result = locationMap.remove(sloc);
        remove(sloc.toString());
        return result;
    }

    public boolean isLocationSetting(IPlayer player) {
        return settingLocation.containsKey(player.getUniqueId());
    }

    /**
     * Initiate the trigger creation process. This will set the player into
     * "location setting" mode, and will cache the trigger script for later use.
     * {@link #handleLocationSetting(ILocation, IPlayer)}
     *
     * @param player the player who is creating the trigger
     * @param script the script to be used for the trigger
     * @return true if the player is successfully set into "location setting" mode; false otherwise (already setting).
     */
    public boolean startLocationSet(IPlayer player, String script) {
        if (settingLocation.containsKey(player.getUniqueId()))
            return false;

        settingLocation.put(player.getUniqueId(), script);

        return true;
    }

    protected boolean stopLocationSet(IPlayer player) {
        if (!settingLocation.containsKey(player.getUniqueId()))
            return false;

        settingLocation.remove(player.getUniqueId());

        return true;
    }

    protected String getSettingLocationScript(IPlayer player) {
        return settingLocation.get(player.getUniqueId());
    }

    /**
     * @param player
     * @param loc
     * @return true if cut ready; false if no trigger found at the location
     */
    public boolean cutTrigger(IPlayer player, ILocation loc) {
        T trigger = getTriggerForLocation(loc);
        if (trigger == null) {
            return false;
        }

        clipboard.put(player.getUniqueId(), new ClipBoard(ClipBoard.BoardType.CUT, loc));
        return true;
    }


    /**
     * @param player
     * @param loc
     * @return true if copy ready; false if no trigger found at the location
     */
    public boolean copyTrigger(IPlayer player, ILocation loc) {
        T trigger = getTriggerForLocation(loc);
        if (trigger == null) {
            return false;
        }

        clipboard.put(player.getUniqueId(), new ClipBoard(ClipBoard.BoardType.COPY, loc));
        return true;
    }


    /**
     * @param player
     * @param loc
     * @return true if pasted; false if nothing in the clipboard
     */
    public boolean pasteTrigger(IPlayer player, ILocation loc) {
        ClipBoard board = clipboard.get(player.getUniqueId());
        if (board == null)
            return false;

        ILocation from = board.location;
        if (from == null) {
            return false;
        }

        T trigger = getTriggerForLocation(from);
        if (trigger == null) {
            return false;
        }

        try {
            if (board.type == ClipBoard.BoardType.CUT)
                trigger = removeLocationCache(board.location);

            T copy = (T) trigger.clone();

            setLocationCache(loc, copy);
            clipboard.remove(player.getUniqueId());
        } catch (Exception e) {
            e.printStackTrace();
            //put it back if failed
            if (board.type == ClipBoard.BoardType.CUT && trigger != null) {
                setLocationCache(board.location, (T) trigger.clone());
            }
        }

        return true;
    }

    public Set<Map.Entry<SimpleLocation, Trigger>> getTriggersInChunk(SimpleChunkLocation scloc) {
        Set<Map.Entry<SimpleLocation, Trigger>> triggers = new HashSet<>();
        if (!chunkMap.containsKey(scloc))
            return triggers;

        // TODO this isn't right. we don't return triggers of every chunk, but only the chunk specified
        //   also fix the test code
        for (Entry<SimpleChunkLocation, Map<SimpleLocation, T>> entry : chunkMap.entrySet()) {
            for (Entry<SimpleLocation, T> entryIn : entry.getValue().entrySet()) {
                triggers.add(new SimpleEntry<>(entryIn.getKey(), entryIn.getValue()));
            }
        }

        return triggers;
    }

    private String asTriggerName(ILocation loc) {
        return loc.toSimpleLocation().toString();
    }

    /**
     * Finalize the pending trigger creation initiated by {@link #startLocationSet(IPlayer, String)}.
     * If successful, the trigger will be created with the script provided in
     * {@link #startLocationSet(IPlayer, String)}.
     *
     * @param loc    the location to save the pending trigger
     * @param player the player who initiated the trigger creation
     * @deprecated for test or listener use only
     */
    @Deprecated
    public void handleLocationSetting(ILocation loc, IPlayer player) {
        T trigger = getTriggerForLocation(loc);
        if (trigger != null) {
            player.sendMessage("&cAnother trigger is set at there!");
            showTriggerInfo(player, loc);
            return;
        }

        String script = getSettingLocationScript(player);
        if (script == null) {
            player.sendMessage("&cCould not find script... but how?");
            return;
        }

        File file = getTriggerFile(folder, asTriggerName(loc), true);
        try {
            String name = TriggerInfo.extractName(file);
            IConfigSource config = configSourceFactory.create(folder, name);
            TriggerInfo info = TriggerInfo.defaultInfo(file, config);
            trigger = this.newInstance(info, script);
        } catch (Exception e1) {
            player.sendMessage("&cEncountered an error!");
            player.sendMessage("&c" + e1.getMessage());
            player.sendMessage("&cIf you are an administrator, check console to see details.");
            e1.printStackTrace();

            stopLocationSet(player);
            return;
        }

        setTriggerForLocation(loc, trigger);
        showTriggerInfo(player, loc);
        stopLocationSet(player);


    }

    protected abstract T newInstance(TriggerInfo info, String script) throws TriggerInitFailedException;

    /**
     * Initiate editing of a trigger. This will open the in-game text editor for the
     * player to edit the trigger script. (ex. Shift right click with bone to edit)
     *
     * @param player  the player who is editing the trigger
     * @param trigger the trigger to be edited
     */
    public void handleScriptEdit(IPlayer player, T trigger) {
        scriptEditManager.startEdit(player, trigger.getInfo().getTriggerName(), trigger.getScript(),
                script -> {
                    try {
                        trigger.setScript(script);
                    } catch (TriggerInitFailedException e) {
                        exceptionHandle.handleException(player, e);
                    }

                    loader.save(trigger);
                });
    }

    public Collection<SimpleLocation> getSurroundingBlocks(SimpleLocation block, Predicate<SimpleLocation> pred) {
        Collection<SimpleLocation> blocks = new ArrayList<>();
        Predicate<SimpleLocation> notNull = Objects::nonNull;

        SimpleLocation relative = null;

        relative = block.add(-1, 0, 0);
        if (notNull.and(pred).test(relative)) {
            blocks.add(relative);
        }

        relative = block.add(1, 0, 0);
        if (notNull.and(pred).test(relative)) {
            blocks.add(relative);
        }

        relative = block.add(0, 0, -1);
        if (notNull.and(pred).test(relative)) {
            blocks.add(relative);
        }

        relative = block.add(0, 0, 1);
        if (notNull.and(pred).test(relative)) {
            blocks.add(relative);
        }

        return blocks;
    }

    protected void setTriggerForLocation(ILocation loc, T trigger) {
        setLocationCache(loc, trigger);
    }

    public T removeTriggerForLocation(ILocation loc) {
        return removeLocationCache(loc);
    }

    public void showTriggerInfo(ICommandSender sender, ILocation clicked) {
        Trigger trigger = getTriggerForLocation(clicked);
        if (trigger == null) {
            return;
        }

        IWorld world = clicked.getWorld();

        sender.sendMessage("- - - - - - - - - - - - - -");
        sender.sendMessage("Trigger: " + getTriggerTypeName());
        sender.sendMessage("Block Type: " + world.getBlock(clicked).getTypeName());
        sender.sendMessage("Location: " + clicked.toSimpleLocation());
        sender.sendMessage("");
        sender.sendMessage("Script:");
        sender.sendMessage(trigger.getScript());
        sender.sendMessage("- - - - - - - - - - - - - -");
    }

    /**
     * @param player
     * @deprecated Event handler. Do not call this method except from listener or tests.
     */
    public void onItemSwap(IPlayer player) {
        if (player.getUniqueId() == null)
            return;

        clipboard.remove(player.getUniqueId());
    }

    /**
     * @param eventInstance
     * @param clicked
     * @param player
     * @param itemInHand
     * @param activity
     * @deprecated Event handler. Do not call this method except from listener or tests.
     */
    public void handleClick(Object eventInstance,
                            IBlock clicked,
                            IPlayer player,
                            IItemStack itemInHand,
                            Activity activity) {
        if (clicked == null)
            return;

        ILocation locationWrapped = clicked.getLocation();
        T trigger = getTriggerForLocation(locationWrapped);
        if (trigger == null)
            return;

        Map<String, Object> varMap = new HashMap<>();
        varMap.put(LocationBasedTriggerManager.KEY_CONTEXT_ACTIVITY, activity);
        varMap.put("player", player.get());
        varMap.put("block", clicked.get());
        varMap.put("itemInHand", itemInHand.get());
        switch (activity) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                varMap.put("click", "left");
                break;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                varMap.put("click", "right");
                break;
            default:
                varMap.put("click", "unknown");
        }

        trigger.activate(eventInstance, varMap);
    }

    /**
     * @param eventInstance
     * @param player
     * @param from
     * @param to
     * @param bottomBlock
     * @deprecated Event handler. Do not call this method except from listener or tests.
     */
    public void handleWalk(Object eventInstance,
                           IPlayer player,
                           SimpleLocation from,
                           SimpleLocation to,
                           IBlock bottomBlock) {
        SimpleLocation bottomLoc = to.add(0, -1, 0);

        T trigger = getTriggerForLocation(bottomLoc);
        if (trigger == null)
            return;

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("player", player.get());
        varMap.put("from", from);
        varMap.put("to", to);
        varMap.put("block", bottomBlock.get());

        trigger.activate(eventInstance, varMap);
    }

    private static class ClipBoard {
        final BoardType type;
        final ILocation location;

        public ClipBoard(BoardType type, ILocation location) {
            this.type = type;
            this.location = location;
        }

        enum BoardType {
            CUT,
            COPY
        }
    }

    public static String KEY_CONTEXT_ACTIVITY = "location.activity";
}
